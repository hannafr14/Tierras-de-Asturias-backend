package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.CreateOrderRequestDto;
import com.tierrasdeasturias.backend.dtos.OrderResponseDto;
import com.tierrasdeasturias.backend.entities.CustomerOrder;
import com.tierrasdeasturias.backend.services.OrderService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;

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
}
