package com.example.order_management.controller;

import com.example.order_management.dto.CustomerRequest;
import com.example.order_management.entity.Customer;
import com.example.order_management.service.CustomerService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "API untuk mengelola pelanggan")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Operation(summary = "Dapatkan semua pelanggan", description = "Mengambil daftar semua pelanggan")
    @ApiResponse(responseCode = "200", description = "Daftar pelanggan berhasil diambil")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @Operation(summary = "Dapatkan pelanggan berdasarkan ID", description = "Mengambil detail pelanggan spesifik")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pelanggan ditemukan"),
            @ApiResponse(responseCode = "404", description = "Pelanggan tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@Parameter(description = "ID Pelanggan") @PathVariable Long id) {
        Optional<Customer> customer = customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buat pelanggan baru", description = "Membuat profil pelanggan baru")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pelanggan berhasil dibuat"),
            @ApiResponse(responseCode = "400", description = "Data tidak valid")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Parameter(description = "Data pelanggan") @RequestBody @Valid CustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        return new ResponseEntity<>(customer, HttpStatus.CREATED);
    }

    @Operation(summary = "Update pelanggan", description = "Memperbarui data pelanggan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pelanggan berhasil diperbarui"),
            @ApiResponse(responseCode = "404", description = "Pelanggan tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@Parameter(description = "ID Pelanggan") @PathVariable Long id, 
                                                   @Parameter(description = "Data pelanggan") @RequestBody @Valid CustomerRequest request) {
        try {
            Customer customer = customerService.updateCustomer(id, request);
            return ResponseEntity.ok(customer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Hapus pelanggan", description = "Menghapus profil pelanggan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pelanggan berhasil dihapus"),
            @ApiResponse(responseCode = "404", description = "Pelanggan tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@Parameter(description = "ID Pelanggan") @PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
