package com.example.order_management.controller;

import com.example.order_management.dto.CategoryRequest;
import com.example.order_management.entity.Category;
import com.example.order_management.service.CategoryService;
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
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "API untuk mengelola kategori produk")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Dapatkan semua kategori", description = "Mengambil daftar semua kategori produk")
    @ApiResponse(responseCode = "200", description = "Daftar kategori berhasil diambil")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Dapatkan kategori berdasarkan ID", description = "Mengambil detail kategori spesifik")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kategori ditemukan"),
            @ApiResponse(responseCode = "404", description = "Kategori tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@Parameter(description = "ID Kategori") @PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buat kategori baru", description = "Membuat kategori produk baru")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Kategori berhasil dibuat"),
            @ApiResponse(responseCode = "400", description = "Data tidak valid")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Category> createCategory(@Parameter(description = "Data kategori") @RequestBody @Valid CategoryRequest request) {
        Category category = categoryService.createCategory(request);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @Operation(summary = "Update kategori", description = "Memperbarui data kategori")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kategori berhasil diperbarui"),
            @ApiResponse(responseCode = "404", description = "Kategori tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@Parameter(description = "ID Kategori") @PathVariable Long id, 
                                                   @Parameter(description = "Data kategori") @RequestBody @Valid CategoryRequest request) {
        try {
            Category category = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Hapus kategori", description = "Menghapus kategori produk")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Kategori berhasil dihapus"),
            @ApiResponse(responseCode = "404", description = "Kategori tidak ditemukan")
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@Parameter(description = "ID Kategori") @PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
