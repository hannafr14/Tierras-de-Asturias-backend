package com.tierrasdeasturias.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DeliveryDto {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotBlank
    @Pattern(regexp = "[0-9]{5}")
    private String postalCode;

    @NotBlank
    @Size(max = 200)
    private String streetAddress;

    @Size(max = 100)
    private String apartment;

    @NotBlank
    @Pattern(regexp = "\\+34[6789][0-9]{8}",
            message = "Introduce un tel\u00e9fono espa\u00f1ol de 9 d\u00edgitos que empiece por 6, 7, 8 o 9. El prefijo +34 es opcional.")
    private String phone;

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

    public void setFirstName(String firstName) {
        this.firstName = trim(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = trim(lastName);
    }

    public void setCity(String city) {
        this.city = trim(city);
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = trim(postalCode);
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = trim(streetAddress);
    }

    public void setApartment(String apartment) {
        this.apartment = trim(apartment);
    }

    public void setPhone(String phone) {
        if (phone == null) {
            this.phone = null;
            return;
        }

        String compact = phone.replaceAll("[\\p{Zs}-]", "");
        String national = compact.startsWith("+34") ? compact.substring(3) : compact;
        this.phone = national.matches("[6789][0-9]{8}") ? "+34" + national : compact;
    }

    private static String trim(String value) {
        return value == null ? null
                : value.replaceAll("^[\\s\\p{Z}\\uFEFF]+|[\\s\\p{Z}\\uFEFF]+$", "");
    }
}
