package com.prueba_cuscatlan.shopping_Car_miguel.integration;

import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Customer;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Order;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderPayment;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.OrderStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.CustomerRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderPaymentRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderPaymentIntegrationTest {

        @LocalServerPort
        int port;

        @Autowired
        CustomerRepository customerRepository;
        @Autowired
        OrderRepository orderRepository;
        @Autowired
        OrderPaymentRepository paymentRepository;

        WebTestClient client;
        Order savedOrder;

        @BeforeEach
        void setUp() {
                client = WebTestClient
                                .bindToServer()
                                .baseUrl("http://localhost:" + port)
                                .build();

                Customer customer = customerRepository.save(Customer.builder()
                                .name("Miguel").email("miguelxdxp94@gmail.com").address("San Salvador")
                                .build());

                savedOrder = orderRepository.save(Order.builder()
                                .customer(customer)
                                .status(OrderStatus.CONFIRMED)
                                .total(new BigDecimal("99.99"))
                                .build());
        }

        @Test
        @DisplayName("POST /payments → 202 PENDING, async processor resolves to COMPLETED or FAILED")
        void payment_asyncFlow_pendingThenResolved() {
                String body = """
                                {
                                "orderId": %d,
                                "paymentMethod": "CASH",
                                "amount": 99.99
                                }
                                """.formatted(savedOrder.getId());

                // 1 submit: must return 202 with Location header
                client.post()
                                .uri("/api/v1/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(body)
                                .exchange()
                                .expectStatus().isAccepted()
                                .expectHeader().exists("Location")
                                .expectBody()
                                .jsonPath("$.status").isEqualTo("PENDING")
                                .jsonPath("$.id").value(id -> {
                                        Long paymentId = Long.valueOf(id.toString());

                                        // 2 poll DB until async processor finishes (CASH is always approved)
                                        await().atMost(10, SECONDS).untilAsserted(() -> {
                                                OrderPayment payment = paymentRepository.findById(paymentId)
                                                                .orElseThrow();
                                                assertThat(payment.getStatus())
                                                                .isIn(PaymentStatus.COMPLETED, PaymentStatus.FAILED);
                                        });

                                        // 3 order status updated
                                        Order updated = orderRepository.findById(savedOrder.getId()).orElseThrow();
                                        assertThat(updated.getStatus())
                                                        .isIn(OrderStatus.PAID, OrderStatus.PAYMENT_FAILED);

                                        // 4 GET /payments/{id} returns final status
                                        client.get()
                                                        .uri("/api/v1/payments/" + paymentId)
                                                        .exchange()
                                                        .expectStatus().isOk()
                                                        .expectBody()
                                                        .jsonPath("$.status").value(s -> assertThat(s.toString())
                                                                        .isIn("COMPLETED", "FAILED"));
                                });
        }

        @Test
        @DisplayName("Duplicate Idempotency-Key returns same payment without reprocessing")
        void payment_idempotencyKey_returnsSameResult() {
                String body = """
                                {
                                  "orderId": %d,
                                  "paymentMethod": "CASH",
                                  "amount": 99.99
                                }
                                """.formatted(savedOrder.getId());

                // first call
                client.post()
                                .uri("/api/v1/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Idempotency-Key", "idem-001")
                                .bodyValue(body)
                                .exchange()
                                .expectStatus().isAccepted()
                                .expectBody()
                                .jsonPath("$.id").value(id -> {
                                        Long paymentId = Long.valueOf(id.toString());

                                        await().atMost(10, SECONDS).untilAsserted(() -> {
                                                OrderPayment p = paymentRepository.findById(paymentId).orElseThrow();
                                                assertThat(p.getStatus()).isIn(PaymentStatus.COMPLETED,
                                                                PaymentStatus.FAILED);
                                        });

                                        // second call — same key
                                        client.post()
                                                        .uri("/api/v1/payments")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .header("Idempotency-Key", "idem-001")
                                                        .bodyValue(body)
                                                        .exchange()
                                                        .expectStatus().isAccepted()
                                                        .expectBody()
                                                        .jsonPath("$.id").isEqualTo(paymentId.intValue());

                                        assertThat(paymentRepository.count()).isEqualTo(1);
                                });
        }

        @Test
        @DisplayName("Amount mismatch returns 400 Bad Request")
        void payment_wrongAmount_returns400() {
                String body = """
                                {
                                "orderId": %d,
                                "paymentMethod": "CASH",
                                "amount": 1.00
                                }
                                """.formatted(savedOrder.getId());

                client.post()
                                .uri("/api/v1/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(body)
                                .exchange()
                                .expectStatus().isBadRequest()
                                .expectBody()
                                .jsonPath("$.message")
                                .value(msg -> assertThat(msg.toString()).contains("does not match"));
        }
}
