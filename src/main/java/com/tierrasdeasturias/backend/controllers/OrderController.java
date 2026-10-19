package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.CreateOrderRequestDto;
import com.tierrasdeasturias.backend.dtos.OrderResponseDto;
import com.tierrasdeasturias.backend.entities.CustomerOrder;
import com.tierrasdeasturias.backend.services.OrderService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponseDto createOrder(
            @Valid @RequestBody CreateOrderRequestDto request,
            Principal principal
    ) {
        CustomerOrder order = principal == null
                ? orderService.createOrder(request)
                : orderService.createOrder(request, principal.getName());

        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotal(),
                order.getOrderDate()
        );
    }

    @GetMapping("/mine")
    public PagedModel<OrderResponseDto> getMyOrders(
            Principal principal,
            @RequestParam(name = "page", defaultValue = "0") int page
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must not be negative"
            );
        }

        var orders = orderService.getUserOrders(
                principal.getName(),
                PageRequest.of(page, 3)
        );

        return new PagedModel<>(orders);
    }
}
