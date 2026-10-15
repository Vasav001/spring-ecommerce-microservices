package com.vasav.ecom.orderservice.service;

import com.vasav.ecom.orderservice.client.CatalogClient;
import com.vasav.ecom.orderservice.client.CatalogProduct;
import com.vasav.ecom.orderservice.dto.CartResponse;
import com.vasav.ecom.orderservice.dto.OrderResponse;
import com.vasav.ecom.orderservice.dto.ShippingAddress;
import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.entity.OrderSaga;
import com.vasav.ecom.orderservice.entity.enums.SagaStep;
import com.vasav.ecom.orderservice.producer.InventoryCommandProducer;
import com.vasav.ecom.orderservice.repository.OrderRepository;
import com.vasav.ecom.orderservice.repository.OrderSagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final CatalogClient catalogClient;
    private final OrderRepository orderRepository;
    private final OrderSagaRepository orderSagaRepository;
    private final InventoryCommandProducer inventoryCommandProducer;

    @Value("${order.saga.step-timeout:10m}")
    private Duration stepTimeout;

    public record Result(OrderResponse order, boolean created) {
    }

    @Transactional
    public Result checkout(String customerId, String idempotencyKey, ShippingAddress address) {
        Optional<Order> existing = orderRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey);
        if (existing.isPresent()) {
            return new Result(OrderResponse.from(existing.get()), false);
        }

        List<CartResponse.CartItem> cart = cartService.get(customerId).items();
        if (cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Map<UUID, CatalogProduct> products = catalogClient.getProducts(cart.stream().map(CartResponse.CartItem::productId).toList());
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setIdempotencyKey(idempotencyKey);
        address.applyTo(order);
        BigDecimal total = BigDecimal.ZERO;
        for (CartResponse.CartItem item : cart) {
            CatalogProduct product = products.get(item.productId());
            if (product == null || !product.active()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product " + item.productId() + " is not available");
            }
            order.addItem(product.productId(), product.name(), product.price(), item.quantity());
            total = total.add(product.price().multiply(BigDecimal.valueOf(item.quantity())));
        }
        order.setTotalAmount(total);
        orderRepository.saveAndFlush(order);

        OrderSaga saga = new OrderSaga();
        saga.setOrderId(order.getId());
        saga.setStep(SagaStep.RESERVING_INVENTORY);
        saga.setStepDeadline(Instant.now().plus(stepTimeout));
        orderSagaRepository.save(saga);

        inventoryCommandProducer.reserve(order);
        return new Result(OrderResponse.from(order), true);
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponse> findByIdempotencyKey(String customerId, String idempotencyKey) {
        return orderRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey).map(OrderResponse::from);
    }
}
