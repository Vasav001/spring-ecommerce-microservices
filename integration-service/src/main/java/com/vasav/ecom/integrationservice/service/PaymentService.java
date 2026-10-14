package com.vasav.ecom.integrationservice.service;

import com.vasav.ecom.integrationservice.dto.PayRequest;
import com.vasav.ecom.integrationservice.dto.PaymentResponse;
import com.vasav.ecom.integrationservice.entity.Payment;
import com.vasav.ecom.integrationservice.entity.enums.PaymentStatus;
import com.vasav.ecom.integrationservice.events.EventTypes;
import com.vasav.ecom.integrationservice.events.ProcessPayment;
import com.vasav.ecom.integrationservice.producer.PaymentReplyProducer;
import com.vasav.ecom.integrationservice.repository.PaymentRepository;
import com.vasav.ecom.integrationservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String CONSUMER = "integration-service.payments";

    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final PaymentReplyProducer replies;

    @Transactional
    public void process(UUID eventId, ProcessPayment command) {
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        paymentRepository.createPending(command.orderId(), command.customerId(), command.amount());
    }

    @Transactional
    public void refund(UUID eventId, UUID orderId) {
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        if (!paymentRepository.existsById(orderId)) {
            log.warn("Refund for unknown payment of order {}", orderId);
            return;
        }
        paymentRepository.refund(orderId);
        replies.reply(orderId, EventTypes.PAYMENT_REFUNDED, null);
    }

    @Transactional
    public PaymentResponse pay(String customerId, UUID orderId, PayRequest request) {
        Payment payment = find(customerId, orderId);
        String reason = request.failed() ? "DECLINED"
                : request.amount().compareTo(payment.getAmount()) != 0 ? "AMOUNT_MISMATCH"
                : null;
        PaymentStatus to = reason == null ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;
        if (paymentRepository.updateStatus(orderId, PaymentStatus.PENDING, to, reason) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment is not pending");
        }
        replies.reply(orderId, reason == null ? EventTypes.PAYMENT_SUCCEEDED : EventTypes.PAYMENT_FAILED, reason);
        return PaymentResponse.from(find(customerId, orderId));
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(String customerId, UUID orderId) {
        return PaymentResponse.from(find(customerId, orderId));
    }

    private Payment find(String customerId, UUID orderId) {
        return paymentRepository.findByOrderIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }
}
