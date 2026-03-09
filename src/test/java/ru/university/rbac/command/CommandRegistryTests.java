package ru.university.rbac.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.university.rbac.model.*;
import java.io.ByteArrayInputStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

public class CommandRegistryTests {

    private RBACSystem system;
    private CommandParser parser;

    @BeforeEach
    public void setUp() {
        system = new RBACSystem();
        system.initialize();
        parser = new CommandParser();
        CommandRegistry.registerAll(parser);
    }

    @Test
    public void testUserLifecycle() {
        String input = "test_user\nTest Name\ntest@mail.com\n";
        parser.parseAndExecute("user-create", new Scanner(input), system);
        assertTrue(system.getUserManager().exists("test_user"));

        String inputDelete = "test_user\nда\n";
        parser.parseAndExecute("user-delete", new Scanner(inputDelete), system);
        assertFalse(system.getUserManager().exists("test_user"));
    }

    @Test
    public void testRoleLifecycle() {
        String input = "Editor\nDescription\nнет\n";
        parser.parseAndExecute("role-create", new Scanner(input), system);
        assertTrue(system.getRoleManager().exists("Editor"));

        String inputPerm = "Editor\nWRITE\nreports\nSome desc\n";
        parser.parseAndExecute("role-add-permission", new Scanner(inputPerm), system);
        assertTrue(system.getRoleManager().findByName("Editor").get().hasPermission("WRITE", "reports"));
    }

    @Test
    public void testAssignRole() {
        String input = "admin\nViewer\n1\nInitial reason\n";
        parser.parseAndExecute("assign-role", new Scanner(input), system);

        User admin = system.getUserManager().findByUsername("admin").get();
        assertTrue(system.getAssignmentManager().userHasRole(admin, system.getRoleManager().findByName("Viewer").get()));
    }

    @Test
    public void testPermissionsCheck() {
        String input = "admin\nREAD\nusers\n";
        assertDoesNotThrow(() -> parser.parseAndExecute("permissions-check", new Scanner(input), system));
    }

    @Test
    public void testStatsCommand() {
        assertDoesNotThrow(() -> parser.parseAndExecute("stats", new Scanner(""), system));
    }

    @Test
    public void testUserSearch() {
        String input = "1\nadmin\n";
        assertDoesNotThrow(() -> parser.parseAndExecute("user-search", new Scanner(input), system));
    }
}