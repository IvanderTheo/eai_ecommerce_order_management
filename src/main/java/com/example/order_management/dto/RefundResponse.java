package com.example.order_management.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {

    private Long id;

    private Long orderId;

    private Long customerId;

    private Double originalAmount;

    private Double refundAmount;

    private String status;

    private String reason;

    private String paymentMethod;

    private String refundTransactionId;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    private LocalDateTime estimatedArrival;

    private String message;
}
