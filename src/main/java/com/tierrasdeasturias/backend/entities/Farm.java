package com.tierrasdeasturias.backend.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "farms")
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String address;
    private String image;

    private Double latitude;
    private Double longitude;

    @ManyToMany
    @JoinTable(
            name = "farm_farm_categories",
            joinColumns = @JoinColumn(name = "farm_id"),
            inverseJoinColumns = @JoinColumn(name = "farm_category_id")
    )
    private Set<FarmCategory> categories = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getImage() {
        return image;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Set<FarmCategory> getCategories() {
        return categories;
    }
}
