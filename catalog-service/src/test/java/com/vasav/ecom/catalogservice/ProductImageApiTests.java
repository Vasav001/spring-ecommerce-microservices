package com.vasav.ecom.catalogservice;

import com.vasav.ecom.catalogservice.config.TestJwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductImageApiTests extends AbstractIntegrationTest {

    @Value("${app.images.max-bytes}")
    long maxBytes;

    static String sha1Hex(String s) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(s.getBytes(StandardCharsets.UTF_8)));
    }

    private ResultActions uploadUrl(UUID productId, String contentType, long length) throws Exception {
        return mvc.perform(post(PRODUCTS + "/" + productId + "/images/upload-url").with(TestJwts.admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("contentType", contentType, "contentLength", length))));
    }

    @Test
    void uploadUrlReturnsSignedCloudinaryInstruction() throws Exception {
        UUID id = UUID.randomUUID();
        createProduct(id, uniqueSku(), "DRAFT");

        long before = Instant.now().getEpochSecond();
        MvcResult result = uploadUrl(id, "image/png", 1024)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.url").value("https://api.cloudinary.com/v1_1/test-cloud/image/upload"))
                .andExpect(jsonPath("$.formFields.api_key").value("test-api-key"))
                .andReturn();

        String ref = json(result, "$.ref");
        String publicId = json(result, "$.formFields.public_id");
        String timestamp = json(result, "$.formFields.timestamp");
        assertThat(publicId).matches("products/" + id + "/[0-9a-f-]{36}");   // extension stripped
        assertThat(ref).isEqualTo("cloudinary://" + publicId);
        assertThat(Long.parseLong(timestamp)).isBetween(before, Instant.now().getEpochSecond());
        assertThat(json(result, "$.formFields.signature"))
                .isEqualTo(sha1Hex("public_id=" + publicId + "&timestamp=" + timestamp + "test-api-secret"));

        // the returned ref is accepted on the product and resolved to the delivery URL
        mvc.perform(put(PRODUCTS + "/" + id).with(TestJwts.admin()).header("If-Match", "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(uniqueSku(), "DRAFT", List.of(ref))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0].ref").value(ref))
                .andExpect(jsonPath("$.images[0].url")
                        .value("https://res.cloudinary.com/test-cloud/image/upload/" + publicId));
    }

    @Test
    void allowedTypesAndBoundarySizes() throws Exception {
        UUID id = UUID.randomUUID();
        createProduct(id, uniqueSku(), "DRAFT");
        for (String type : List.of("image/jpeg", "image/png", "image/webp")) {
            uploadUrl(id, type, maxBytes).andExpect(status().isOk());
        }
        uploadUrl(id, "image/jpeg", 1).andExpect(status().isOk());
    }

    @Test
    void unsupportedTypeIs415() throws Exception {
        UUID id = UUID.randomUUID();
        createProduct(id, uniqueSku(), "DRAFT");
        uploadUrl(id, "image/gif", 100).andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
        uploadUrl(id, "application/pdf", 100).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void zeroOrTooLargeIs413() throws Exception {
        UUID id = UUID.randomUUID();
        createProduct(id, uniqueSku(), "DRAFT");
        uploadUrl(id, "image/png", 0).andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.status").value(413));
        uploadUrl(id, "image/png", maxBytes + 1).andExpect(status().isContentTooLarge());
    }

    @Test
    void unknownProductIs404() throws Exception {
        uploadUrl(UUID.randomUUID(), "image/png", 100).andExpect(status().isNotFound());
    }

    @Test
    void customerCannotRequestUploadUrl() throws Exception {
        mvc.perform(post(PRODUCTS + "/" + UUID.randomUUID() + "/images/upload-url").with(TestJwts.customer("c-1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("contentType", "image/png", "contentLength", 10))))
                .andExpect(status().isForbidden());
    }
}
