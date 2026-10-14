package com.vasav.ecom.integrationservice.service;

import com.vasav.ecom.integrationservice.dto.ShipmentResponse;
import com.vasav.ecom.integrationservice.entity.Shipment;
import com.vasav.ecom.integrationservice.entity.enums.ShipmentStatus;
import com.vasav.ecom.integrationservice.events.EventTypes;
import com.vasav.ecom.integrationservice.events.OrderConfirmed;
import com.vasav.ecom.integrationservice.producer.ShipmentEventProducer;
import com.vasav.ecom.integrationservice.repository.ProcessedEventRepository;
import com.vasav.ecom.integrationservice.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static com.vasav.ecom.integrationservice.entity.enums.ShipmentStatus.CREATED;
import static com.vasav.ecom.integrationservice.entity.enums.ShipmentStatus.DELIVERED;
import static com.vasav.ecom.integrationservice.entity.enums.ShipmentStatus.DISPATCHED;
import static com.vasav.ecom.integrationservice.entity.enums.ShipmentStatus.FAILED;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private static final String CONSUMER = "integration-service.shipping";

    private final ShipmentRepository shipmentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ShipmentEventProducer shipmentEvents;

    @Transactional
    public void create(UUID eventId, OrderConfirmed event) {
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        shipmentRepository.create(event.orderId(), event.customerId());
    }

    @Transactional
    public ShipmentResponse dispatch(UUID orderId) {
        return move(orderId, List.of(CREATED), DISPATCHED, null, EventTypes.SHIPMENT_DISPATCHED);
    }

    @Transactional
    public ShipmentResponse deliver(UUID orderId) {
        return move(orderId, List.of(DISPATCHED), DELIVERED, null, EventTypes.SHIPMENT_DELIVERED);
    }

    @Transactional
    public ShipmentResponse fail(UUID orderId) {
        return move(orderId, List.of(CREATED, DISPATCHED), FAILED, "SHIPMENT_FAILED", EventTypes.SHIPMENT_FAILED);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse get(String customerId, UUID orderId) {
        return ShipmentResponse.from(shipmentRepository.findByOrderIdAndCustomerId(orderId, customerId)
                .orElseThrow(ShipmentService::notFound));
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getAny(UUID orderId) {
        return ShipmentResponse.from(find(orderId));
    }

    private ShipmentResponse move(UUID orderId, List<ShipmentStatus> from, ShipmentStatus to, String reason,
                                  String eventType) {
        find(orderId);
        if (shipmentRepository.updateStatus(orderId, from, to, reason) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Shipment is not " + from);
        }
        shipmentEvents.publish(orderId, eventType, reason);
        return ShipmentResponse.from(find(orderId));
    }

    private Shipment find(UUID orderId) {
        return shipmentRepository.findById(orderId).orElseThrow(ShipmentService::notFound);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found");
    }
}
