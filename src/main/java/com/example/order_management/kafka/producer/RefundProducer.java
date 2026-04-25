package com.example.order_management.kafka.producer;

import com.example.order_management.kafka.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefundProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRefundInitiatedEvent(OrderEvent event) {
        log.info("Publishing refund initiated event for refundId: {}, orderId: {}", 
                event.getOrderId(), event.getOrderId());
        sendMessage("refund-initiated", String.valueOf(event.getOrderId()), event);
    }

    public void sendRefundProcessingEvent(OrderEvent event) {
        log.info("Publishing refund processing event for refundId: {}", event.getOrderId());
        sendMessage("refund-processing", String.valueOf(event.getOrderId()), event);
    }

    public void sendRefundCompletedEvent(OrderEvent event) {
        log.info("Publishing refund completed event for refundId: {}", event.getOrderId());
        sendMessage("refund-completed", String.valueOf(event.getOrderId()), event);
    }

    public void sendRefundFailedEvent(OrderEvent event) {
        log.info("Publishing refund failed event for refundId: {}", event.getOrderId());
        sendMessage("refund-failed", String.valueOf(event.getOrderId()), event);
    }

    public void sendPaymentRefundedEvent(OrderEvent event) {
        log.info("Publishing payment refunded event for refundId: {}", event.getOrderId());
        sendMessage("payment-refunded", String.valueOf(event.getOrderId()), event);
    }

    private void sendMessage(String topic, String key, OrderEvent event) {
        try {
            Message<OrderEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .build();
            
            kafkaTemplate.send(message);
            log.info("Refund message sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Error sending refund message to topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Failed to send refund message to Kafka", e);
        }
    }
}
