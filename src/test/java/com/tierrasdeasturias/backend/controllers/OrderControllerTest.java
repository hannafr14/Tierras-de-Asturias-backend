package com.tierrasdeasturias.backend.controllers;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
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
import static org.mockito.ArgumentMatchers.argThat;
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.tierrasdeasturias.backend.dtos.OrderDetailsResponseDto;
import com.tierrasdeasturias.backend.dtos.OrderDeliveryResponseDto;
import com.tierrasdeasturias.backend.dtos.OrderItemResponseDto;

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

    @Test
    void getMyOrderRejectsAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/orders/mine/19"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @ParameterizedTest
    @ValueSource(longs = {19L, 999999L})
    void getMyOrderReturnsNotFoundForUnavailableOrder(Long id)
            throws Exception {
        when(orderService.getUserOrder(id, "maria@example.com"))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        mockMvc.perform(get("/api/orders/mine/{id}", id)
                        .with(user("maria@example.com"))
                        .param("email", "ana@example.com"))
                .andExpect(status().isNotFound());

        verify(orderService).getUserOrder(id, "maria@example.com");
    }

    @Test
    void getMyOrderReturnsDetailsForAuthenticatedCustomer() throws Exception {
        var item = new OrderItemResponseDto(
                1L, "Queso", "/images/queso.jpg", 2, 12.5
        );
        var delivery = new OrderDeliveryResponseDto(
                "Ana", "Garcia", "Oviedo", "33001",
                "Calle Mayor 12", "Piso 2", "+34 600 000 000"
        );
        var order = new OrderDetailsResponseDto(
                19L, "ORD-19", "PENDING",
                LocalDateTime.of(2026, 10, 19, 15, 0),
                25.0, 0.0, 4.34, 25.0,
                List.of(item), delivery
        );
        when(orderService.getUserOrder(19L, "ana@example.com"))
                .thenReturn(order);

        mockMvc.perform(get("/api/orders/mine/19")
                        .with(user("ana@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(19))
                .andExpect(jsonPath("$.orderNumber").value("ORD-19"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(25.0))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Queso"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(12.5))
                .andExpect(jsonPath("$.delivery.firstName").value("Ana"))
                .andExpect(jsonPath("$.delivery.streetAddress")
                        .value("Calle Mayor 12"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist());

        verify(orderService).getUserOrder(19L, "ana@example.com");
    }

    @ParameterizedTest
    @CsvSource({
            "600000000, +34600000000",
            "+34 600 000 000, +34600000000",
            "600-000-000, +34600000000",
            "912345678, +34912345678"
    })
    void createOrderNormalizesPhoneFromRawJson(String phone, String expected) throws Exception {
        var body = objectMapper.valueToTree(createValidOrderRequest());
        ((ObjectNode) body.get("delivery")).put("phone", phone);
        var order = new CustomerOrder();
        setField(order, "id", 1L);
        order.setOrderNumber("ORD-1");
        order.setStatus("PENDING");
        order.setTotal(12.5);
        order.setOrderDate(LocalDateTime.of(2026, 10, 12, 10, 30));
        when(orderService.createOrder(any(CreateOrderRequestDto.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(orderService).createOrder(argThat(request ->
                expected.equals(request.getDelivery().getPhone())));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ", "123", "60000000", "6000000000", "500000000",
            "+33600000000", "+34576575757", "0034600000000", "34600000000", "++34600000000",
            "600+000000", "60000000a", "6        1", "600000000\n", "600\t000000"
    })
    void createOrderRejectsInvalidPhoneWithoutCallingService(String phone) throws Exception {
        var body = objectMapper.valueToTree(createValidOrderRequest());
        ((ObjectNode) body.get("delivery")).put("phone", phone);

        mockMvc.perform(post("/api/orders").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @ParameterizedTest
    @CsvSource({"city, 100", "streetAddress, 200", "apartment, 100"})
    void createOrderRejectsOversizedAddressFields(String field, int limit) throws Exception {
        var body = objectMapper.valueToTree(createValidOrderRequest());
        ((ObjectNode) body.get("delivery")).put(field, "x".repeat(limit + 1));

        mockMvc.perform(post("/api/orders").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"city", "streetAddress", "postalCode"})
    void createOrderRejectsBlankAddressFields(String field) throws Exception {
        var body = objectMapper.valueToTree(createValidOrderRequest());
        ((ObjectNode) body.get("delivery")).put(field, " \u00a0\uFEFF ");

        mockMvc.perform(post("/api/orders").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrderAcceptsAndTrimsAddressWithoutChangingItsMeaning() throws Exception {
        var body = objectMapper.valueToTree(createValidOrderRequest());
        var delivery = (ObjectNode) body.get("delivery");
        delivery.put("firstName", " Ana ");
        delivery.put("lastName", " Garcia ");
        delivery.put("city", " " + "x".repeat(100) + " ");
        delivery.put("postalCode", " 00301 ");
        delivery.put("streetAddress", " Calle Pe\u00f1a, 12-B / s/n ");
        delivery.put("apartment", "   ");
        var order = new CustomerOrder();
        setField(order, "id", 1L);
        order.setOrderNumber("ORD-1");
        order.setStatus("PENDING");
        order.setTotal(12.5);
        order.setOrderDate(LocalDateTime.of(2026, 10, 12, 10, 30));
        when(orderService.createOrder(any(CreateOrderRequestDto.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(orderService).createOrder(argThat(request ->
                "Ana".equals(request.getDelivery().getFirstName())
                        && "Garcia".equals(request.getDelivery().getLastName())
                        && "x".repeat(100).equals(request.getDelivery().getCity())
                        && "00301".equals(request.getDelivery().getPostalCode())
                        && "Calle Pe\u00f1a, 12-B / s/n".equals(request.getDelivery().getStreetAddress())
                        && "".equals(request.getDelivery().getApartment())));
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
