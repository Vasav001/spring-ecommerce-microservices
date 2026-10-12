package com.vasav.ecom.catalogservice.util.image;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ImageRefValidator {

    private final ImageUrlResolver resolver;

    @Value("${app.images.max-per-product}")
    private int maxImages;

    private static ResponseStatusException unprocessable(String message) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }

    public void validate(UUID productId, List<String> refs) {
        if (refs.size() > maxImages) {
            throw unprocessable("Too many images, max " + maxImages);
        }
        String ownPath = "products/" + productId + "/";
        for (String ref : refs) {
            if (ref.startsWith("https://")) {
                continue;
            }
            int sep = ref.indexOf("://");
            boolean valid = sep > 0
                    && resolver.knowsScheme(ref.substring(0, sep))
                    && ref.substring(sep + 3).startsWith(ownPath);
            if (!valid) {
                throw unprocessable("Invalid image ref: " + ref);
            }
        }
    }
}