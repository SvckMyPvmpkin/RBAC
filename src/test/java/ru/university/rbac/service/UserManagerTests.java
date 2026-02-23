package ru.university.rbac.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.university.rbac.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTests {
    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    void testAddUser() {
        User user = User.create("test_user", "Test Name", "test@mail.com");
        userManager.add(user);
        assertEquals(1, userManager.count());
        assertTrue(userManager.findById("test_user").isPresent());
    }

    @Test
    void testAddDuplicateUserThrowsException() {
        User user = User.create("test_user", "Name", "mail@mail.com");
        userManager.add(user);
        assertThrows(IllegalArgumentException.class, () -> userManager.add(user));
    }

    @Test
    void testUpdateUser() {
        userManager.add(User.create("admin", "Old Name", "old@mail.com"));
        userManager.update("admin", "New Name", "new@mail.com");

        User updated = userManager.findById("admin").get();
        assertEquals("New Name", updated.fullname());
        assertEquals("new@mail.com", updated.email());
    }
}