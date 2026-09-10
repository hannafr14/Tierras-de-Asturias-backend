package com.tierrasdeasturias.backend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(length = 1000)
    private String image;
    private Integer quantity;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "product_category_id")
    private ProductCategory category;

    @ManyToOne
    @JoinColumn(name = "farm_id")
    private Farm farm;
    
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }
}
