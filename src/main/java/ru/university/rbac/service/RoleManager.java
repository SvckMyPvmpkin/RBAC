package ru.university.rbac.service;

import ru.university.rbac.model.Role;
import ru.university.rbac.model.Permission;
import ru.university.rbac.filter.RoleFilter;
import java.util.*;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();

    private AssignmentManager assignmentManager;

    public void setAssignmentManager(AssignmentManager assignmentManager) {
        this.assignmentManager = assignmentManager;
    }

    @Override
    public void add(Role role) {
        if (rolesByName.containsKey(role.getName())) {
            throw new IllegalArgumentException("Имя роли должно быть уникальным: " + role.getName());
        }
        rolesById.put(role.getId(), role);
        rolesByName.put(role.getName(), role);
    }

    @Override
    public boolean remove(Role role) {
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

    public void addPermissionToRole(String roleName, Permission permission) {
        findByName(roleName).ifPresent(role -> role.addPermission(permission));
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
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
    @Override public void clear() {
        rolesById.clear(); rolesByName.clear();
    }
}