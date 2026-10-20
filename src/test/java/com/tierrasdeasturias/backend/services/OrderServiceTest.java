package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.entities.CustomerOrder;
import com.tierrasdeasturias.backend.entities.OrderItem;
import com.tierrasdeasturias.backend.repositories.CustomerOrderRepository;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import com.tierrasdeasturias.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Test
    void getUserOrdersSumsItemQuantities() {
        var orderRepository = mock(CustomerOrderRepository.class);
        var productRepository = mock(ProductRepository.class);
        var userRepository = mock(UserRepository.class);
        var service = new OrderService(
                orderRepository, productRepository, userRepository
        );

        var order = new CustomerOrder();
        order.setOrderNumber("ORD-18");

        var firstItem = new OrderItem();
        firstItem.setQuantity(2);
        var secondItem = new OrderItem();
        secondItem.setQuantity(1);
        order.getItems().addAll(List.of(firstItem, secondItem));

        var pageable = PageRequest.of(0, 3);
        when(orderRepository.findByUser_EmailOrderByOrderDateDescIdDesc(
                "ana@example.com", pageable
        )).thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        var result = service.getUserOrders("ana@example.com", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("ORD-18", result.getContent().getFirst().getOrderNumber());
        assertEquals(3, result.getContent().getFirst().getProductCount());
        verify(orderRepository).findByUser_EmailOrderByOrderDateDescIdDesc(
                "ana@example.com", pageable
        );
    }
}
