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
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderEvent event) {
        log.info("Publishing order created event for orderId: {}", event.getOrderId());
        sendMessage("order-created", String.valueOf(event.getOrderId()), event);
    }

    public void sendOrderStatusUpdatedEvent(OrderEvent event) {
        log.info("Publishing order status updated event for orderId: {}", event.getOrderId());
        sendMessage("order-status-updated", String.valueOf(event.getOrderId()), event);
    }

    public void sendOrderCancelledEvent(OrderEvent event) {
        log.info("Publishing order cancelled event for orderId: {}", event.getOrderId());
        sendMessage("order-cancelled", String.valueOf(event.getOrderId()), event);
    }

    private void sendMessage(String topic, String key, OrderEvent event) {
        try {
            Message<OrderEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .build();
            
            kafkaTemplate.send(message);
            log.info("Message sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Error sending message to topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Failed to send message to Kafka", e);
        }
    }
}
