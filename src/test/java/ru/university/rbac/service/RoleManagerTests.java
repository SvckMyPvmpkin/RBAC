package ru.university.rbac.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.university.rbac.model.Role;
import ru.university.rbac.model.Permission;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTests {
    private RoleManager roleManager;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
    }

    @Test
    void testAddRoleUniqueName() {
        Role role1 = new Role("Admin", "Desc");
        Role role2 = new Role("Admin", "Desc 2");

        roleManager.add(role1);
        assertThrows(IllegalArgumentException.class, () -> roleManager.add(role2));
    }

    @Test
    void testFindByName() {
        Role role = new Role("Manager", "Desc");
        roleManager.add(role);
        assertTrue(roleManager.findByName("Manager").isPresent());
    }
}