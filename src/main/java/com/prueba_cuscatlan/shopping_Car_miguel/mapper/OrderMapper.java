package com.prueba_cuscatlan.shopping_Car_miguel.mapper;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderDetailResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Order;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderDetail;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final CustomerMapper customerMapper;

    public OrderResponse toResponse(Order order) {
        List<OrderDetailResponse> details = order.getDetails().stream()
                .map(this::toDetailResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customer(customerMapper.toResponse(order.getCustomer()))
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .total(order.getTotal())
                .details(details)
                .payment(order.getPayment() != null ? toPaymentResponse(order.getPayment()) : null)
                .build();
    }

    private OrderDetailResponse toDetailResponse(OrderDetail detail) {
        return OrderDetailResponse.builder()
                .id(detail.getId())
                .productId(detail.getProductId())
                .productName(detail.getProductName())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .subtotal(detail.getSubtotal())
                .build();
    }

    private OrderPaymentResponse toPaymentResponse(OrderPayment payment) {
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
