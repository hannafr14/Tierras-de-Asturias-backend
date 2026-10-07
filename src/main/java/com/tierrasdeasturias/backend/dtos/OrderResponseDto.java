package com.tierrasdeasturias.backend.dtos;

public class OrderResponseDto {
    private Long id;
    private String orderNumber;
    private String status;
    private Double total;

    public OrderResponseDto(Long id, String orderNumber, String status, Double total) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.status = status;
        this.total = total;
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
}
