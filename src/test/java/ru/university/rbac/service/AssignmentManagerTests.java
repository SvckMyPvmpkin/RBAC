package ru.university.rbac.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.university.rbac.model.*;
import ru.university.rbac.model.PermanentAssignment;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTests {
    private AssignmentManager assignmentManager;
    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        assignmentManager = new AssignmentManager();
        testUser = User.create("worker", "Worker Name", "work@mail.com");
        testRole = new Role("Dev", "Developer Role");
        testRole.addPermission(new Permission("READ", "code", "Can read code"));
    }

    @Test
    void testAddDuplicateActiveAssignmentThrowsException() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "setup");
        PermanentAssignment a1 = new PermanentAssignment(testUser, testRole, meta);
        PermanentAssignment a2 = new PermanentAssignment(testUser, testRole, meta);

        assignmentManager.add(a1);
        assertThrows(IllegalStateException.class, () -> assignmentManager.add(a2));
    }

    @Test
    void testUserHasPermission() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "setup");
        PermanentAssignment a1 = new PermanentAssignment(testUser, testRole, meta);
        assignmentManager.add(a1);

        assertTrue(assignmentManager.userHasPermission(testUser, "READ", "code"));
        assertFalse(assignmentManager.userHasPermission(testUser, "WRITE", "code"));
    }
}