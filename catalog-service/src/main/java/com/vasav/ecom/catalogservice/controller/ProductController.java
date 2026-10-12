package com.vasav.ecom.catalogservice.controller;

import com.vasav.ecom.catalogservice.dto.product.AdminProductResponse;
import com.vasav.ecom.catalogservice.dto.product.ProductRequest;
import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;
import com.vasav.ecom.catalogservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private static String etag(AdminProductResponse product) {
        return "\"" + product.version() + "\"";
    }

    private static long parseVersion(String ifMatch) {
        String value = ifMatch.replace("W/", "").replace("\"", "").trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid If-Match: " + ifMatch);
        }
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProductResponse> put(
            @PathVariable UUID productId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @Valid @RequestBody ProductRequest request) {

        if ("*".equals(ifNoneMatch)) {
            AdminProductResponse created = productService.create(productId, request);
            return ResponseEntity.status(HttpStatus.CREATED).eTag(etag(created)).body(created);
        }
        if (ifMatch != null) {
            AdminProductResponse updated = productService.update(productId, parseVersion(ifMatch), request);
            return ResponseEntity.ok().eTag(etag(updated)).body(updated);
        }
        throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,
                "Send If-None-Match: * to create or If-Match: \"<version>\" to update");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductResponse> get(@PathVariable UUID productId) {
        AdminProductResponse product = productService.get(productId);
        return ResponseEntity.ok().eTag(etag(product)).body(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PagedModel<AdminProductResponse> getAll(
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(productService.getAll(status, pageable));
    }
}