package com.prueba_cuscatlan.shopping_Car_miguel.repository;

import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(String userId);
}
