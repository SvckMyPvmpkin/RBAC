package ru.university.rbac.model;

import org.w3c.dom.ls.LSOutput;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String currentTime = LocalDateTime.now().format(FORMATTER);

        String finalReason;

        if (reason == null || reason.isBlank()) {
            finalReason = "Причина не указана";
        } else {
            finalReason = reason;
        }

        if (assignedBy == null || assignedBy.isBlank()) {
            throw new IllegalArgumentException("Роль должна быть кем-то назначена");
        }

        return new AssignmentMetadata(assignedBy, currentTime, finalReason);
    }

    public String format() {
        return String.format("Назначен %s в %s. Причина: %s", assignedBy, assignedAt, reason);
    }
}
