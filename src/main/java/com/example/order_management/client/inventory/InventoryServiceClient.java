package com.example.order_management.client.inventory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client untuk mengakses Inventory Management Service
 * Base URL: http://localhost:8086
 */
@FeignClient(name = "inventory-service", url = "http://localhost:8086")
public interface InventoryServiceClient {
    
    /**
     * Dapatkan semua stocks di inventory
     * @return List of stocks
     */
    @GetMapping("/api/stocks")
    List<StockResponse> getAllStocks();
    
    /**
     * Dapatkan stock berdasarkan product ID
     * @param productId ID produk
     * @return Stock untuk product tersebut
     */
    @GetMapping("/api/stocks/product/{productId}")
    StockResponse getStockByProductId(@PathVariable Long productId);
    
    /**
     * Cek apakah ada stock yang cukup untuk product
     * @param productId ID produk
     * @param quantity Jumlah yang dibutuhkan
     * @return true jika ada stock yang cukup
     */
    @GetMapping("/api/stocks/product/{productId}/check/{quantity}")
    Boolean checkSufficientStock(@PathVariable Long productId, @PathVariable Integer quantity);
    
    /**
     * Dapatkan stock berdasarkan ID
     * @param stockId ID stock
     * @return Detail stock
     */
    @GetMapping("/api/stocks/{id}")
    StockResponse getStockById(@PathVariable Long id);
}
