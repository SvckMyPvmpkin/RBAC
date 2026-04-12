package ru.university.rbac.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.university.rbac.model.*;
import ru.university.rbac.service.*;

import static org.junit.jupiter.api.Assertions.*;

public class ParallelReportGeneratorTest {

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

        for (int i = 0; i < 100; i++) {
            User user = User.create("user_" + i, "User " + i, "user" + i + "@test.com");
            userManager.add(user);

            Role role = new Role("Role_" + i, "Desc");
            role.addPermission(new Permission("READ", "resource_" + i, "desc"));
            roleManager.add(role);

            assignmentManager.add(new PermanentAssignment(user, role, AssignmentMetadata.now("admin", "test")));
        }
    }

    @Test
    public void testParallelUserReport() {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("=== ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ ==="));

        for (int i = 0; i < 100; i++) {
            assertTrue(report.contains("user_" + i), "В отчете отсутствует user_" + i);
        }
    }

    @Test
    public void testParallelPermissionMatrix() {
        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("=== МАТРИЦА ПРАВ (User x Resource) ==="));

        for (int i = 0; i < 100; i++) {
            assertTrue(report.contains("resource_" + i), "В матрице отсутствует resource_" + i);
        }
    }
}