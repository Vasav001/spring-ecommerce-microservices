package com.vasav.ecom.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DummyCatalogClient implements CatalogClient {

    @Value("${order.catalog.dummy.price:100.00}")
    private BigDecimal price;

    @Value("${order.catalog.dummy.inactive-products:}")
    private List<UUID> inactiveProducts;

    @Override
    public Map<UUID, CatalogProduct> getProducts(Collection<UUID> productIds) {
        return productIds.stream().distinct().collect(Collectors.toMap(Function.identity(), id -> new CatalogProduct(
                id, "Product " + id.toString().substring(0, 8), price, !inactiveProducts.contains(id))));
    }
}
