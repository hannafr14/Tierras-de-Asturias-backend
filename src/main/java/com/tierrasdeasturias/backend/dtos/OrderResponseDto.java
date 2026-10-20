package com.tierrasdeasturias.backend.dtos;

import java.time.LocalDateTime;

public class OrderResponseDto {
    private Long id;
    private String orderNumber;
    private String status;
    private Double total;
    private LocalDateTime orderDate;
    private int productCount;

    public OrderResponseDto(
            Long id,
            String orderNumber,
            String status,
            Double total,
            LocalDateTime orderDate,
            int productCount
    ) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.status = status;
        this.total = total;
        this.orderDate = orderDate;
        this.productCount = productCount;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public Double getTotal() {
        return total;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public int getProductCount() {
        return productCount;
    }
}
