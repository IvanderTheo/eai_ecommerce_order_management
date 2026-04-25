package com.example.order_management.kafka.consumer;

import com.example.order_management.kafka.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumer {

    @KafkaListener(topics = "order-created", groupId = "order-management-group")
    public void consumeOrderCreatedEvent(OrderEvent event) {
        log.info("Received order-created event: orderId={}, customerId={}, amount={}",
                event.getOrderId(), event.getCustomerId(), event.getTotalAmount());
        
        // Process order created event
        // Example: Send notification, update inventory, etc.
        handleOrderCreated(event);
    }

    @KafkaListener(topics = "order-status-updated", groupId = "order-management-group")
    public void consumeOrderStatusUpdatedEvent(OrderEvent event) {
        log.info("Received order-status-updated event: orderId={}, status={}, timestamp={}",
                event.getOrderId(), event.getStatus(), event.getTimestamp());
        
        // Process order status updated event
        // Example: Send notification to customer, update dashboard, etc.
        handleOrderStatusUpdate(event);
    }

    @KafkaListener(topics = "order-cancelled", groupId = "order-management-group")
    public void consumeOrderCancelledEvent(OrderEvent event) {
        log.info("Received order-cancelled event: orderId={}, description={}",
                event.getOrderId(), event.getDescription());
        
        // Process order cancelled event
        // Example: Release inventory, refund, send notification, etc.
        handleOrderCancelled(event);
    }

    private void handleOrderCreated(OrderEvent event) {
        // Implement business logic for order creation
        log.debug("Processing order creation: {}", event);
    }

    private void handleOrderStatusUpdate(OrderEvent event) {
        // Implement business logic for order status update
        log.debug("Processing order status update: {}", event);
    }

    private void handleOrderCancelled(OrderEvent event) {
        // Implement business logic for order cancellation
        log.debug("Processing order cancellation: {}", event);
    }
}
