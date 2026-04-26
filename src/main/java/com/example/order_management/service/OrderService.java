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
        log.info("Creating order for customer: {} with {} items", customerId, items.size());

        // ✅ VALIDATE CUSTOMER dari User Management
        UserResponse user = null;
        try {
            user = userServiceClient.getUserById(customerId);
            if (user == null) {
                throw new RuntimeException("Customer tidak ditemukan dengan ID: " + customerId);
            }
            log.info("Customer validated: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error validating customer: {}", e.getMessage());
            throw new RuntimeException("Gagal validasi customer: " + e.getMessage(), e);
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(customerId);
        order.setCustomerNameSnapshot(user.getUsername());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        double total = 0;

        // ✅ VALIDATE SETIAP ITEM dari Inventory Management
        for (OrderItemRequest req : items) {
            log.debug("Validating product {} with quantity {}", req.getProductId(), req.getQuantity());

            // Get product info
            ProductResponse product = null;
            try {
                product = productServiceClient.getProductById(req.getProductId());
                if (product == null) {
                    throw new RuntimeException("Product tidak ditemukan: " + req.getProductId());
                }
            } catch (Exception e) {
                log.error("Error getting product: {}", e.getMessage());
                throw new RuntimeException("Gagal mendapatkan data product: " + e.getMessage(), e);
            }

            // ✅ CHECK STOCK DARI INVENTORY MANAGEMENT
            try {
                Boolean hasSufficientStock = inventoryServiceClient.checkSufficientStock(
                        req.getProductId(),
                        req.getQuantity()
                );
                
                if (!hasSufficientStock) {
                    log.warn("Insufficient stock for product {}: required {}", 
                            req.getProductId(), req.getQuantity());
                    throw new RuntimeException(
                            "Stok tidak cukup untuk product " + req.getProductId() +
                            ". Dibutuhkan: " + req.getQuantity()
                    );
                }
                log.info("Stock validated for product {}", req.getProductId());
            } catch (Exception e) {
                log.error("Error checking stock: {}", e.getMessage());
                throw new RuntimeException("Gagal validasi stok: " + e.getMessage(), e);
            }

            // Create OrderItem
            OrderItem item = new OrderItem();
            item.setProductId(req.getProductId());
            item.setQuantity(req.getQuantity());
            item.setPrice(Double.valueOf(product.getPrice())); // ambil dari product-service
            item.setSubtotal(product.getPrice() * req.getQuantity());

            total += item.getSubtotal();
            order.addItem(item);
            
            log.debug("Item added: product {}, quantity {}, price {}", 
                    req.getProductId(), req.getQuantity(), product.getPrice());
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {} with total: {}", savedOrder.getId(), total);
        
        return savedOrder;
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