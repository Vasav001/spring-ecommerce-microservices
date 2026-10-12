package com.vasav.ecom.catalogservice.service.impl;

import com.vasav.ecom.catalogservice.dto.product.AdminProductResponse;
import com.vasav.ecom.catalogservice.dto.product.ProductRequest;
import com.vasav.ecom.catalogservice.entity.OutboxEvent;
import com.vasav.ecom.catalogservice.entity.Product;
import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;
import com.vasav.ecom.catalogservice.events.KafkaTopics;
import com.vasav.ecom.catalogservice.events.ProductUpserted;
import com.vasav.ecom.catalogservice.repository.OutboxEventRepository;
import com.vasav.ecom.catalogservice.repository.ProductRepository;
import com.vasav.ecom.catalogservice.service.ProductService;
import com.vasav.ecom.catalogservice.util.image.ImageRefValidator;
import com.vasav.ecom.catalogservice.util.image.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private static final String PRODUCT_UPSERTED = "ProductUpserted";

    private final ProductRepository products;
    private final OutboxEventRepository outbox;
    private final ImageRefValidator imageRefValidator;
    private final ImageUrlResolver imageUrlResolver;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AdminProductResponse create(UUID productId, ProductRequest request) {
        if (products.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Product already exists");
        }
        imageRefValidator.validate(productId, request.imageRefs());

        Product product = new Product();
        product.setId(productId);
        apply(product, request);
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.DRAFT);
        }

        Product saved = saveAndFlush(product);
        publish(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AdminProductResponse update(UUID productId, long expectedVersion, ProductRequest request) {
        Product product = findOrThrow(productId);
        if (product.getVersion() != expectedVersion) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Product was modified, reload it");
        }
        imageRefValidator.validate(productId, request.imageRefs());

        apply(product, request);
        Product saved = saveAndFlush(product);
        publish(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductResponse get(UUID productId) {
        return toResponse(findOrThrow(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProductResponse> getAll(ProductStatus status, Pageable pageable) {
        Page<Product> page = status == null
                ? products.findAll(pageable)
                : products.findByStatus(status, pageable);
        return page.map(this::toResponse);
    }

    private Product findOrThrow(UUID productId) {
        return products.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void apply(Product product, ProductRequest request) {
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setBrand(request.brand());
        product.setCategory(request.category());
        product.setPrice(request.price());
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        product.setImageRefs(new ArrayList<>(request.imageRefs()));
    }

    private Product saveAndFlush(Product product) {
        try {
            return products.saveAndFlush(product);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already used by an active product");
        }
    }

    private void publish(Product product) {
        OutboxEvent event = new OutboxEvent();
        event.setTopic(KafkaTopics.CATALOG_PRODUCTS);
        event.setAggregateId(product.getId().toString());
        event.setEventType(PRODUCT_UPSERTED);
        event.setCorrelationId(MDC.get("correlationId"));
        event.setPayload(objectMapper.writeValueAsString(ProductUpserted.from(product)));
        outbox.save(event);
    }

    private AdminProductResponse toResponse(Product product) {
        return AdminProductResponse.from(product, imageUrlResolver);
    }
}