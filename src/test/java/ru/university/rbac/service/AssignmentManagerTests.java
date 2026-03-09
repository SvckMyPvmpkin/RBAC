package ru.university.rbac.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.university.rbac.model.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentManagerTests {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    public void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
    }

    @Test
    public void testAddAssignmentAndDuplicateProtection() {
        User user = User.create("ivan", "Ivan Ivanov", "ivan@company.com");
        userManager.add(user);

        Role role = new Role("Manager", "Manager role");
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignment1 = new PermanentAssignment(user, role, meta);

        assignmentManager.add(assignment1);
        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.userHasRole(user, role));

        PermanentAssignment assignment2 = new PermanentAssignment(user, role, meta);

        assertThrows(IllegalStateException.class, () -> {
            assignmentManager.add(assignment2);
        });
    }

    @Test
    public void testValidationFailsIfUserOrRoleDoesNotExist() {
        User ghostUser = User.create("ghost", "Ghost", "ghost@company.com");
        Role ghostRole = new Role("GhostRole", "Desc");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignment = new PermanentAssignment(ghostUser, ghostRole, meta);

        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.add(assignment);
        });
    }

    @Test
    public void testGetUserPermissionsAggregation() {
        User user = User.create("ivan", "Ivan Ivanov", "ivan@company.com");
        userManager.add(user);

        Permission readUsers = new Permission("READ", "users", "Read users");
        Role role1 = new Role("Viewer", "Viewer");
        role1.addPermission(readUsers);
        roleManager.add(role1);

        Permission deleteReports = new Permission("DELETE", "reports", "Delete reports");
        Role role2 = new Role("ReportManager", "Report Mgr");
        role2.addPermission(deleteReports);
        roleManager.add(role2);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        assignmentManager.add(new PermanentAssignment(user, role1, meta));
        assignmentManager.add(new PermanentAssignment(user, role2, meta));

        Set<Permission> allPermissions = assignmentManager.getUserPermissions(user);

        assertEquals(2, allPermissions.size());
        assertTrue(allPermissions.contains(readUsers));
        assertTrue(allPermissions.contains(deleteReports));
    }
}