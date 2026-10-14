package com.vasav.ecom.orderservice.service;

import com.vasav.ecom.orderservice.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final StringRedisTemplate redis;

    @Value("${order.cart.ttl:30d}")
    private Duration ttl;

    @Value("${order.cart.max-items:50}")
    private int maxItems;

    // list of items(product id and quantity) in a customer's cart
    public CartResponse get(String customerId) {
        return new CartResponse(hash().entries(key(customerId)).entrySet().stream()
                .map(e -> new CartResponse.Item(UUID.fromString(e.getKey()), Integer.parseInt(e.getValue())))
                .sorted(Comparator.comparing(CartResponse.Item::productId))
                .toList());
    }

    // set quantity of product in customer's cart; delete if 0
    public CartResponse setQuantity(String customerId, UUID productId, int quantity) {
        String key = key(customerId);
        if (quantity == 0) {
            hash().delete(key, productId.toString());
        } else {
            if (!hash().hasKey(key, productId.toString()) && hash().size(key) >= maxItems) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cart cannot have more than " + maxItems + " items");
            }
            hash().put(key, productId.toString(), Integer.toString(quantity));
            redis.expire(key, ttl);
        }
        return get(customerId);
    }

//    called by controller to delete hashkey (one map inside the hashset i.e. one product in cart)
    public CartResponse remove(String customerId, UUID productId) {
        return setQuantity(customerId, productId, 0);
    }

//    delete cart entirely
    public void clear(String customerId) {
        redis.delete(key(customerId));
    }

    private HashOperations<String, String, String> hash() {
        return redis.opsForHash();
    }

    // cart key -> each user has one personal cart
    private static String key(String customerId) {
        return "cart:" + customerId;
    }
}
