package ru.university.rbac.service;

import ru.university.rbac.model.Role;
import ru.university.rbac.model.Permission;
import ru.university.rbac.filter.RoleFilter;
import java.util.*;
import ru.university.rbac.util.ValidationUtils;
import java.util.concurrent.ConcurrentHashMap;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new ConcurrentHashMap<>();
    private final Map<String, Role> rolesByName = new ConcurrentHashMap<>();

    private volatile AssignmentManager assignmentManager;

    public void setAssignmentManager(AssignmentManager assignmentManager) {
        this.assignmentManager = assignmentManager;
    }

    @Override
    public synchronized void add(Role role) {
        ValidationUtils.requireNonEmpty(role.getName(), "Имя роли");
        ValidationUtils.requireNonEmpty(role.getDescription(), "Описание роли");

        String normalizedName = ValidationUtils.normalizeString(role.getName());

        if (rolesByName.containsKey(normalizedName)) {
            throw new IllegalArgumentException("Имя роли должно быть уникальным: " + normalizedName);
        }

        rolesById.put(role.getId(), role);
        rolesByName.put(normalizedName, role);
    }

    @Override
    public synchronized boolean remove(Role role) {
        if (assignmentManager != null) {
            boolean isAssigned = assignmentManager.getActiveAssignments().stream()
                    .anyMatch(a -> a.role().equals(role));

            if (isAssigned) {
                throw new IllegalStateException("Ошибка: Нельзя удалить роль, так как она активно назначена пользователям!");
            }
        }

        rolesByName.remove(role.getName());
        return rolesById.remove(role.getId()) != null;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .toList();
    }

    public synchronized void addPermissionToRole(String roleName, Permission permission) {
        ValidationUtils.requireNonEmpty(roleName, "Имя роли");
        findByName(roleName).ifPresent(role -> role.addPermission(permission));
    }

    public synchronized void removePermissionFromRole(String roleName, Permission permission) {
        ValidationUtils.requireNonEmpty(roleName, "Имя роли");
        findByName(roleName).ifPresent(role -> role.removePermission(permission));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .toList();
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .toList();
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    @Override public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }
    @Override public int count() {
        return rolesById.size();
    }
    @Override public synchronized void clear() {
        rolesById.clear(); rolesByName.clear();
    }
}