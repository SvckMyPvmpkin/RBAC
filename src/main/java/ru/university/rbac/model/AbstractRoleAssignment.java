package ru.university.rbac.model;

import ru.university.rbac.model.User;
import ru.university.rbac.model.Role;
import ru.university.rbac.model.AssignmentMetadata;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = "asgn_" + UUID.randomUUID().toString().substring(0, 10);
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() { return assignmentId; }

    @Override
    public User user() { return user; }

    @Override
    public Role role() { return role; }

    @Override
    public AssignmentMetadata metadata() { return metadata; }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    public String summary() {
        String status;

        if (isActive()) {
            status = "ACTIVE";
        } else {
            status = "INACTIVE";
        }

        return String.format(
                "[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s",
                assignmentType(),
                role.getName(),
                user.username(),
                metadata.assignedBy(),
                metadata.assignedAt(),
                metadata.reason(),
                status
        );
    }
}
