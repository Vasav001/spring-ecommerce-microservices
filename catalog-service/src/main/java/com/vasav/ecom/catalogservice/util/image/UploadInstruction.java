package com.vasav.ecom.catalogservice.util.image;

import java.util.Map;

public record UploadInstruction(
        String ref,
        String method,
        String url,
        Map<String, String> headers,
        Map<String, String> formFields
) {
}