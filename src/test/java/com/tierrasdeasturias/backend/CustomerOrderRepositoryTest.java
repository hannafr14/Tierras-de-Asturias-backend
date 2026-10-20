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

    private User saveUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setPassword("unused-test-value");
        entityManager.persist(user);
        return user;
    }

    private void saveOrder(User owner, String number) {
        var order = new CustomerOrder();
        order.setUser(owner);
        order.setOrderNumber(number);
        order.setOrderDate(LocalDateTime.of(2026, 10, 19, 12, 0));
        order.setStatus("PENDING");
        order.setTotal(3.3);
        entityManager.persist(order);
    }
}
