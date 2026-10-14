package com.vasav.ecom.integrationservice.dto;

public record UploadSignatureResponse(
        String uploadUrl,
        String apiKey,
        long timestamp,
        String folder,
        String signature
) {
}
