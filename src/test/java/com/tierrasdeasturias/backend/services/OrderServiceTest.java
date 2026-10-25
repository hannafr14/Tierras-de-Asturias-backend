package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.dtos.CreateOrderRequestDto;
import com.tierrasdeasturias.backend.dtos.DeliveryDto;
import com.tierrasdeasturias.backend.dtos.OrderDeliveryResponseDto;
import com.tierrasdeasturias.backend.dtos.OrderDetailsResponseDto;
import com.tierrasdeasturias.backend.dtos.OrderItemRequestDto;
import com.tierrasdeasturias.backend.dtos.OrderItemResponseDto;
import com.tierrasdeasturias.backend.entities.CustomerOrder;
import com.tierrasdeasturias.backend.entities.OrderItem;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.entities.User;
import com.tierrasdeasturias.backend.exceptions.InvalidCredentialsException;
import com.tierrasdeasturias.backend.repositories.CustomerOrderRepository;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import com.tierrasdeasturias.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private CustomerOrderRepository orderRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private OrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(CustomerOrderRepository.class);
        productRepository = mock(ProductRepository.class);
        userRepository = mock(UserRepository.class);
        service = new OrderService(
                orderRepository, productRepository, userRepository
        );
    }

    @Test
    void createGuestOrderSavesDeliveryAndAllItems() {
        var request = validOrderRequest();
        var firstProduct = new Product();
        firstProduct.setPrice(12.5);
        var secondProduct = new Product();
        secondProduct.setPrice(4.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(firstProduct));
        when(productRepository.findById(2L)).thenReturn(Optional.of(secondProduct));
        var savedOrder = new CustomerOrder();
        when(orderRepository.save(any(CustomerOrder.class))).thenReturn(savedOrder);

        var beforeCreation = LocalDateTime.now();
        var result = service.createOrder(request);
        var afterCreation = LocalDateTime.now();

        var captor = ArgumentCaptor.forClass(CustomerOrder.class);
        verify(orderRepository).save(captor.capture());
        var order = captor.getValue();
        assertSame(savedOrder, result);
        assertNull(order.getUser());
        assertEquals("PENDING", order.getStatus());
        assertTrue(order.getOrderNumber().matches("ORD-[0-9]+"));
        assertTrue(!order.getOrderDate().isBefore(beforeCreation));
        assertTrue(!order.getOrderDate().isAfter(afterCreation));

        assertEquals("Ana", order.getFirstName());
        assertEquals("Garcia", order.getLastName());
        assertEquals("Oviedo", order.getCity());
        assertEquals("33001", order.getPostalCode());
        assertEquals("Calle Mayor 12", order.getStreetAddress());
        assertEquals("2A", order.getApartment());
        assertEquals("+34612345678", order.getPhone());
        assertEquals(29.0, order.getSubtotal());
        assertEquals(3.0, order.getShipping());
        assertEquals(0.0, order.getTaxes());
        assertEquals(32.0, order.getTotal());

        assertEquals(2, order.getItems().size());
        var firstItem = order.getItems().get(0);
        assertSame(order, firstItem.getOrder());
        assertSame(firstProduct, firstItem.getProduct());
        assertEquals(2, firstItem.getQuantity());
        assertEquals(12.5, firstItem.getUnitPrice());
        var secondItem = order.getItems().get(1);
        assertSame(order, secondItem.getOrder());
        assertSame(secondProduct, secondItem.getProduct());
        assertEquals(1, secondItem.getQuantity());
        assertEquals(4.0, secondItem.getUnitPrice());
        verify(productRepository).findById(1L);
        verify(productRepository).findById(2L);
        verifyNoInteractions(userRepository);
    }

    @Test
    void createAuthenticatedOrderLinksTheUserFoundByEmail() {
        var request = validOrderRequest();
        var user = new User();
        user.setEmail("ana@example.com");
        when(userRepository.findByEmail("ana@example.com"))
                .thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findById(2L)).thenReturn(Optional.of(new Product()));
        var savedOrder = new CustomerOrder();
        when(orderRepository.save(any(CustomerOrder.class))).thenReturn(savedOrder);

        var result = service.createOrder(request, "ana@example.com");

        var captor = ArgumentCaptor.forClass(CustomerOrder.class);
        verify(orderRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
        assertEquals(2, captor.getValue().getItems().size());
        assertSame(savedOrder, result);
        verify(userRepository).findByEmail("ana@example.com");
    }

    @Test
    void createOrderDoesNotSaveWhenAuthenticatedUserIsMissing() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> service.createOrder(validOrderRequest(), "missing@example.com"));

        verify(userRepository).findByEmail("missing@example.com");
        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void createOrderDoesNotSavePartialOrderWhenLaterProductIsMissing() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.createOrder(validOrderRequest()));

        verify(productRepository).findById(1L);
        verify(productRepository).findById(2L);
        verify(orderRepository, never()).save(any(CustomerOrder.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserOrdersSumsItemQuantities() {

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

    @Test
    void getUserOrderReturnsFullDetailsWithStoredItemPrices() {
        var orderDate = LocalDateTime.of(2026, 10, 20, 10, 30);
        var order = spy(new CustomerOrder());
        when(order.getId()).thenReturn(18L);
        order.setOrderNumber("ORD-18");
        order.setStatus("PENDING");
        order.setOrderDate(orderDate);
        order.setSubtotal(29.0);
        order.setShipping(3.0);
        order.setTaxes(4.0);
        order.setTotal(36.0);
        order.setFirstName("Ana");
        order.setLastName("Garcia");
        order.setCity("Oviedo");
        order.setPostalCode("33001");
        order.setStreetAddress("Calle Mayor 12");
        order.setApartment("2A");
        order.setPhone("+34612345678");

        var firstProduct = spy(new Product());
        when(firstProduct.getId()).thenReturn(1L);
        firstProduct.setName("Queso");
        firstProduct.setImage("queso.jpg");
        firstProduct.setPrice(99.0);
        var firstItem = new OrderItem();
        firstItem.setOrder(order);
        firstItem.setProduct(firstProduct);
        firstItem.setQuantity(2);
        firstItem.setUnitPrice(12.5);

        var secondProduct = spy(new Product());
        when(secondProduct.getId()).thenReturn(2L);
        secondProduct.setName("Yogur");
        secondProduct.setImage("yogur.jpg");
        secondProduct.setPrice(8.0);
        var secondItem = new OrderItem();
        secondItem.setOrder(order);
        secondItem.setProduct(secondProduct);
        secondItem.setQuantity(1);
        secondItem.setUnitPrice(4.0);
        order.getItems().addAll(List.of(firstItem, secondItem));

        when(orderRepository.findByIdAndUser_Email(18L, "ana@example.com"))
                .thenReturn(Optional.of(order));

        var result = service.getUserOrder(18L, "ana@example.com");

        var expected = new OrderDetailsResponseDto(
                18L, "ORD-18", "PENDING", orderDate,
                29.0, 3.0, 4.0, 36.0,
                List.of(
                        new OrderItemResponseDto(1L, "Queso", "queso.jpg", 2, 12.5),
                        new OrderItemResponseDto(2L, "Yogur", "yogur.jpg", 1, 4.0)
                ),
                new OrderDeliveryResponseDto(
                        "Ana", "Garcia", "Oviedo", "33001",
                        "Calle Mayor 12", "2A", "+34612345678"
                )
        );
        assertEquals(expected, result);
        verify(orderRepository).findByIdAndUser_Email(18L, "ana@example.com");
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(productRepository, userRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "999, ana@example.com",
            "18, other@example.com"
    })
    void getUserOrderThrowsNotFoundWhenOwnerScopedLookupIsEmpty(Long id, String email) {
        when(orderRepository.findByIdAndUser_Email(id, email))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ResponseStatusException.class,
                () -> service.getUserOrder(id, email));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pedido no encontrado", exception.getReason());
        verify(orderRepository).findByIdAndUser_Email(id, email);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(productRepository, userRepository);
    }

    private CreateOrderRequestDto validOrderRequest() {
        var delivery = new DeliveryDto();
        delivery.setFirstName("Ana");
        delivery.setLastName("Garcia");
        delivery.setCity("Oviedo");
        delivery.setPostalCode("33001");
        delivery.setStreetAddress("Calle Mayor 12");
        delivery.setApartment("2A");
        delivery.setPhone("+34612345678");

        var firstItem = new OrderItemRequestDto();
        firstItem.setProductId(1L);
        firstItem.setName("Queso");
        firstItem.setQuantity(2);
        firstItem.setUnitPrice(12.5);
        var secondItem = new OrderItemRequestDto();
        secondItem.setProductId(2L);
        secondItem.setName("Yogur");
        secondItem.setQuantity(1);
        secondItem.setUnitPrice(4.0);

        var request = new CreateOrderRequestDto();
        request.setDelivery(delivery);
        request.setItems(List.of(firstItem, secondItem));
        request.setSubtotal(29.0);
        request.setShipping(3.0);
        request.setTaxes(0.0);
        request.setTotal(32.0);
        return request;
    }
}
