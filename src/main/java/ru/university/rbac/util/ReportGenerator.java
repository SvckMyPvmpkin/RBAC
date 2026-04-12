package ru.university.rbac.util;

import ru.university.rbac.model.*;
import ru.university.rbac.service.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager um, AssignmentManager am) {
        String header = "=== ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ ===\n";

        String body = um.findAll().parallelStream()
                .map(u -> {
                    List<RoleAssignment> roles = am.findByUser(u);
                    String rolesStr = roles.isEmpty() ? "нет" :
                            roles.stream().map(a -> a.role().getName()).collect(Collectors.joining(", "));

                    return String.format("User: %s | Email: %s\n  Роли: %s", u.username(), u.email(), rolesStr);
                })
                .collect(Collectors.joining("\n"));

        return header + body + "\n";
    }

    public String generateRoleReport(RoleManager rm, AssignmentManager am) {
        StringBuilder sb = new StringBuilder("=== ОТЧЕТ ПО РОЛЯМ ===\n");
        for (Role r : rm.findAll()) {
            long count = am.findByRole(r).size();
            sb.append(String.format("Роль: %-15s | Пользователей: %d\n", r.getName(), count));
        }
        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager um, AssignmentManager am) {
        String header = "=== МАТРИЦА ПРАВ (User x Resource) ===\n";

        String body = um.findAll().parallelStream()
                .map(u -> {
                    Set<Permission> perms = am.getUserPermissions(u);
                    String resources = perms.stream()
                            .map(Permission::resource)
                            .distinct()
                            .collect(Collectors.joining(", "));

                    return String.format("User: %-15s | Ресурсы: %s", u.username(), resources.isEmpty() ? "нет" : resources);
                })
                .collect(Collectors.joining("\n"));

        return header + body + "\n";
    }

    public void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
            System.out.println("Отчет сохранен в " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения отчета: " + e.getMessage());
        }
    }
}