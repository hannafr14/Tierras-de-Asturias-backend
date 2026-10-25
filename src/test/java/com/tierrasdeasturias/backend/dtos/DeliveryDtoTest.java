package com.tierrasdeasturias.backend.dtos;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapperImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryDtoTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidation() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidation() {
        factory.close();
    }

    @ParameterizedTest
    @CsvSource({
            "600000000, +34600000000",
            "700000000, +34700000000",
            "812345678, +34812345678",
            "912345678, +34912345678",
            "+34600000000, +34600000000",
            "+34 600 000 000, +34600000000",
            "600-000-000, +34600000000",
            "' +34 600-000-000 ', +34600000000",
            "+34\u00a0600\u202f000\u00a0000, +34600000000"
    })
    void acceptsAndNormalizesSpanishPhone(String input, String expected) {
        var delivery = new DeliveryDto();
        delivery.setPhone(input);

        assertEquals(expected, delivery.getPhone());
        assertTrue(validator.validateProperty(delivery, "phone").isEmpty());
        delivery.setPhone(delivery.getPhone());
        assertEquals(expected, delivery.getPhone());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ", "---", "123", "60000000", "6000000000", "500000000",
            "+33600000000", "+34576575757", "0034600000000", "34600000000", "++34600000000",
            "600+000000", "60000000a", "6        1", "+34", "600000000\n", "600\t000000"
    })
    void rejectsInvalidOrMissingPhone(String input) {
        var delivery = new DeliveryDto();
        delivery.setPhone(input);

        assertFalse(validator.validateProperty(delivery, "phone").isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"city, 100", "streetAddress, 200", "apartment, 100"})
    void enforcesAddressLengthAfterTrimming(String field, int limit) {
        var delivery = new DeliveryDto();
        var bean = new BeanWrapperImpl(delivery);
        for (int length : new int[]{limit - 1, limit}) {
            bean.setPropertyValue(field, " \u00a0" + "a".repeat(length) + "\uFEFF ");
            assertEquals("a".repeat(length), bean.getPropertyValue(field));
            assertTrue(validator.validateProperty(delivery, field).isEmpty());
        }

        bean.setPropertyValue(field, "a".repeat(limit + 1));
        assertFalse(validator.validateProperty(delivery, field).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"city", "streetAddress", "firstName", "lastName"})
    void rejectsMissingOrWhitespaceOnlyRequiredText(String field) {
        var delivery = new DeliveryDto();
        var bean = new BeanWrapperImpl(delivery);
        for (String value : new String[]{null, "", "   ", "\u00a0\uFEFF"}) {
            bean.setPropertyValue(field, value);
            assertFalse(validator.validateProperty(delivery, field).isEmpty());
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void allowsMissingApartment(String apartment) {
        var delivery = new DeliveryDto();
        delivery.setApartment(apartment);

        assertTrue(validator.validateProperty(delivery, "apartment").isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1234", "123456", "33a01", "33-001", "\uFF13\uFF13\uFF10\uFF10\uFF11"})
    void rejectsInvalidPostalCodes(String postalCode) {
        var delivery = new DeliveryDto();
        delivery.setPostalCode(postalCode);

        assertFalse(validator.validateProperty(delivery, "postalCode").isEmpty());
    }

    @Test
    void preservesAddressPunctuationAndPostalLeadingZeros() {
        var delivery = new DeliveryDto();
        delivery.setFirstName(" Ana ");
        delivery.setLastName(" Garcia ");
        delivery.setCity(" L'Entregu ");
        delivery.setStreetAddress(" Calle Pe\u00f1a, 12-B / s/n ");
        delivery.setApartment(" 3\u00ba A ");
        delivery.setPostalCode(" 00301 ");
        delivery.setPhone("600000000");

        assertTrue(validator.validate(delivery).isEmpty());
        assertEquals("Ana", delivery.getFirstName());
        assertEquals("Garcia", delivery.getLastName());
        assertEquals("L'Entregu", delivery.getCity());
        assertEquals("Calle Pe\u00f1a, 12-B / s/n", delivery.getStreetAddress());
        assertEquals("3\u00ba A", delivery.getApartment());
        assertEquals("00301", delivery.getPostalCode());
    }
}
