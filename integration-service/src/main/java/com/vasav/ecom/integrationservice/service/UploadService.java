package com.vasav.ecom.integrationservice.service;

import com.vasav.ecom.integrationservice.dto.UploadSignatureResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class UploadService {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public UploadService(@Value("${cloudinary.cloud-name}") String cloudName,
                         @Value("${cloudinary.api-key}") String apiKey,
                         @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public UploadSignatureResponse productImage() {
        return sign("products");
    }

    public UploadSignatureResponse profilePicture(String customerId) {
        return sign("profiles/" + customerId);
    }

    private UploadSignatureResponse sign(String folder) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Image uploads are not configured");
        }
        long timestamp = Instant.now().getEpochSecond();
        String signature = sha1("folder=" + folder + "&timestamp=" + timestamp + apiSecret);
        return new UploadSignatureResponse("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload",
                apiKey, timestamp, folder, signature);
    }

    private static String sha1(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
