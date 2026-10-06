package com.tierrasdeasturias.backend.dtos;

public class DeliveryDto {
    private String firstName;
    private String lastName;
    private String city;
    private String postalCode;
    private String streetAddress;
    private String apartment;
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
