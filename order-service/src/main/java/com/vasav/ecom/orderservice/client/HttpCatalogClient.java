package com.vasav.ecom.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HttpCatalogClient implements CatalogClient {

    private final RestClient restClient;

    public HttpCatalogClient(RestClient.Builder builder, @Value("${order.catalog.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Map<UUID, CatalogProduct> getProducts(Collection<UUID> productIds) {
        List<ProductView> products;
        try {
            products = restClient.get()
                    .uri(uri -> uri.path("/api/v1/products/lookup").queryParam("ids", productIds.stream().distinct().toList()).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog is unavailable", e);
        }
        return products.stream()
                .map(p -> new CatalogProduct(p.id(), p.name(), p.price(), "ACTIVE".equals(p.status())))
                .collect(Collectors.toMap(CatalogProduct::productId, Function.identity()));
    }

    private record ProductView(UUID id, String name, BigDecimal price, String status) {
    }
}
