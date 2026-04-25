package com.example.order_management.controller;

import com.example.order_management.dto.ProductRequest;
import com.example.order_management.entity.Product;
import com.example.order_management.service.ProductService;
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
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "API untuk mengelola produk")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Dapatkan semua produk", description = "Mengambil daftar semua produk dengan filter kategori")
    @ApiResponse(responseCode = "200", description = "Daftar produk berhasil diambil")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(summary = "Dapatkan produk berdasarkan ID", description = "Mengambil detail produk spesifik")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produk ditemukan"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@Parameter(description = "ID Produk") @PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buat produk baru", description = "Membuat produk baru dengan kategori")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produk berhasil dibuat"),
            @ApiResponse(responseCode = "400", description = "Data tidak valid"),
            @ApiResponse(responseCode = "404", description = "Kategori tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Product> createProduct(@Parameter(description = "Data produk") @RequestBody @Valid ProductRequest request) {
        Product product = productService.createProduct(request);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @Operation(summary = "Update produk", description = "Memperbarui data produk")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produk berhasil diperbarui"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@Parameter(description = "ID Produk") @PathVariable Long id, 
                                                 @Parameter(description = "Data produk") @RequestBody @Valid ProductRequest request) {
        try {
            Product product = productService.updateProduct(id, request);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Hapus produk", description = "Menghapus produk dari sistem")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produk berhasil dihapus"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "ID Produk") @PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
