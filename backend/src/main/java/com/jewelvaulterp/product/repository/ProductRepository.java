package com.jewelvaulterp.product.repository;

import com.jewelvaulterp.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Product> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    Optional<Product> findBySku(String sku);

    long countByActiveTrue();

    long countByActiveFalse();
}