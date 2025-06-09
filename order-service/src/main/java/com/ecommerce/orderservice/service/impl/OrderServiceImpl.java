
package com.ecommerce.orderservice.service.impl;


import com.ecommerce.orderservice.dto.OrderDTO;
import com.ecommerce.orderservice.dto.OrderItemDTO;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderItem;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = mapToEntity(orderDTO);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // Calculate total amount as if not given correctly
        BigDecimal totalAmount = order.getItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Publish order created event
        OrderDTO event = mapToDTO(savedOrder);
        kafkaTemplate.send("order.created", savedOrder.getId().toString(), event);
        log.info("Order created with ID: {}", savedOrder.getId());

        return mapToDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        return mapToDTO(order);
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByCustomerId(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Publish order status updated event
        kafkaTemplate.send("order.status.updated", updatedOrder.getId().toString(),
                mapToDTO(updatedOrder));
        log.info("Order status updated for ID: {} to {}", updatedOrder.getId(), status);

        return mapToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel order that is already shipped or delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Publish order cancelled event
        kafkaTemplate.send("order.cancelled", order.getId().toString(), mapToDTO(order));
        log.info("Order cancelled with ID: {}", order.getId());
    }

    private OrderDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

        return new OrderDTO(
                order.getId(),
                order.getCustomerId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                itemDTOs,
                order.getShippingAddress(),
                order.getBillingAddress()
        );
    }

    private Order mapToEntity(OrderDTO orderDTO) {
        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setCustomerId(orderDTO.getCustomerId());
        order.setOrderDate(orderDTO.getOrderDate());
        order.setStatus(orderDTO.getStatus());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setBillingAddress(orderDTO.getBillingAddress());

        List<OrderItem> items = orderDTO.getItems().stream()
                .map(itemDTO -> mapItemToEntity(itemDTO, order))
                .collect(Collectors.toList());
        order.setItems(items);

        return order;
    }

    private OrderItemDTO mapItemToDTO(OrderItem item) {
        return new OrderItemDTO(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }

    private OrderItem mapItemToEntity(OrderItemDTO itemDTO, Order order) {
        OrderItem item = new OrderItem();
        item.setId(itemDTO.getId());
        item.setOrder(order);
        item.setProductId(itemDTO.getProductId());
        item.setProductName(itemDTO.getProductName());
        item.setQuantity(itemDTO.getQuantity());
        item.setUnitPrice(itemDTO.getUnitPrice());
        item.setTotalPrice(itemDTO.getTotalPrice());
        return item;
    }
}
