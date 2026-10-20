package com.tierrasdeasturias.backend.controllers;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.tierrasdeasturias.backend.dtos.CreateOrderRequestDto;
import com.tierrasdeasturias.backend.dtos.DeliveryDto;
import com.tierrasdeasturias.backend.dtos.OrderItemRequestDto;
import com.tierrasdeasturias.backend.entities.CustomerOrder;
import com.tierrasdeasturias.backend.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.tierrasdeasturias.backend.config.SecurityConfig;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.tierrasdeasturias.backend.config.RememberMeTestConfig;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.tierrasdeasturias.backend.dtos.OrderResponseDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, RememberMeTestConfig.class})
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void createOrderReturnsCreatedOrder() throws Exception {
        CustomerOrder order = new CustomerOrder();

        setField(order, "id", 1L);
        order.setOrderNumber("ORD-1");
        order.setStatus("PENDING");
        order.setTotal(12.5);
        order.setOrderDate(LocalDateTime.of(2026, 10, 12, 10, 30));

        when(orderService.createOrder(any(CreateOrderRequestDto.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidOrderRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-1"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(12.5))
                .andExpect(jsonPath("$.orderDate").value("2026-10-12T10:30:00"));
    }

    @Test
    void createOrderReturnsBadRequestForInvalidOrderData() throws Exception {
        CreateOrderRequestDto request = createValidOrderRequest();

        request.getDelivery().setPostalCode("33");

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequestDto.class));
    }

    @Test
    void getMyOrdersRejectsAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/orders/mine"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void getMyOrdersUsesAuthenticatedEmail() throws Exception {
        var pageable = PageRequest.of(0, 3);
        var order = new OrderResponseDto(
                18L, "ORD-18", "PENDING", 3.3,
                LocalDateTime.of(2026, 10, 19, 14, 8), 3
        );

        when(orderService.getUserOrders("ana@example.com", pageable))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        mockMvc.perform(get("/api/orders/mine")
                        .with(user("ana@example.com"))
                        .param("email", "other@example.com")
                        .param("userId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(18))
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-18"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].total").value(3.3))
                .andExpect(jsonPath("$.content[0].productCount").value(3))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(3))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        verify(orderService).getUserOrders("ana@example.com", pageable);
    }

    @Test
    void getMyOrdersReturnsEmptyHistory() throws Exception {
        var pageable = PageRequest.of(0, 3);
        when(orderService.getUserOrders("ana@example.com", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/api/orders/mine")
                        .with(user("ana@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    void getMyOrdersReturnsRequestedPage() throws Exception {
        var pageable = PageRequest.of(1, 3);
        var order = new OrderResponseDto(
                19L, "ORD-19", "PENDING", 12.5,
                LocalDateTime.of(2026, 10, 19, 15, 0), 2
        );
        when(orderService.getUserOrders("ana@example.com", pageable))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 4));

        mockMvc.perform(get("/api/orders/mine")
                        .with(user("ana@example.com"))
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-19"))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.page.totalPages").value(2));

        verify(orderService).getUserOrders("ana@example.com", pageable);
    }

    @Test
    void getMyOrdersRejectsNegativePage() throws Exception {
        mockMvc.perform(get("/api/orders/mine")
                        .with(user("ana@example.com"))
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    private CreateOrderRequestDto createValidOrderRequest() {
        DeliveryDto delivery = new DeliveryDto();
        delivery.setFirstName("Ana");
        delivery.setLastName("Garcia");
        delivery.setCity("Oviedo");
        delivery.setPostalCode("33001");
        delivery.setStreetAddress("Calle Mayor 12");
        delivery.setApartment("Piso 2");
        delivery.setPhone("+34 600 000 000");

        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setProductId(1L);
        item.setName("Queso Gamoneu");
        item.setQuantity(1);
        item.setUnitPrice(12.5);

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setDelivery(delivery);
        request.setItems(List.of(item));
        request.setSubtotal(12.5);
        request.setShipping(0.0);
        request.setTaxes(2.17);
        request.setTotal(12.5);

        return request;
    }
}
