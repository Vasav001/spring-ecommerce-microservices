package com.vasav.ecom.catalogservice.util.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
public class CloudinaryImageStorage implements ImageStorage {

    @Value("${app.storage.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${app.storage.cloudinary.api-key}")
    private String apiKey;

    @Value("${app.storage.cloudinary.api-secret}")
    private String apiSecret;

    @Value("${app.storage.cloudinary.upload-preset:}")
    private String uploadPreset;

    @Override
    public String scheme() {
        return "cloudinary";
    }

    @Override
    public UploadInstruction prepareUpload(String key, String contentType, long contentLength) {
        String publicId = key.replaceFirst("\\.[A-Za-z0-9]+$", "");

        Map<String, String> signed = new TreeMap<>();
        signed.put("public_id", publicId);
        signed.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));
        if (!uploadPreset.isBlank()) {
            signed.put("upload_preset", uploadPreset);
        }

        String toSign = String.join("&", signed.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .toList());

        Map<String, String> form = new TreeMap<>(signed);
        form.put("api_key", apiKey);
        form.put("signature", sha1Hex(toSign + apiSecret));

        return new UploadInstruction(
                scheme() + "://" + publicId,
                "POST",
                "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload",
                Map.of(),
                form);
    }

    private static String sha1Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}