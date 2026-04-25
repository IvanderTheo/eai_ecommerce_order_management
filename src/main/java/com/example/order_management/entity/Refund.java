package com.example.order_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Double originalAmount;

    @Column(nullable = false)
    private Double refundAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus status; // INITIATED, PROCESSING, COMPLETED, FAILED

    @Column(length = 500)
    private String reason;

    @Column(length = 50)
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, etc

    @Column(length = 100)
    private String originalTransactionId; // Payment gateway transaction ID

    @Column(length = 100)
    private String refundTransactionId; // Refund transaction ID from payment gateway

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Integer retryCount = 0;

    private LocalDateTime nextRetryTime;

    @Column(length = 500)
    private String errorMessage;

    @Column(length = 100)
    private String errorCode;

    private LocalDateTime estimatedArrival; // Estimated refund arrival date

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RefundStatus {
        INITIATED,      // Refund dimulai
        PROCESSING,     // Refund sedang diproses
        COMPLETED,      // Refund berhasil
        FAILED,         // Refund gagal
        CANCELLED       // Refund dibatalkan
    }
}
