package com.example.order_management.client.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer minimalStock;
    private Integer maximalStock;

    @JsonProperty("availableQuantity")
    public Integer getAvailableQuantity() {
        return quantity != null && reservedQuantity != null ? 
               quantity - reservedQuantity : 0;
    }
}
