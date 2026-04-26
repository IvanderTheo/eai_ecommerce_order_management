package com.example.order_management.controller;

import com.example.order_management.dto.OrderItemRequest;
import com.example.order_management.dto.OrderResponse;
import com.example.order_management.dto.OrderItemResponse;
import com.example.order_management.entity.Order;
import com.example.order_management.service.OrderService;
import com.example.order_management.kafka.producer.OrderProducer;
import com.example.order_management.kafka.event.OrderEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "API untuk mengelola pesanan dengan Kafka event streaming")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

        @Autowired
        private OrderService orderService;

        @Autowired
        private OrderProducer orderProducer;

        @Operation(summary = "Buat pesanan baru", description = "Membuat pesanan baru dan mengirim event ke Kafka")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Pesanan berhasil dibuat",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                @ApiResponse(responseCode = "400", description = "Data tidak valid"),
                @ApiResponse(responseCode = "401", description = "Tidak terautentikasi"),
                @ApiResponse(responseCode = "404", description = "Customer atau product tidak ditemukan")
        })
        @PreAuthorize("isAuthenticated()")
                @PostMapping
                public ResponseEntity<OrderResponse> createOrder(
                @RequestParam Long customerId, 
                @RequestBody List<OrderItemRequest> items) {
                
                // Pastikan list items tidak kosong
                if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().build();
                }

                Order order = orderService.createOrder(customerId, items);

                // Langsung kirim event Kafka di sini atau di Service (disarankan di Service agar atomik)
                return new ResponseEntity<>(convertToDto(order), HttpStatus.CREATED);
        }

        @Operation(summary = "Dapatkan semua pesanan", description = "Mengambil daftar semua pesanan")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Daftar pesanan berhasil diambil"),
                @ApiResponse(responseCode = "401", description = "Tidak terautentikasi")
        })
        @PreAuthorize("isAuthenticated()")
        @GetMapping
        public ResponseEntity<List<OrderResponse>> getAllOrders() {
                List<OrderResponse> orders = orderService.getAllOrders().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(orders);
        }

        @Operation(summary = "Dapatkan pesanan berdasarkan ID", description = "Mengambil detail pesanan spesifik")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Pesanan ditemukan"),
                @ApiResponse(responseCode = "401", description = "Tidak terautentikasi"),
                @ApiResponse(responseCode = "404", description = "Pesanan tidak ditemukan")
        })
        @PreAuthorize("isAuthenticated()")
        @GetMapping("/{id}")
        public ResponseEntity<OrderResponse> getOrderById(@Parameter(description = "ID Pesanan", required = true)
                                                @PathVariable Long id) {
                Optional<Order> order = orderService.getOrderById(id);
                return order.map(value -> ResponseEntity.ok(convertToDto(value)))
                        .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @Operation(summary = "Update status pesanan", description = "Memperbarui status pesanan dan mengirim event ke Kafka")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Status pesanan berhasil diperbarui"),
                @ApiResponse(responseCode = "401", description = "Tidak terautentikasi"),
                @ApiResponse(responseCode = "404", description = "Pesanan tidak ditemukan")
        })
        @PreAuthorize("isAuthenticated()")
        @PutMapping("/{id}/status")
        public ResponseEntity<OrderResponse> updateStatus(
                @PathVariable Long id,
                @RequestParam String status) {

        Order order = orderService.updateStatus(id, status);

        OrderEvent event = OrderEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId()) // 🔥 FIX
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .eventType("ORDER_STATUS_UPDATED")
                .timestamp(LocalDateTime.now())
                .description("Status updated")
                .build();

        orderProducer.sendOrderStatusUpdatedEvent(event);

        return ResponseEntity.ok(convertToDto(order));
        }

    @Operation(summary = "Batalkan pesanan", description = "Membatalkan pesanan dan mengirim event ke Kafka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pesanan berhasil dibatalkan"),
            @ApiResponse(responseCode = "400", description = "Pesanan tidak dapat dibatalkan"),
            @ApiResponse(responseCode = "401", description = "Tidak terautentikasi"),
            @ApiResponse(responseCode = "404", description = "Pesanan tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@Parameter(description = "ID Pesanan", required = true)
                                              @PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            
            OrderEvent event = OrderEvent.builder()
                    .orderId(id)
                    .eventType("ORDER_CANCELLED")
                    .timestamp(LocalDateTime.now())
                    .description("Pesanan telah dibatalkan oleh user")
                    .build();
            orderProducer.sendOrderCancelledEvent(event);
            
            return ResponseEntity.ok("Order berhasil dibatalkan");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

        private OrderResponse convertToDto(Order order) {
        // Pastikan order.getItems() tidak null untuk menghindari NPE
        List<OrderItemResponse> itemResponses = order.getItems() == null ? new ArrayList<>() : 
                order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerNameSnapshot())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(itemResponses) 
                .build();
        }
}