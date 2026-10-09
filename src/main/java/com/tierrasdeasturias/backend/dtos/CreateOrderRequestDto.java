package com.tierrasdeasturias.backend.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateOrderRequestDto {
    @Valid
    @NotNull
    private DeliveryDto delivery;

    @Valid
    @NotEmpty
    private List<OrderItemRequestDto> items;

    @NotNull
    @Min(0)
    private Double subtotal;

    @NotNull
    @Min(0)
    private Double shipping;

    @NotNull
    @Min(0)
    private Double taxes;

    @NotNull
    @Min(0)
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
