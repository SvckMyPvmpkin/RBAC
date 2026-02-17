package ru.university.rbac.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    @Override
    public boolean isActive() {
        String now = LocalDateTime.now().format(FORMATTER);
        return now.compareTo(expiresAt) < 0;
    }

    public void extend(String newExpirationDate) {
        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        return !isActive();
    }

    @Override
    public String summary() {
        String baseSummary = super.summary();
        return baseSummary + "\nExpires at: " + expiresAt + " (Auto-renew: " + autoRenew + ")";
    }
}
