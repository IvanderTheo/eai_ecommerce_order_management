package com.example.order_management.service;

import com.example.order_management.client.user.UserResponse;
import com.example.order_management.client.user.UserServiceClient;
import com.example.order_management.client.product.ProductResponse;
import com.example.order_management.client.product.ProductServiceClient;
import com.example.order_management.dto.OrderItemRequest;
import com.example.order_management.entity.Order;
import com.example.order_management.entity.OrderItem;
import com.example.order_management.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    public Order createOrder(Long customerId, List<OrderItemRequest> items) {

        // 🔥 VALIDATE USER
        UserResponse user = userServiceClient.getUserById(customerId);

        if (user == null) {
            throw new RuntimeException("Customer tidak ditemukan");
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(customerId);
        order.setCustomerNameSnapshot(user.getUsername());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        double total = 0;

        // 🔥 LOOP ITEMS (MICROSERVICE CORRECT WAY)
        for (OrderItemRequest req : items) {

            ProductResponse product = productServiceClient.getProductById(req.getProductId());

            if (product == null) {
                throw new RuntimeException("Product tidak ditemukan: " + req.getProductId());
            }

            OrderItem item = new OrderItem();
            item.setProductId(req.getProductId()); // ✔ FIXED
            item.setQuantity(req.getQuantity());
            item.setPrice(product.getPrice()); // ambil dari product-service
            item.setSubtotal(product.getPrice() * req.getQuantity());

            total += item.getSubtotal();
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
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));

        if ("CANCELLED".equals(order.getStatus()) || "SHIPPED".equals(order.getStatus())) {
            throw new RuntimeException("Order tidak dapat dibatalkan");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}