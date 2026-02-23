package ru.university.rbac.filter;

import ru.university.rbac.model.Role;
import java.util.Comparator;

public class RoleSorters {
    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::getName);
    }
    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(r -> r.getPermissions().size());
    }
}
