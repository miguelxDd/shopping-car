package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.BadRequestException;
import com.prueba_cuscatlan.shopping_Car_miguel.exception.ResourceNotFoundException;
import com.prueba_cuscatlan.shopping_Car_miguel.mapper.OrderMapper;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ExternalProductDTO;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderDetailRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.UpdateOrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Customer;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Order;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.OrderDetail;
import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.OrderStatus;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.CustomerRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.OrderRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.service.ExternalProductService;
import com.prueba_cuscatlan.shopping_Car_miguel.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ExternalProductService externalProductService; // injected proxy
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        Customer customer = findCustomerOrThrow(request.getCustomerId());

        Order order = Order.builder()
                .customer(customer)
                .build();

        List<OrderDetail> details = buildDetails(request.getItems(), order);
        BigDecimal total = computeTotal(details);

        order.getDetails().addAll(details);
        order.setTotal(total);

        log.info("Creating order for customerId={} with {} items, total={}",
                customer.getId(), details.size(), total);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    public OrderResponse findById(Long id) {
        return orderMapper.toResponse(findOrderOrThrow(id));
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, UpdateOrderRequest request) {
        Order order = findOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a cancelled order");
        }

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            order.getDetails().clear();
            List<OrderDetail> newDetails = buildDetails(request.getItems(), order);
            order.getDetails().addAll(newDetails);
            order.setTotal(computeTotal(newDetails));
        }

        log.info("Updating orderId={} status={} total={}", id, order.getStatus(), order.getTotal());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Order order = findOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel an order that has already been delivered");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order id={} cancelled", id);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * For each requested item, calls the ExternalProductService (proxy) to get
     * the current price, then builds an OrderDetail with a frozen snapshot.
     */
    private List<OrderDetail> buildDetails(List<OrderDetailRequest> items, Order order) {
        return items.stream().map(item -> {
            // Price consultation via Proxy — DIP: depends on abstraction
            ExternalProductDTO product = externalProductService.findById(item.getProductId());

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal  = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            return OrderDetail.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getTitle())   // snapshot at order time
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

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
