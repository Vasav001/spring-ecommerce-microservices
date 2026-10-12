package com.vasav.ecom.catalogservice.util.image;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.images")
@Setter
public class ImageUrlResolver {

    private Map<String, String> baseUrls = new HashMap<>();

    private static String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    public String toPublicUrl(String ref) {
        if (ref.startsWith("https://") || ref.startsWith("http://")) {
            return ref;
        }
        int sep = ref.indexOf("://");
        if (sep < 0) {
            throw new IllegalArgumentException("Invalid image ref: " + ref);
        }
        String base = baseUrls.get(ref.substring(0, sep));
        if (base == null) {
            throw new IllegalStateException("No base URL configured for " + ref);
        }
        return stripTrailingSlash(base) + "/" + ref.substring(sep + 3);
    }

    public boolean knowsScheme(String scheme) {
        return baseUrls.containsKey(scheme);
    }
}