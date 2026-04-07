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
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentContext;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentResult;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final PaymentStrategyFactory strategyFactory;

    @Override
    @Transactional
    public OrderPaymentResponse processPayment(String idempotencyKey, OrderPaymentRequest request) {
        // Idempotency check: same key , return the stored result, no re-processing
        if (idempotencyKey != null) {
            var existing = orderPaymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent replay for key={} orderId={}", idempotencyKey, request.getOrderId());
                return toResponse(existing.get());
            }
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        validatePaymentEligibility(order, request.getAmount());

        PaymentContext context = PaymentContext.builder()
                .order(order)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .build();

        log.info("Processing {} payment for orderId={} amount={}",
                request.getPaymentMethod(), order.getId(), request.getAmount());

        PaymentResult result = resolveAndProcess(context);

        OrderPayment payment = OrderPayment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getAmount())
                .status(result.isApproved() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .transactionId(result.getTransactionId())
                .idempotencyKey(idempotencyKey)
                .build();

        orderPaymentRepository.save(payment);

        order.setStatus(result.isApproved() ? OrderStatus.PAID : OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);

        log.info("Payment for orderId={} finished — approved={} txId={}",
                order.getId(), result.isApproved(), result.getTransactionId());

        return toResponse(payment);
    }

    private PaymentResult resolveAndProcess(PaymentContext context) {
        try {
            return strategyFactory.resolve(context.getPaymentMethod()).process(context).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Payment processing was interrupted");
        } catch (ExecutionException e) {
            throw new BadRequestException("Payment processing failed: " + e.getCause().getMessage());
        }
    }

    private void validatePaymentEligibility(Order order, BigDecimal amount) {
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BadRequestException("Order " + order.getId() + " is already paid");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay a cancelled order");
        }
        if (orderPaymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BadRequestException("A payment already exists for order " + order.getId());
        }
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
