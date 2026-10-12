package com.vasav.ecom.catalogservice.util.image;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Offline: no Spring context, no network, dummy credentials. */
class ImageStorageUnitTests {

    private static String sha1Hex(String s) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(s.getBytes(StandardCharsets.UTF_8)));
    }

    private static CloudinaryImageStorage storage(String uploadPreset) {
        CloudinaryImageStorage storage = new CloudinaryImageStorage();
        ReflectionTestUtils.setField(storage, "cloudName", "demo-cloud");
        ReflectionTestUtils.setField(storage, "apiKey", "key-123");
        ReflectionTestUtils.setField(storage, "apiSecret", "s3cr3t");
        ReflectionTestUtils.setField(storage, "uploadPreset", uploadPreset);
        return storage;
    }

    @Test
    void signatureIsSha1OfSortedParamsPlusSecret() throws Exception {
        UploadInstruction ui = storage("").prepareUpload("products/p1/abc.jpg", "image/jpeg", 10);

        Map<String, String> form = ui.formFields();
        assertThat(form).containsOnlyKeys("public_id", "timestamp", "api_key", "signature");
        assertThat(form.get("public_id")).isEqualTo("products/p1/abc");
        assertThat(form.get("api_key")).isEqualTo("key-123");
        assertThat(form.get("signature"))
                .isEqualTo(sha1Hex("public_id=products/p1/abc&timestamp=" + form.get("timestamp") + "s3cr3t"));
        assertThat(ui.ref()).isEqualTo("cloudinary://products/p1/abc");
        assertThat(ui.method()).isEqualTo("POST");
        assertThat(ui.url()).isEqualTo("https://api.cloudinary.com/v1_1/demo-cloud/image/upload");
        assertThat(ui.headers()).isEmpty();
    }

    @Test
    void uploadPresetIsSignedInSortedPosition() throws Exception {
        Map<String, String> form = storage("catalog").prepareUpload("products/p1/abc.webp", "image/webp", 10)
                .formFields();

        assertThat(form.get("upload_preset")).isEqualTo("catalog");
        assertThat(form.get("signature")).isEqualTo(sha1Hex(
                "public_id=products/p1/abc&timestamp=" + form.get("timestamp") + "&upload_preset=catalog" + "s3cr3t"));
    }

    @Test
    void resolverMapsSchemeRefsAndPassesHttpsThrough() {
        ImageUrlResolver resolver = new ImageUrlResolver();
        resolver.setBaseUrls(Map.of("cloudinary", "https://res.cloudinary.com/demo-cloud/image/upload/"));

        assertThat(resolver.toPublicUrl("cloudinary://products/p1/abc"))
                .isEqualTo("https://res.cloudinary.com/demo-cloud/image/upload/products/p1/abc");   // no double slash
        assertThat(resolver.toPublicUrl("https://cdn.example.com/a.jpg")).isEqualTo("https://cdn.example.com/a.jpg");
        assertThat(resolver.knowsScheme("cloudinary")).isTrue();
        assertThat(resolver.knowsScheme("s3")).isFalse();
        assertThatThrownBy(() -> resolver.toPublicUrl("s3://x")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> resolver.toPublicUrl("no-scheme")).isInstanceOf(IllegalArgumentException.class);
    }
}
