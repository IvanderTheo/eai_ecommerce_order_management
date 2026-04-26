package com.example.order_management.service;

import com.example.order_management.dto.RefundRequest;
import com.example.order_management.dto.RefundResponse;
import com.example.order_management.entity.Order;
import com.example.order_management.entity.Refund;
import com.example.order_management.repository.RefundRepository;
import com.example.order_management.repository.OrderRepository;
import com.example.order_management.kafka.event.OrderEvent;
import com.example.order_management.kafka.producer.RefundProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundProducer refundProducer;

    /**
     * Request refund untuk order tertentu
     * Validasi eligibility sebelum membuat refund record
     */
    public RefundResponse requestRefund(RefundRequest request) {
        log.info("Requesting refund for orderId: {}, amount: {}", request.getOrderId(), request.getRefundAmount());

        // Validasi order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validasi refund window (e.g., 30 days)
        long daysSinceCreation = java.time.temporal.ChronoUnit.DAYS
                .between(order.getCreatedAt(), LocalDateTime.now());
        if (daysSinceCreation > 30) {
            throw new RuntimeException("Refund window expired. Orders can only be refunded within 30 days.");
        }

        // Validasi order status
        if (!order.getStatus().equals("DELIVERED") && !order.getStatus().equals("CONFIRMED")) {
            throw new RuntimeException("Cannot refund order with status: " + order.getStatus());
        }

        // Validasi refund amount
        if (request.getRefundAmount() <= 0 || request.getRefundAmount() > order.getTotalAmount()) {
            throw new RuntimeException("Invalid refund amount. Max refund: " + order.getTotalAmount());
        }

        // Cek apakah sudah ada refund untuk order ini
        Optional<Refund> existingRefund = refundRepository.findByOrderId(request.getOrderId());
        if (existingRefund.isPresent() && 
            !existingRefund.get().getStatus().equals(Refund.RefundStatus.FAILED)) {
            throw new RuntimeException("Refund already exists for this order");
        }

        // Create refund record
        Refund refund = Refund.builder()
                .orderId(request.getOrderId())
                .customerId(order.getCustomerNameSnapshot().getId())
                .originalAmount(order.getTotalAmount())
                .refundAmount(request.getRefundAmount())
                .status(Refund.RefundStatus.INITIATED)
                .reason(request.getReason())
                .paymentMethod(request.getPaymentMethod())
                .originalTransactionId(request.getOriginalTransactionId())
                .retryCount(0)
                .build();

        Refund savedRefund = refundRepository.save(refund);
        log.info("Refund initiated with ID: {}, Order ID: {}", savedRefund.getId(), request.getOrderId());

        // Update order status to REFUNDING
        order.setStatus("REFUNDING");
        orderRepository.save(order);

        // Publish REFUND_INITIATED event
        OrderEvent event = OrderEvent.builder()
                .orderId(savedRefund.getId())
                .customerId(savedRefund.getCustomerId())
                .totalAmount(savedRefund.getRefundAmount())
                .status("INITIATED")
                .eventType("REFUND_INITIATED")
                .timestamp(LocalDateTime.now())
                .description("Refund initiated for order: " + request.getOrderId() + 
                           ". Reason: " + request.getReason())
                .build();

        refundProducer.sendRefundInitiatedEvent(event);

        return convertToResponse(savedRefund);
    }

    /**
     * Process refund - called by event consumer
     */
    public void processRefund(Long refundId) {
        log.info("Processing refund with ID: {}", refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        // Update status to PROCESSING
        refund.setStatus(Refund.RefundStatus.PROCESSING);
        refundRepository.save(refund);

        // Publish REFUND_PROCESSING event
        OrderEvent event = OrderEvent.builder()
                .orderId(refund.getId())
                .customerId(refund.getCustomerId())
                .totalAmount(refund.getRefundAmount())
                .status("PROCESSING")
                .eventType("REFUND_PROCESSING")
                .timestamp(LocalDateTime.now())
                .description("Processing refund for order: " + refund.getOrderId())
                .build();

        refundProducer.sendRefundProcessingEvent(event);
    }

    /**
     * Mark refund as completed - called after successful payment gateway refund
     */
    public void completeRefund(Long refundId, String refundTransactionId) {
        log.info("Completing refund with ID: {}, Transaction: {}", refundId, refundTransactionId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        // Update refund status
        refund.setStatus(Refund.RefundStatus.COMPLETED);
        refund.setRefundTransactionId(refundTransactionId);
        refund.setCompletedAt(LocalDateTime.now());
        refund.setEstimatedArrival(LocalDateTime.now().plusDays(3)); // Usually 3-5 business days
        refundRepository.save(refund);

        // Update order status
        Order order = orderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("REFUNDED");
        orderRepository.save(order);

        // Publish REFUND_COMPLETED event
        OrderEvent event = OrderEvent.builder()
                .orderId(refund.getId())
                .customerId(refund.getCustomerId())
                .totalAmount(refund.getRefundAmount())
                .status("COMPLETED")
                .eventType("REFUND_COMPLETED")
                .timestamp(LocalDateTime.now())
                .description("Refund completed. Transaction ID: " + refundTransactionId + 
                           ". Estimated arrival: 3-5 business days")
                .build();

        refundProducer.sendRefundCompletedEvent(event);

        // Publish PAYMENT_REFUNDED event
        OrderEvent paymentEvent = OrderEvent.builder()
                .orderId(order.getId())
                .customerId(refund.getCustomerId())
                .totalAmount(refund.getRefundAmount())
                .status("REFUNDED")
                .eventType("PAYMENT_REFUNDED")
                .timestamp(LocalDateTime.now())
                .description("Payment refunded successfully. Amount: " + refund.getRefundAmount())
                .build();

        refundProducer.sendPaymentRefundedEvent(paymentEvent);
    }

    /**
     * Handle refund failure with retry logic
     */
    public void failRefund(Long refundId, String errorCode, String errorMessage) {
        log.error("Refund failed with ID: {}, Error: {}, Message: {}", refundId, errorCode, errorMessage);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        refund.setErrorCode(errorCode);
        refund.setErrorMessage(errorMessage);
        refund.setRetryCount(refund.getRetryCount() + 1);

        // Check if we should retry
        if (refund.getRetryCount() < 3) {
            log.info("Scheduling retry for refund ID: {}. Retry count: {}", refundId, refund.getRetryCount());
            
            // Schedule next retry (exponential backoff: 1, 2, 4 hours)
            long delayHours = (long) Math.pow(2, refund.getRetryCount() - 1);
            refund.setNextRetryTime(LocalDateTime.now().plusHours(delayHours));
            refund.setStatus(Refund.RefundStatus.FAILED);
        } else {
            // Max retries exceeded - manual intervention needed
            log.error("Max retries exceeded for refund ID: {}. Manual intervention required.", refundId);
            refund.setStatus(Refund.RefundStatus.FAILED);
            // Optionally: send alert to admin
        }

        refundRepository.save(refund);

        // Publish REFUND_FAILED event
        OrderEvent event = OrderEvent.builder()
                .orderId(refund.getId())
                .customerId(refund.getCustomerId())
                .totalAmount(refund.getRefundAmount())
                .status("FAILED")
                .eventType("REFUND_FAILED")
                .timestamp(LocalDateTime.now())
                .description("Refund failed. Error: " + errorMessage + 
                           ". Retry count: " + refund.getRetryCount())
                .build();

        refundProducer.sendRefundFailedEvent(event);
    }

    /**
     * Get refund by ID
     */
    public RefundResponse getRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));
        return convertToResponse(refund);
    }

    /**
     * Get refunds by customer
     */
    public List<RefundResponse> getRefundsByCustomer(Long customerId) {
        return refundRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get refunds by order
     */
    public Optional<RefundResponse> getRefundByOrder(Long orderId) {
        return refundRepository.findByOrderId(orderId)
                .map(this::convertToResponse);
    }

    /**
     * Get failed refunds for retry
     */
    public List<Refund> getFailedRefundsForRetry() {
        return refundRepository.findByStatusAndRetryCountLessThan(Refund.RefundStatus.FAILED, 3);
    }

    /**
     * Cancel refund
     */
    public void cancelRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        if (refund.getStatus() == Refund.RefundStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed refund");
        }

        refund.setStatus(Refund.RefundStatus.CANCELLED);
        refundRepository.save(refund);

        // Update order status back to original
        Order order = orderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("CONFIRMED");
        orderRepository.save(order);

        log.info("Refund cancelled with ID: {}", refundId);
    }

    /**
     * Helper method to convert entity to response DTO
     */
    private RefundResponse convertToResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getOrderId())
                .customerId(refund.getCustomerId())
                .originalAmount(refund.getOriginalAmount())
                .refundAmount(refund.getRefundAmount())
                .status(refund.getStatus().toString())
                .reason(refund.getReason())
                .paymentMethod(refund.getPaymentMethod())
                .refundTransactionId(refund.getRefundTransactionId())
                .createdAt(refund.getCreatedAt())
                .completedAt(refund.getCompletedAt())
                .estimatedArrival(refund.getEstimatedArrival())
                .message(generateMessage(refund))
                .build();
    }

    /**
     * Generate user-friendly message based on refund status
     */
    private String generateMessage(Refund refund) {
        switch (refund.getStatus()) {
            case INITIATED:
                return "Refund request received. Processing will start shortly.";
            case PROCESSING:
                return "Your refund is being processed. Please wait.";
            case COMPLETED:
                String arrivalDate = refund.getEstimatedArrival() != null 
                    ? refund.getEstimatedArrival().toLocalDate().toString() 
                    : "3-5 business days";
                return "Refund completed! Expected to arrive by: " + arrivalDate;
            case FAILED:
                return "Refund processing failed: " + refund.getErrorMessage() + 
                       ". Retry count: " + refund.getRetryCount();
            case CANCELLED:
                return "Refund has been cancelled.";
            default:
                return "Refund status: " + refund.getStatus();
        }
    }
}
