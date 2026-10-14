package com.vasav.ecom.catalogservice.service;

import com.vasav.ecom.catalogservice.dto.ProductRequest;
import com.vasav.ecom.catalogservice.dto.ProductResponse;
import com.vasav.ecom.catalogservice.entity.Product;
import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;
import com.vasav.ecom.catalogservice.producer.ProductEventProducer;
import com.vasav.ecom.catalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEvents;

    @Value("${images.url-prefix}")
    private String imageUrlPrefix;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        checkImageUrl(request.imageUrl());
        Product product = new Product();
        product.setId(UUID.randomUUID());
        request.applyTo(product);
        product = productRepository.saveAndFlush(product);
        productEvents.upserted(product);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        checkImageUrl(request.imageUrl());
        Product product = find(id);
        request.applyTo(product);
        product = productRepository.saveAndFlush(product);
        productEvents.upserted(product);
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getAny(UUID id) {
        return ProductResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getActive(UUID id) {
        return productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE).map(ProductResponse::from)
                .orElseThrow(ProductService::notFound);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listActive(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> lookup(Collection<UUID> ids) {
        return productRepository.findAllById(ids).stream().map(ProductResponse::from).toList();
    }

    private void checkImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.startsWith(imageUrlPrefix)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageUrl must start with " + imageUrlPrefix);
        }
    }

    private Product find(UUID id) {
        return productRepository.findById(id).orElseThrow(ProductService::notFound);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }
}
