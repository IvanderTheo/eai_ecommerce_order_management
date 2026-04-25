package com.example.order_management.controller;

import com.example.order_management.dto.RefundRequest;
import com.example.order_management.dto.RefundResponse;
import com.example.order_management.service.RefundService;
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

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@Tag(name = "Refund Management", description = "API untuk mengelola refund dengan Kafka event streaming")
@SecurityRequirement(name = "Bearer Authentication")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @Operation(summary = "Buat request refund baru", 
               description = "Membuat request refund untuk order tertentu dan mengirim event ke Kafka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Refund request berhasil dibuat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefundResponse.class))),
            @ApiResponse(responseCode = "400", description = "Data tidak valid atau order tidak eligible untuk refund"),
            @ApiResponse(responseCode = "401", description = "Tidak terautentikasi"),
            @ApiResponse(responseCode = "404", description = "Order tidak ditemukan"),
            @ApiResponse(responseCode = "409", description = "Refund sudah ada untuk order ini")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<RefundResponse> requestRefund(@Parameter(description = "Data refund request", required = true)
                                                        @Valid @RequestBody RefundRequest request) {
        try {
            RefundResponse response = refundService.requestRefund(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Return appropriate error based on message
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(RefundResponse.builder()
                                .message(e.getMessage())
                                .build());
            } else if (e.getMessage().contains("expired") || e.getMessage().contains("status") || 
                      e.getMessage().contains("Invalid amount")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(RefundResponse.builder()
                                .message(e.getMessage())
                                .build());
            } else if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(RefundResponse.builder()
                                .message(e.getMessage())
                                .build());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RefundResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "Dapatkan refund berdasarkan ID", 
               description = "Mengambil detail refund spesifik")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund ditemukan",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefundResponse.class))),
            @ApiResponse(responseCode = "401", description = "Tidak terautentikasi"),
            @ApiResponse(responseCode = "404", description = "Refund tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponse> getRefund(@Parameter(description = "ID Refund", required = true)
                                                     @PathVariable Long refundId) {
        try {
            RefundResponse response = refundService.getRefund(refundId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Dapatkan refund berdasarkan Order ID", 
               description = "Mengambil detail refund untuk order tertentu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund ditemukan"),
            @ApiResponse(responseCode = "401", description = "Tidak terautentikasi"),
            @ApiResponse(responseCode = "404", description = "Refund tidak ditemukan untuk order ini")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<RefundResponse> getRefundByOrder(@Parameter(description = "ID Order", required = true)
                                                           @PathVariable Long orderId) {
        return refundService.getRefundByOrder(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Dapatkan semua refund pelanggan", 
               description = "Mengambil daftar semua refund untuk customer tertentu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daftar refund berhasil diambil",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefundResponse.class))),
            @ApiResponse(responseCode = "401", description = "Tidak terautentikasi")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RefundResponse>> getRefundsByCustomer(@Parameter(description = "ID Customer", required = true)
                                                                     @PathVariable Long customerId) {
        List<RefundResponse> refunds = refundService.getRefundsByCustomer(customerId);
        return ResponseEntity.ok(refunds);
    }

    @Operation(summary = "Batalkan refund", 
               description = "Membatalkan refund yang masih dalam status INITIATED atau PROCESSING")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund berhasil dibatalkan"),
            @ApiResponse(responseCode = "400", description = "Refund tidak dapat dibatalkan"),
            @ApiResponse(responseCode = "401", description = "Tidak terautentikasi"),
            @ApiResponse(responseCode = "404", description = "Refund tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{refundId}/cancel")
    public ResponseEntity<String> cancelRefund(@Parameter(description = "ID Refund", required = true)
                                               @PathVariable Long refundId) {
        try {
            refundService.cancelRefund(refundId);
            return ResponseEntity.ok("Refund berhasil dibatalkan");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Refund tidak ditemukan");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
