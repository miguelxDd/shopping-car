package com.prueba_cuscatlan.shopping_Car_miguel.repository;

import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
