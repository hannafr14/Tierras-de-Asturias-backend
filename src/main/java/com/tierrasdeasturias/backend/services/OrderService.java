package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.dtos.CreateOrderRequestDto;
import com.tierrasdeasturias.backend.dtos.OrderItemRequestDto;
import com.tierrasdeasturias.backend.entities.CustomerOrder;
import com.tierrasdeasturias.backend.entities.OrderItem;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.repositories.CustomerOrderRepository;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import com.tierrasdeasturias.backend.repositories.UserRepository;
import com.tierrasdeasturias.backend.exceptions.InvalidCredentialsException;

import com.tierrasdeasturias.backend.dtos.OrderResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderService {
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            CustomerOrderRepository customerOrderRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.customerOrderRepository = customerOrderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public CustomerOrder createOrder(CreateOrderRequestDto request) {
        return createOrder(request, null);
    }

    public CustomerOrder createOrder(
            CreateOrderRequestDto request,
            String userEmail
    ) {
        CustomerOrder order = new CustomerOrder();

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() ->
                            new InvalidCredentialsException("Authenticated user not found"));
            order.setUser(user);
        }

        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        order.setSubtotal(request.getSubtotal());
        order.setShipping(request.getShipping());
        order.setTaxes(request.getTaxes());
        order.setTotal(request.getTotal());

        order.setFirstName(request.getDelivery().getFirstName());
        order.setLastName(request.getDelivery().getLastName());
        order.setCity(request.getDelivery().getCity());
        order.setPostalCode(request.getDelivery().getPostalCode());
        order.setStreetAddress(request.getDelivery().getStreetAddress());
        order.setApartment(request.getDelivery().getApartment());
        order.setPhone(request.getDelivery().getPhone());

        for (OrderItemRequestDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(itemDto.getUnitPrice());

            order.getItems().add(orderItem);
        }

        return customerOrderRepository.save(order);
    }

    public Page<OrderResponseDto> getUserOrders(
            String userEmail,
            Pageable pageable
    ) {
        return customerOrderRepository
                .findByUser_EmailOrderByOrderDateDescIdDesc(userEmail, pageable)
                .map(order -> new OrderResponseDto(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getStatus(),
                        order.getTotal(),
                        order.getOrderDate()
                ));
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
