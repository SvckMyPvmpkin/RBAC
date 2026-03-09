package ru.university.rbac.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AuditLogTests {

    private AuditLog auditLog;

    @BeforeEach
    public void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    public void testLogEntryAddition() {
        auditLog.log("LOGIN", "admin", "system", "Успешный вход");
        List<AuditLog.AuditEntry> entries = auditLog.getAll();

        assertEquals(1, entries.size());
        assertEquals("LOGIN", entries.get(0).action());
        assertEquals("admin", entries.get(0).performer());
    }

    @Test
    public void testFilterByPerformer() {
        auditLog.log("LOGIN", "admin", "system", "...");
        auditLog.log("CREATE", "manager", "user1", "...");

        List<AuditLog.AuditEntry> adminLogs = auditLog.getByPerformer("admin");

        assertEquals(1, adminLogs.size());
        assertEquals("admin", adminLogs.get(0).performer());
    }

    @Test
    public void testFilterByAction() {
        auditLog.log("LOGIN", "admin", "system", "...");
        auditLog.log("LOGOUT", "admin", "system", "...");

        List<AuditLog.AuditEntry> loginLogs = auditLog.getByAction("LOGIN");

        assertEquals(1, loginLogs.size());
        assertEquals("LOGIN", loginLogs.get(0).action());
    }

    @Test
    public void testSaveToFile() throws Exception {
        String filename = "test_audit.log";
        auditLog.log("TEST", "user", "target", "details");

        auditLog.saveToFile(filename);

        File file = new File(filename);
        assertTrue(file.exists());

        List<String> lines = Files.readAllLines(file.toPath());
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("TEST"));

        file.delete();
    }
}