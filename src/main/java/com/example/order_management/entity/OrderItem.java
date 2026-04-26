package com.example.order_management.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String productNameSnapshot;

    private Integer quantity;

    private Double price;

    private Double subtotal;

    @ManyToOne(fetch = FetchType.LAZY) // Gunakan LAZY untuk performa
    @JoinColumn(name = "order_id")
    @JsonBackReference // Penting agar tidak terjadi infinite loop saat return JSON
    private Order order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getProductId() { return productId; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
}
