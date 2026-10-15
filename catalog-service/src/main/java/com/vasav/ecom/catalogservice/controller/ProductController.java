package com.vasav.ecom.catalogservice.controller;

import com.vasav.ecom.catalogservice.dto.ProductResponse;
import com.vasav.ecom.catalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private static final int MAX_PRODUCTS = 100;

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedModel<ProductResponse>> list(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(productService.listActive(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getActive(id));
    }

    @GetMapping("/lookup")
    public ResponseEntity<List<ProductResponse>> lookup(@RequestParam List<UUID> ids) {
        if (ids.isEmpty() || ids.size() > MAX_PRODUCTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ids must have 1 to " + MAX_PRODUCTS + " ids");
        }
        return ResponseEntity.ok(productService.lookup(ids));
    }
}
