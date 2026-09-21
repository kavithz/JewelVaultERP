package com.jewelvaulterp.product.repository;

import com.jewelvaulterp.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    long countByActiveTrue();

    long countByActiveFalse();
}