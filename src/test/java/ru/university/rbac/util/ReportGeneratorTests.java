package ru.university.rbac.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.university.rbac.model.*;
import ru.university.rbac.service.*;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportGeneratorTests {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private ReportGenerator reportGenerator;

    @BeforeEach
    public void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        reportGenerator = new ReportGenerator();

        User user = User.create("test_user", "Test Name", "test@mail.com");
        userManager.add(user);

        Role role = new Role("Admin", "Admin role");
        role.addPermission(new Permission("READ", "users", "Read"));
        roleManager.add(role);

        assignmentManager.add(new PermanentAssignment(user, role, AssignmentMetadata.now("system", "Test")));
    }

    @Test
    public void testGenerateUserReport() {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("test_user"));
        assertTrue(report.contains("Admin"));
    }

    @Test
    public void testGenerateRoleReport() {
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("1"));
    }

    @Test
    public void testGeneratePermissionMatrix() {
        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("test_user"));
        assertTrue(report.contains("users"));
    }
}