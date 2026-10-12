package com.vasav.ecom.catalogservice.controller;

import com.vasav.ecom.catalogservice.dto.images.UploadUrlRequest;
import com.vasav.ecom.catalogservice.repository.ProductRepository;
import com.vasav.ecom.catalogservice.util.image.ImageStorage;
import com.vasav.ecom.catalogservice.util.image.UploadInstruction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final ImageStorage storage;
    private final ProductRepository products;

    @Value("${app.images.max-bytes}")
    private long maxBytes;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload-url")
    public UploadInstruction uploadUrl(@PathVariable UUID productId, @RequestBody UploadUrlRequest request) {
        String ext = EXTENSIONS.get(request.contentType());
        if (ext == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only jpeg, png, webp allowed");
        }
        if (request.contentLength() <= 0 || request.contentLength() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "Max " + maxBytes + " bytes");
        }
        if (!products.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        String key = "products/" + productId + "/" + UUID.randomUUID() + "." + ext;
        return storage.prepareUpload(key, request.contentType(), request.contentLength());
    }
}