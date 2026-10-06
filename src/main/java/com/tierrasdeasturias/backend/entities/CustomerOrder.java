package com.tierrasdeasturias.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String status;

    private Double subtotal;
    private Double shipping;
    private Double taxes;
    private Double total;

    private String firstName;
    private String lastName;
    private String city;
    private String postalCode;
    private String streetAddress;
    private String apartment;
    private String phone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public Double getShipping() {
        return shipping;
    }

    public Double getTaxes() {
        return taxes;
    }

    public Double getTotal() {
        return total;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getApartment() {
        return apartment;
    }

    public String getPhone() {
        return phone;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public void setShipping(Double shipping) {
        this.shipping = shipping;
    }

    public void setTaxes(Double taxes) {
        this.taxes = taxes;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
