package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.BadRequestException;
import com.prueba_cuscatlan.shopping_Car_miguel.exception.ResourceNotFoundException;
import com.prueba_cuscatlan.shopping_Car_miguel.mapper.OrderMapper;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ExternalProductDTO;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderDetailRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.UpdateOrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Cart;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Order;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderDetail;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.OrderStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.CartRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.service.ExternalProductService;
import com.prueba_cuscatlan.shopping_Car_miguel.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ExternalProductService externalProductService;
    private final OrderMapper orderMapper;

    // Checkout: converts the user's cart into a confirmed order, then clears the
    // cart.
    // Prices are fetched fresh from FakeStore at checkout time.

    @Override
    @Transactional
    public OrderResponse checkout(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user: " + userId));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout an empty cart");
        }

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.CONFIRMED)
                .build();

        List<OrderDetail> details = cart.getItems().stream().map(item -> {
            ExternalProductDTO product = externalProductService.findById(item.getProductId());
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            return OrderDetail.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getTitle())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
        }).toList();

        order.getDetails().addAll(details);
        order.setTotal(computeTotal(details));

        OrderResponse response = orderMapper.toResponse(orderRepository.save(order));

        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Checkout complete userId={} orderId={} total={}",
                userId, response.getId(), response.getTotal());

        return response;
    }

    @Override
    @Transactional
    public OrderResponse create(String userId, OrderRequest request) {
        Order order = Order.builder()
                .userId(userId)
                .build();

        List<OrderDetail> details = buildDetails(request.getItems(), order);
        order.getDetails().addAll(details);
        order.setTotal(computeTotal(details));

        log.info("Creating order for userId={} items={} total={}",
                userId, details.size(), order.getTotal());

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public Page<OrderResponse> findAllByUser(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toResponse);
    }

    @Override
    public OrderResponse findById(String userId, Long id) {
        return orderMapper.toResponse(findOwnedOrderOrThrow(userId, id));
    }

    @Override
    @Transactional
    public OrderResponse update(String userId, Long id, UpdateOrderRequest request) {
        Order order = findOwnedOrderOrThrow(userId, id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a cancelled order");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot update a delivered order");
        }
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPED) {
                throw new BadRequestException("Cannot modify items on an order that is already " + order.getStatus());
            }
            order.getDetails().clear();
            List<OrderDetail> newDetails = buildDetails(request.getItems(), order);
            order.getDetails().addAll(newDetails);
            order.setTotal(computeTotal(newDetails));
        }
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        log.info("Updating orderId={} status={} total={}", id, order.getStatus(), order.getTotal());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancel(String userId, Long id) {
        Order order = findOwnedOrderOrThrow(userId, id);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel an order that has already been delivered");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BadRequestException("Cannot cancel an order that has already been paid");
        }
        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new BadRequestException("Cannot cancel an order that has already been shipped");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order id={} cancelled", id);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<OrderDetail> buildDetails(List<OrderDetailRequest> items, Order order) {
        return items.stream().map(item -> {
            ExternalProductDTO product = externalProductService.findById(item.getProductId());
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            return OrderDetail.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getTitle())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
        }).toList();
    }

    private BigDecimal computeTotal(List<OrderDetail> details) {
        return details.stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private Order findOwnedOrderOrThrow(String userId, Long id) {
        Order order = findOrderOrThrow(id);
        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to the authenticated user");
        }
        return order;
    }
}
