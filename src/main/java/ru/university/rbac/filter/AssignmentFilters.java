package ru.university.rbac.filter;

import ru.university.rbac.model.Role;
import ru.university.rbac.model.User;
import ru.university.rbac.model.RoleAssignment;

public class AssignmentFilters {
    public static AssignmentFilter byUser(User user) {
        return a -> a.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return a -> a.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return a -> a.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return a -> a.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return a -> !a.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return a -> a.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return a -> a.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        return a -> a.metadata().assignedAt().compareTo(date) > 0;
    }

    public static AssignmentFilter expiringBefore(String date) {
        return a -> a.assignmentType().equals("TEMPORARY") && !a.isActive();
    }
}