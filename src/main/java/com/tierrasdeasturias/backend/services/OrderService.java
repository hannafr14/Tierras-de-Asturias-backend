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
import org.springframework.transaction.annotation.Transactional;
import com.tierrasdeasturias.backend.dtos.OrderDetailsResponseDto;
import com.tierrasdeasturias.backend.dtos.OrderDeliveryResponseDto;
import com.tierrasdeasturias.backend.dtos.OrderItemResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional(readOnly = true)
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
                        order.getOrderDate(),
                        order.getItems().stream()
                                .mapToInt(item -> item.getQuantity())
                                .sum()
                ));
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponseDto getUserOrder(Long id, String userEmail) {
        var order = customerOrderRepository
                .findByIdAndUser_Email(id, userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        var items = order.getItems().stream()
                .map(item -> new OrderItemResponseDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImage(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        var delivery = new OrderDeliveryResponseDto(
                order.getFirstName(),
                order.getLastName(),
                order.getCity(),
                order.getPostalCode(),
                order.getStreetAddress(),
                order.getApartment(),
                order.getPhone()
        );

        return new OrderDetailsResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getOrderDate(),
                order.getSubtotal(),
                order.getShipping(),
                order.getTaxes(),
                order.getTotal(),
                items,
                delivery
        );
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
