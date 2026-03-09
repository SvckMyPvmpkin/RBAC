package ru.university.rbac.service;

import ru.university.rbac.model.*;
import ru.university.rbac.filter.AssignmentFilter;
import java.util.*;
import java.util.stream.Collectors;
import ru.university.rbac.util.ValidationUtils;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new HashMap<>();

    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        ValidationUtils.requireNonEmpty(assignment.assignmentId(), "Assignment ID");
        if (!userManager.exists(assignment.user().username())) {
            throw new IllegalArgumentException("Ошибка: Пользователь не существует в системе!");
        }
        if (!roleManager.exists(assignment.role().getName())) {
            throw new IllegalArgumentException("Ошибка: Роль не существует в системе!");
        }

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

    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .toList();
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.role().equals(role))
                .toList();
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .toList();
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(a -> !a.isActive()) // берем все неактивные
                .toList();
    }

    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role) && a.isActive());
    }

    public void revokeAssignment(String assignmentId) {
        ValidationUtils.requireNonEmpty(assignmentId, "Assignment ID");
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment != null) {
            if (assignment instanceof PermanentAssignment) {
                ((PermanentAssignment) assignment).revoke();
            } else {
                throw new IllegalArgumentException("Отменить можно только постоянное назначение!");
            }
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        ValidationUtils.requireNonEmpty(assignmentId, "Assignment ID");
        if (!ValidationUtils.isValidDate(newExpirationDate)) {
            throw new IllegalArgumentException("Неверный формат даты! Используйте YYYY-MM-DD");
        }
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment != null) {
            if (assignment instanceof TemporaryAssignment) {
                ((TemporaryAssignment) assignment).extend(newExpirationDate);
            } else {
                throw new IllegalArgumentException("Продлить можно только временное назначение!");
            }
        }
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