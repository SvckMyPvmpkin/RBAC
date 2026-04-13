package ru.university.rbac.command;

import ru.university.rbac.model.*;
import ru.university.rbac.service.AssignmentManager;
import ru.university.rbac.service.BackgroundExecutor;
import ru.university.rbac.service.RoleManager;
import ru.university.rbac.service.UserManager;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;

    private final BackgroundExecutor backgroundExecutor;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(this.userManager, this.roleManager);

        this.backgroundExecutor = new BackgroundExecutor();

        this.roleManager.setAssignmentManager(this.assignmentManager);

        this.currentUser = "system";
    }


    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public BackgroundExecutor getBackgroundExecutor() {
        return backgroundExecutor;
    }

    public void initialize() {
        Permission readUsers = new Permission("READ", "users", "Просмотр пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Создание и редактирование пользователей");
        Permission deleteUsers = new Permission("DELETE", "users", "Удаление пользователей");

        Role adminRole = new Role("Admin", "Полный доступ к системе");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        roleManager.add(adminRole);

        Role managerRole = new Role("Manager", "Управление пользователями (без удаления)");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        roleManager.add(managerRole);

        Role viewerRole = new Role("Viewer", "Только чтение");
        viewerRole.addPermission(readUsers);
        roleManager.add(viewerRole);

        User adminUser = User.create("admin", "System Administrator", "admin@system.local");
        userManager.add(adminUser);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(adminUser, adminRole, meta);
        assignmentManager.add(adminAssignment);

        this.currentUser = "admin";

        System.out.println("Система успешно инициализирована. Базовые данные загружены.");
    }

    public String generateStatistics() {
        int usersCount = userManager.count();
        int rolesCount = roleManager.count();
        int totalAssignments = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();

        double avgRoles = usersCount == 0 ? 0 : (double) totalAssignments / usersCount;

        Map<Role, Long> roleCounts = assignmentManager.findAll().stream()
                .collect(Collectors.groupingBy(RoleAssignment::role, Collectors.counting()));

        String top3Roles = roleCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // сортировка по убыванию
                .limit(3)
                .map(e -> e.getKey().getName() + " (" + e.getValue() + " назнач.)")
                .collect(Collectors.joining(", "));

        if (top3Roles.isEmpty()) {
            top3Roles = "Нет назначений";
        }

        return String.format(
                "========== СТАТИСТИКА СИСТЕМЫ ==========\n" +
                        "Пользователей: %d\n" +
                        "Ролей: %d\n" +
                        "Назначений: %d (Активных: %d, Истёкших: %d)\n" +
                        "Среднее кол-во ролей на пользователя: %.2f\n" +
                        "Топ-3 популярных ролей: %s\n" +
                        "========================================",
                usersCount, rolesCount, totalAssignments, activeAssignments, expiredAssignments, avgRoles, top3Roles
        );
    }

    public void startScheduledTasks() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                assignmentManager.deactivateExpiredAssignments();

                String stats = generateStatistics();

                auditLog.log("SCHEDULED_STATS", "system", "statistics",
                        "Автоматическая проверка завершена. Текущее состояние: " + stats);

            } catch (Exception e) {
                auditLog.log("SCHEDULED_TASK_ERROR", "system", "errors", e.getMessage());
            }
        }, 5, 30, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
        backgroundExecutor.shutdown();
    }
}