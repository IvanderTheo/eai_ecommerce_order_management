package com.example.order_management.repository;

import com.example.order_management.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByOrderId(Long orderId);

    List<Refund> findByCustomerId(Long customerId);

    List<Refund> findByStatus(Refund.RefundStatus status);

    List<Refund> findByCustomerIdAndStatus(Long customerId, Refund.RefundStatus status);

    List<Refund> findByStatusAndRetryCountLessThan(Refund.RefundStatus status, Integer maxRetries);
}
