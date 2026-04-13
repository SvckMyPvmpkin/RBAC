package ru.university.rbac.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;
    private boolean revoked = false;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void revoke() {
        this.revoked = true;
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    @Override
    public boolean isActive() {
        if (revoked) return false;

        try {
            String now = LocalDateTime.now().format(FORMATTER);
            return now.compareTo(expiresAt) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void extend(String newExpirationDate) {
        this.expiresAt = newExpirationDate;
        this.revoked = false;
    }

    public boolean isExpired() {
        return !isActive();
    }

    @Override
    public String summary() {
        String baseSummary = super.summary();
        return baseSummary + "\nStatus: " + (isActive() ? "ACTIVE" : "INACTIVE") +
                "\nExpires at: " + expiresAt + " (Auto-renew: " + autoRenew + ")";
    }
}