package com.vasav.ecom.integrationservice.controller;

import com.vasav.ecom.integrationservice.dto.UploadSignatureResponse;
import com.vasav.ecom.integrationservice.security.CurrentUser;
import com.vasav.ecom.integrationservice.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/api/v1/uploads/signature")
    public ResponseEntity<UploadSignatureResponse> profilePicture(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(uploadService.profilePicture(CurrentUser.from(jwt).subject()));
    }

    @PostMapping("/api/v1/admin/uploads/signature")
    public ResponseEntity<UploadSignatureResponse> productImage() {
        return ResponseEntity.ok(uploadService.productImage());
    }
}
