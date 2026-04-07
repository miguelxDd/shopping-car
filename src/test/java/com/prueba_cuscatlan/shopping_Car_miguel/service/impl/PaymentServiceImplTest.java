package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.BadRequestException;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Order;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderPayment;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.OrderStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentMethod;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderPaymentRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentContext;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentResult;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentStrategy;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

        @Mock
        OrderRepository orderRepository;
        @Mock
        OrderPaymentRepository orderPaymentRepository;
        @Mock
        PaymentStrategyFactory strategyFactory;
        @Mock
        PaymentStrategy paymentStrategy;

        @InjectMocks
        PaymentServiceImpl paymentService;

        private Order order;
        private OrderPaymentRequest request;

        @BeforeEach
        void setUp() {
                order = Order.builder()
                                .id(1L).status(OrderStatus.CONFIRMED)
                                .total(new BigDecimal("99.99"))
                                .build();

                request = OrderPaymentRequest.builder()
                                .orderId(1L)
                                .paymentMethod(PaymentMethod.CREDIT_CARD)
                                .amount(new BigDecimal("99.99"))
                                .build();
        }

        @Test
        @DisplayName("processPayment returns COMPLETED when strategy approves")
        void processPayment_returnsCompleted_whenApproved() {
                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
                when(strategyFactory.resolve(PaymentMethod.CREDIT_CARD)).thenReturn(paymentStrategy);
                when(paymentStrategy.process(any(PaymentContext.class)))
                                .thenReturn(CompletableFuture.completedFuture(PaymentResult.builder()
                                                .approved(true).transactionId("CC-123").message("Approved").build()));
                when(orderPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
                when(orderRepository.save(any())).thenReturn(order);

                OrderPaymentResponse response = paymentService.processPayment(null, request);

                assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
                assertThat(response.getTransactionId()).isEqualTo("CC-123");
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("processPayment returns FAILED when strategy declines")
        void processPayment_returnsFailed_whenDeclined() {
                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
                when(strategyFactory.resolve(PaymentMethod.CREDIT_CARD)).thenReturn(paymentStrategy);
                when(paymentStrategy.process(any(PaymentContext.class)))
                                .thenReturn(CompletableFuture.completedFuture(PaymentResult.builder()
                                                .approved(false).transactionId(null).message("Declined").build()));
                when(orderPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
                when(orderRepository.save(any())).thenReturn(order);

                OrderPaymentResponse response = paymentService.processPayment(null, request);

                assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        }

        @Test
        @DisplayName("processPayment throws BadRequestException when order is already paid")
        void processPayment_throwsBadRequest_whenAlreadyPaid() {
                order.setStatus(OrderStatus.PAID);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> paymentService.processPayment(null, request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("already paid");
        }

        @Test
        @DisplayName("processPayment throws BadRequestException when amount mismatches")
        void processPayment_throwsBadRequest_whenAmountMismatch() {
                request = OrderPaymentRequest.builder()
                                .orderId(1L).paymentMethod(PaymentMethod.CASH)
                                .amount(new BigDecimal("50.00")) // order total is 99.99
                                .build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> paymentService.processPayment(null, request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("does not match");
        }

        @Test
        @DisplayName("processPayment is idempotent — returns stored result on duplicate key")
        void processPayment_returnsStoredResult_onDuplicateIdempotencyKey() {
                OrderPayment existingPayment = OrderPayment.builder()
                                .id(99L).paymentMethod(PaymentMethod.CREDIT_CARD)
                                .amount(new BigDecimal("99.99")).status(PaymentStatus.COMPLETED)
                                .transactionId("CC-ALREADY").idempotencyKey("key-abc")
                                .build();

                when(orderPaymentRepository.findByIdempotencyKey("key-abc"))
                                .thenReturn(Optional.of(existingPayment));

                OrderPaymentResponse response = paymentService.processPayment("key-abc", request);

                assertThat(response.getTransactionId()).isEqualTo("CC-ALREADY");
                assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
                // Should NOT call the strategy at all
                verifyNoInteractions(strategyFactory, orderRepository);
        }
}
