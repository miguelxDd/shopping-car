package com.prueba_cuscatlan.shopping_Car_miguel.repository;

import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
