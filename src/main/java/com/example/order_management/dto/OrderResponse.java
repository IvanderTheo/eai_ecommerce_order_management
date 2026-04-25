package com.example.order_management.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String status;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}