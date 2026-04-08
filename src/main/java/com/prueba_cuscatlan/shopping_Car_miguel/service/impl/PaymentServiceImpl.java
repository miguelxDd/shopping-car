package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.BadRequestException;
import com.prueba_cuscatlan.shopping_Car_miguel.exception.ResourceNotFoundException;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Order;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderPayment;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.OrderStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderPaymentRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.service.PaymentService;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentAsyncProcessor;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final PaymentAsyncProcessor asyncProcessor;

    @Override
    @Transactional
    public OrderPaymentResponse processPayment(String userId, String idempotencyKey, OrderPaymentRequest request) {
        // Idempotency: same key → return stored result without reprocessing
        if (idempotencyKey != null) {
            var existing = orderPaymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent replay key={} orderId={}", idempotencyKey, request.getOrderId());
                return toResponse(existing.get());
            }
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to the authenticated user");
        }

        validatePaymentEligibility(order, request.getAmount());

        // 1. Persist PENDING immediately — HTTP thread returns 202 right after this
        OrderPayment payment = OrderPayment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();
        orderPaymentRepository.save(payment);

        // 2. Build context and hand off to async executor — non-blocking
        PaymentContext context = PaymentContext.builder()
                .orderId(order.getId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .build();

        asyncProcessor.execute(payment.getId(), context);

        log.info("Payment accepted orderId={} paymentId={} method={}",
                order.getId(), payment.getId(), request.getPaymentMethod());

        return toResponse(payment); // PENDING — client polls GET /payments/{id}
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPaymentResponse findById(String userId, Long id) {
        OrderPayment payment = orderPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        if (!payment.getOrder().getUserId().equals(userId)) {
            throw new AccessDeniedException("Payment does not belong to the authenticated user");
        }
        return toResponse(payment);
    }

    private void validatePaymentEligibility(Order order, BigDecimal amount) {
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BadRequestException("Order " + order.getId() + " is already paid");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay a cancelled order");
        }
        orderPaymentRepository.findByOrderId(order.getId()).ifPresent(existing -> {
            if (existing.getStatus() == PaymentStatus.PENDING || existing.getStatus() == PaymentStatus.COMPLETED) {
                throw new BadRequestException("A payment already exists for order " + order.getId());
            }
            // FAILED payment — delete to allow retry
            orderPaymentRepository.delete(existing);
        });
        if (amount.compareTo(order.getTotal()) != 0) {
            throw new BadRequestException(
                    "Payment amount " + amount + " does not match order total " + order.getTotal());
        }
    }

    private OrderPaymentResponse toResponse(OrderPayment payment) {
        return OrderPaymentResponse.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .transactionDate(payment.getTransactionDate())
                .build();
    }
}
