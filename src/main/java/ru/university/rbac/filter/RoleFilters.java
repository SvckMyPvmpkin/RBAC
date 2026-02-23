package ru.university.rbac.filter;

import ru.university.rbac.model.Permission;
import ru.university.rbac.model.Role;

public class RoleFilters {
    public static RoleFilter byName(String name) {
        return r -> r.getName().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        return r -> r.getName().toLowerCase().contains(substring.toLowerCase());
    }

    public static RoleFilter hasPermission(Permission permission) {
        return r -> r.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        return r -> r.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        // Проверяем размер набора прав через геттер, который возвращает Set
        return r -> r.getPermissions().size() >= n;
    }
}