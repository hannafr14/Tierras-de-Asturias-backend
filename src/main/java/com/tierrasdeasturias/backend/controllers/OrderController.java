package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.CreateOrderRequestDto;
import com.tierrasdeasturias.backend.services.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public String createOrder(@RequestBody CreateOrderRequestDto request) {
        orderService.createOrder(request);
        return "Order created";
    }
}
