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

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean
    private OrderService orderService;

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
