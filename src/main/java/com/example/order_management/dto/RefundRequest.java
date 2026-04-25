package com.example.order_management.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    private Long orderId;

    private Double refundAmount;

    @NonNull
    private String reason;

    private String paymentMethod;

    private String originalTransactionId;
}
