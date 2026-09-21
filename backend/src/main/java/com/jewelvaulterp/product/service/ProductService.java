package com.jewelvaulterp.product.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.product.dto.CreateProductRequest;
import com.jewelvaulterp.product.dto.ProductResponse;
import com.jewelvaulterp.product.dto.UpdateProductRequest;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    public ProductService(
            ProductRepository productRepository,
            CompanyRepository companyRepository
    ) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse createProduct(CreateProductRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (productRepository.findBySku(request.sku()).isPresent()) {
            throw new IllegalArgumentException("SKU already exists");
        }

        LocalDateTime now = LocalDateTime.now();

        Product product = new Product(
                UUID.randomUUID(),
                company,
                request.sku(),
                request.name(),
                request.jewelleryType(),
                request.metalType(),
                request.purity(),
                request.grossWeight(),
                request.netWeight(),
                request.makingCharge(),
                true,
                now,
                now
        );

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(
            UUID id,
            UpdateProductRequest request
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        productRepository.findBySku(request.sku())
                .filter(existingProduct -> !existingProduct.getId().equals(id))
                .ifPresent(existingProduct -> {
                    throw new IllegalArgumentException("SKU already exists");
                });

        product.update(
                request.sku(),
                request.name(),
                request.jewelleryType(),
                request.metalType(),
                request.purity(),
                request.grossWeight(),
                request.netWeight(),
                request.makingCharge()
        );

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateStatus(
            UUID id,
            boolean active
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.setActive(active);

        return toResponse(productRepository.save(product));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCompany().getId(),
                product.getSku(),
                product.getName(),
                product.getJewelleryType(),
                product.getMetalType(),
                product.getPurity(),
                product.getGrossWeight(),
                product.getNetWeight(),
                product.getMakingCharge(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}