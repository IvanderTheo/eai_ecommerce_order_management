package com.example.order_management.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service", url = "http://localhost:8086")
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable Long id);
}
