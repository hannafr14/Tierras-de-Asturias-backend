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

@Service
public class OrderService {
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductRepository productRepository;

    public OrderService(
            CustomerOrderRepository customerOrderRepository,
            ProductRepository productRepository
    ) {
        this.customerOrderRepository = customerOrderRepository;
        this.productRepository = productRepository;
    }

    public CustomerOrder createOrder(CreateOrderRequestDto request) {
        CustomerOrder order = new CustomerOrder();

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

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
