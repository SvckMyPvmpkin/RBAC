package ru.university.rbac.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTests {

    @Test
    public void testUsernameValidation() {
        assertTrue(ValidationUtils.isValidUsername("ivan_123"));
        assertFalse(ValidationUtils.isValidUsername("iv")); // слишком короткий
        assertFalse(ValidationUtils.isValidUsername("ivan@name")); // спецсимвол
    }

    @Test
    public void testEmailValidation() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertFalse(ValidationUtils.isValidEmail("test-example.com")); // нет @
    }

    @Test
    public void testDateValidation() {
        assertTrue(ValidationUtils.isValidDate("2026-02-15"));
        assertFalse(ValidationUtils.isValidDate("15-02-2026")); // неверный формат
    }

    @Test
    public void testNormalizeString() {
        assertEquals("Hello World", ValidationUtils.normalizeString("  Hello   World  "));
    }

    @Test
    public void testRequireNonEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty("  ", "Name"));
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("Valid", "Name"));
    }
}