package ru.university.rbac.service;

import ru.university.rbac.model.User;
import ru.university.rbac.model.Role;
import ru.university.rbac.model.Permission;
import ru.university.rbac.model.RoleAssignment;
import ru.university.rbac.filter.AssignmentFilter;
import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new HashMap<>();

    @Override
    public void add(RoleAssignment assignment) {
        boolean duplicate = assignments.values().stream()
                .filter(a -> a.user().equals(assignment.user()))
                .filter(a -> a.role().equals(assignment.role()))
                .anyMatch(RoleAssignment::isActive);

        if (duplicate) {
            throw new IllegalStateException("Данная роль уже активно назначена этому пользователю");
        }
        assignments.put(assignment.assignmentId(), assignment);
    }

    public Set<Permission> getUserPermissions(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .filter(RoleAssignment::isActive)
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName)
                        && p.resource().equalsIgnoreCase(resource));
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        return assignments.values().stream().filter(filter::test).toList();
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream().filter(RoleAssignment::isActive).toList();
    }

    @Override public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override public boolean remove(RoleAssignment a) {
        return assignments.remove(a.assignmentId()) != null;
    }

    @Override public int count() {
        return assignments.size();
    }

    @Override public void clear() {
        assignments.clear();
    }
}