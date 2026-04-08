package com.prueba_cuscatlan.shopping_Car_miguel.repository;

import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(String userId);

    @EntityGraph(attributePaths = { "details", "payment" })
    Page<Order> findByUserId(String userId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "details", "payment" })
    Page<Order> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "details", "payment" })
    Optional<Order> findById(Long id);
}
