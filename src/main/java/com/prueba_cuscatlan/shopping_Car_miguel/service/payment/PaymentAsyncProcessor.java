package com.prueba_cuscatlan.shopping_Car_miguel.service.payment;

import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderPayment;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.OrderStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderPaymentRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAsyncProcessor {

        private final OrderPaymentRepository orderPaymentRepository;
        private final OrderRepository orderRepository;
        private final PaymentStrategyFactory strategyFactory;

        @Async("paymentExecutor")
        @Transactional
        public void execute(Long paymentId, PaymentContext context) {
                log.info("Async payment processing started paymentId={} thread={}",
                                paymentId, Thread.currentThread().getName());

                OrderPayment payment = orderPaymentRepository.findById(paymentId)
                                .orElseThrow(() -> new IllegalStateException("Payment not found: " + paymentId));

                try {
                        PaymentResult result = strategyFactory
                                        .resolve(context.getPaymentMethod())
                                        .process(context); // synchronous — Thread.sleep runs here on executor thread

                        payment.setStatus(result.isApproved() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
                        payment.setTransactionId(result.getTransactionId());

                        context.getOrder().setStatus(
                                        result.isApproved() ? OrderStatus.PAID : OrderStatus.PAYMENT_FAILED);

                        log.info("Async payment done paymentId={} approved={} txId={}",
                                        paymentId, result.isApproved(), result.getTransactionId());

                } catch (Exception ex) {
                        log.error("Async payment failed paymentId={}: {}", paymentId, ex.getMessage());
                        payment.setStatus(PaymentStatus.FAILED);
                        context.getOrder().setStatus(OrderStatus.PAYMENT_FAILED);
                }

                orderPaymentRepository.save(payment);
                orderRepository.save(context.getOrder());
        }
}
