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
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentAsyncProcessor;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

        @Mock
        OrderRepository orderRepository;
        @Mock
        OrderPaymentRepository orderPaymentRepository;
        @Mock
        PaymentAsyncProcessor asyncProcessor;

        @InjectMocks
        PaymentServiceImpl paymentService;

        private Order order;
        private OrderPaymentRequest request;

        @BeforeEach
        void setUp() {
                order = Order.builder()
                                .id(1L).userId("test-user").status(OrderStatus.CONFIRMED)
                                .total(new BigDecimal("99.99"))
                                .build();

                request = OrderPaymentRequest.builder()
                                .orderId(1L)
                                .paymentMethod(PaymentMethod.CREDIT_CARD)
                                .amount(new BigDecimal("99.99"))
                                .build();
        }

        @Test
        @DisplayName("processPayment returns PENDING immediately and fires async processor")
        void processPayment_returnsPending_andFiresAsync() {
                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
                when(orderPaymentRepository.save(any())).thenAnswer(inv -> {
                        OrderPayment p = inv.getArgument(0);
                        p.setId(42L);
                        return p;
                });

                OrderPaymentResponse response = paymentService.processPayment("test-user", null, request);

                assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
                assertThat(response.getId()).isEqualTo(42L);
                // async processor must be triggered exactly once with the right payment id
                verify(asyncProcessor).execute(eq(42L), any(PaymentContext.class));
                verifyNoMoreInteractions(asyncProcessor);
        }

        @Test
        @DisplayName("processPayment throws BadRequestException when order is already paid")
        void processPayment_throwsBadRequest_whenAlreadyPaid() {
                order.setStatus(OrderStatus.PAID);
                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> paymentService.processPayment("test-user", null, request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("already paid");

                verifyNoInteractions(asyncProcessor);
        }

        @Test
        @DisplayName("processPayment throws BadRequestException when amount mismatches order total")
        void processPayment_throwsBadRequest_whenAmountMismatch() {
                request = OrderPaymentRequest.builder()
                                .orderId(1L).paymentMethod(PaymentMethod.CASH)
                                .amount(new BigDecimal("50.00")) // order total is 99.99
                                .build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> paymentService.processPayment("test-user", null, request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("does not match");

                verifyNoInteractions(asyncProcessor);
        }

        @Test
        @DisplayName("processPayment throws BadRequestException when a payment already exists for the order")
        void processPayment_throwsBadRequest_whenDuplicateOrderPayment() {
                OrderPayment existing = OrderPayment.builder()
                                .id(10L).status(PaymentStatus.PENDING).build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(existing));

                assertThatThrownBy(() -> paymentService.processPayment("test-user", null, request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("already exists");

                verifyNoInteractions(asyncProcessor);
        }

        @Test
        @DisplayName("processPayment is idempotent — returns stored result on duplicate Idempotency-Key")
        void processPayment_returnsStoredResult_onDuplicateIdempotencyKey() {
                OrderPayment existingPayment = OrderPayment.builder()
                                .id(99L).paymentMethod(PaymentMethod.CREDIT_CARD)
                                .amount(new BigDecimal("99.99")).status(PaymentStatus.COMPLETED)
                                .transactionId("CC-ALREADY").idempotencyKey("key-abc")
                                .build();

                when(orderPaymentRepository.findByIdempotencyKey("key-abc"))
                                .thenReturn(Optional.of(existingPayment));

                OrderPaymentResponse response = paymentService.processPayment("test-user", "key-abc", request);

                assertThat(response.getTransactionId()).isEqualTo("CC-ALREADY");
                assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
                // No order lookup, no async processing
                verifyNoInteractions(asyncProcessor, orderRepository);
        }
}
