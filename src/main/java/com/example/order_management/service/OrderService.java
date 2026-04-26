package com.example.order_management.service;

import com.example.order_management.client.user.UserResponse;
import com.example.order_management.client.user.UserServiceClient;
import com.example.order_management.client.product.ProductResponse;
import com.example.order_management.client.product.ProductServiceClient;
import com.example.order_management.client.inventory.InventoryServiceClient;
import com.example.order_management.dto.OrderItemRequest;
import com.example.order_management.entity.Order;
import com.example.order_management.entity.OrderItem;
import com.example.order_management.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    /**
     * Create order dengan validasi dari User Management dan Inventory Management
     */
    @Transactional
    public Order createOrder(Long customerId, List<OrderItemRequest> items) {
        log.info("Creating order for customer: {}", customerId);

        // 1. Validasi Customer
        UserResponse user = userServiceClient.getUserById(customerId);
        if (user == null) throw new RuntimeException("Customer not found");

        Order order = new Order();
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setCustomerId(customerId);
        order.setCustomerNameSnapshot(user.getUsername());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        
        // Inisialisasi list items agar tidak NullPointerException
        order.setItems(new ArrayList<>()); 

        double total = 0;

        for (OrderItemRequest req : items) {
            // 2. Cek Stock dulu sebelum ambil detail produk (Hemat Resource)
            Boolean hasStock = inventoryServiceClient.checkSufficientStock(req.getProductId(), req.getQuantity());
            if (Boolean.FALSE.equals(hasStock)) {
                throw new RuntimeException("Stok tidak cukup untuk produk ID: " + req.getProductId());
            }

            // 3. Ambil Detail Produk
            ProductResponse product = productServiceClient.getProductById(req.getProductId());
            
            OrderItem item = new OrderItem();
            item.setProductId(req.getProductId());
            item.setProductNameSnapshot(product.getName()); // Simpan nama saat ini
            item.setQuantity(req.getQuantity());
            item.setPrice(product.getPrice());
            item.setSubtotal(product.getPrice() * req.getQuantity());
            
            total += item.getSubtotal();
            
            // Menggunakan helper method untuk set order_id secara otomatis
            order.addItem(item); 
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order updateStatus(Long orderId, String status) {
        log.info("Updating order {} status to {}", orderId, status);
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    Order updated = orderRepository.save(order);
                    log.info("Order {} status updated to {}", orderId, status);
                    return updated;
                })
                .orElseThrow(() -> {
                    log.error("Order tidak ditemukan: {}", orderId);
                    return new RuntimeException("Order tidak ditemukan");
                });
    }

    public void cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order tidak ditemukan: {}", orderId);
                    return new RuntimeException("Order tidak ditemukan");
                });

        if ("CANCELLED".equals(order.getStatus()) || "SHIPPED".equals(order.getStatus())) {
            log.warn("Order {} cannot be cancelled. Current status: {}", orderId, order.getStatus());
            throw new RuntimeException("Order tidak dapat dibatalkan. Status: " + order.getStatus());
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
        log.info("Order {} cancelled successfully", orderId);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}