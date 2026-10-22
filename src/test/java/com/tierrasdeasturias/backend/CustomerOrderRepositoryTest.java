package com.tierrasdeasturias.backend;

import com.tierrasdeasturias.backend.repositories.CustomerOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tierrasdeasturias.backend.entities.CustomerOrder;
import com.tierrasdeasturias.backend.entities.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class CustomerOrderRepositoryTest {

    @Autowired
    private CustomerOrderRepository orderRepository;

    @Test
    void returnsEmptyPageWhenCustomerHasNoOrders() {
        var result = orderRepository
                .findByUser_EmailOrderByOrderDateDescIdDesc(
                        "empty@example.com", PageRequest.of(0, 3)
                );

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Autowired
    private EntityManager entityManager;

    @Test
    void returnsOnlyOrdersBelongingToRequestedCustomer() {
        var ana = saveUser("ana@example.com");
        var maria = saveUser("maria@example.com");

        saveOrder(ana, "ORD-ANA-1");
        saveOrder(maria, "ORD-MARIA");
        saveOrder(ana, "ORD-ANA-2");
        saveOrder(null, "ORD-GUEST");
        entityManager.flush();
        entityManager.clear();

        var anaOrders = orderRepository
                .findByUser_EmailOrderByOrderDateDescIdDesc(
                        "ana@example.com", PageRequest.of(0, 3)
                );
        var mariaOrders = orderRepository
                .findByUser_EmailOrderByOrderDateDescIdDesc(
                        "maria@example.com", PageRequest.of(0, 3)
                );

        assertEquals(2, anaOrders.getTotalElements());
        assertEquals(List.of("ORD-ANA-2", "ORD-ANA-1"),
                anaOrders.getContent().stream()
                        .map(CustomerOrder::getOrderNumber).toList());
        assertEquals(List.of("ORD-MARIA"),
                mariaOrders.getContent().stream()
                        .map(CustomerOrder::getOrderNumber).toList());
    }

    @Test
    void findsOrderOnlyForItsOwner() {
        var ana = saveUser("ana@example.com");
        saveUser("maria@example.com");
        var order = saveOrder(ana, "ORD-ANA");
        entityManager.flush();
        entityManager.clear();

        var ownOrder = orderRepository
                .findByIdAndUser_Email(order.getId(), "ana@example.com")
                .orElseThrow();

        assertEquals("ORD-ANA", ownOrder.getOrderNumber());
        assertTrue(orderRepository
                .findByIdAndUser_Email(order.getId(), "maria@example.com")
                .isEmpty());
    }

    @Test
    void doesNotFindGuestOrMissingOrderForCustomer() {
        saveUser("ana@example.com");
        var guestOrder = saveOrder(null, "ORD-GUEST");
        entityManager.flush();
        entityManager.clear();

        assertTrue(orderRepository
                .findByIdAndUser_Email(guestOrder.getId(), "ana@example.com")
                .isEmpty());
        assertTrue(orderRepository
                .findByIdAndUser_Email(Long.MAX_VALUE, "ana@example.com")
                .isEmpty());
    }

    @Test
    void paginatesCustomerOrdersWithStableSorting() {
        var ana = saveUser("ana@example.com");
        var maria = saveUser("maria@example.com");

        var oldest = saveOrder(ana, "ORD-1");
        oldest.setOrderDate(LocalDateTime.of(2026, 10, 18, 12, 0));

        var newest = saveOrder(ana, "ORD-2");
        newest.setOrderDate(LocalDateTime.of(2026, 10, 20, 12, 0));

        saveOrder(ana, "ORD-3");
        saveOrder(ana, "ORD-4");
        saveOrder(maria, "ORD-MARIA");
        saveOrder(null, "ORD-GUEST");
        entityManager.flush();
        entityManager.clear();

        var firstPage = orderRepository
                .findByUser_EmailOrderByOrderDateDescIdDesc(
                        "ana@example.com", PageRequest.of(0, 3)
                );
        var secondPage = orderRepository
                .findByUser_EmailOrderByOrderDateDescIdDesc(
                        "ana@example.com", PageRequest.of(1, 3)
                );

        assertEquals(4, firstPage.getTotalElements());
        assertEquals(4, secondPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(2, secondPage.getTotalPages());
        assertEquals(0, firstPage.getNumber());
        assertEquals(1, secondPage.getNumber());
        assertTrue(firstPage.isFirst());
        assertTrue(secondPage.isLast());

        assertEquals(List.of("ORD-2", "ORD-4", "ORD-3"),
                firstPage.getContent().stream()
                        .map(CustomerOrder::getOrderNumber).toList());
        assertEquals(List.of("ORD-1"),
                secondPage.getContent().stream()
                        .map(CustomerOrder::getOrderNumber).toList());
    }

    private User saveUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setPassword("unused-test-value");
        entityManager.persist(user);
        return user;
    }

    private CustomerOrder saveOrder(User owner, String number) {
        var order = new CustomerOrder();
        order.setUser(owner);
        order.setOrderNumber(number);
        order.setOrderDate(LocalDateTime.of(2026, 10, 19, 12, 0));
        order.setStatus("PENDING");
        order.setTotal(3.3);
        entityManager.persist(order);
        return order;
    }
}
