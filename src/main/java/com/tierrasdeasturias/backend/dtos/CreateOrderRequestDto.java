package com.tierrasdeasturias.backend.dtos;

import java.util.List;

public class CreateOrderRequestDto {
    private DeliveryDto delivery;
    private List<OrderItemRequestDto> items;
    private Double subtotal;
    private Double shipping;
    private Double taxes;
    private Double total;

    public DeliveryDto getDelivery() {
        return delivery;
    }

    public List<OrderItemRequestDto> getItems() {
        return items;
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

    public void setDelivery(DeliveryDto delivery) {
        this.delivery = delivery;
    }

    public void setItems(List<OrderItemRequestDto> items) {
        this.items = items;
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
}
