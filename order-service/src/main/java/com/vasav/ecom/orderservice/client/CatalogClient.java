package com.vasav.ecom.orderservice.client;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface CatalogClient {

    Map<UUID, CatalogProduct> getProducts(Collection<UUID> productIds);
}
