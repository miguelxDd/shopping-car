package com.prueba_cuscatlan.shopping_Car_miguel.repository;

import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);
}
