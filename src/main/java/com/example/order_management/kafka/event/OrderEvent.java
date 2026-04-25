package com.example.order_management.kafka.event;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private Long orderId;
    private Long customerId;
    private Double totalAmount;
    private String status;
    private String eventType;
    private LocalDateTime timestamp;
    private String description;
}