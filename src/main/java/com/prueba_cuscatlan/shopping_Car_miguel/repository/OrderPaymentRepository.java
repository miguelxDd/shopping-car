package com.prueba_cuscatlan.shopping_Car_miguel.repository;

import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    Optional<OrderPayment> findByOrderId(Long orderId);

    Optional<OrderPayment> findByIdempotencyKey(String idempotencyKey);
}
