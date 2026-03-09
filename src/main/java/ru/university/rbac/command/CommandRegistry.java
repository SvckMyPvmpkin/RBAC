package ru.university.rbac.command;

import ru.university.rbac.filter.*;
import ru.university.rbac.model.*;
import ru.university.rbac.util.AuditLog;

import java.util.*;

public class CommandRegistry {

    public static void registerAll(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerSystemCommands(parser);
    }

    private static void logAction(String action, RBACSystem system, String target, String details) {
        AuditLog.getInstance().log(action, system.getCurrentUser(), target, details);
    }

    // 1. КОМАНДЫ УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ
    private static void registerUserCommands(CommandParser parser) {
        parser.registerCommand("user-list", "Вывести список всех пользователей", (scanner, system) -> {
            System.out.printf("%-15s | %-20s | %s\n", "Username", "Full Name", "Email");
            System.out.println("--------------------------------------------------------------");
            for (User u : system.getUserManager().findAll()) {
                System.out.printf("%-15s | %-20s | %s\n", u.username(), u.fullname(), u.email());
            }
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.print("Введите username: "); String username = scanner.nextLine();
            System.out.print("Введите полное имя: "); String fullName = scanner.nextLine();
            System.out.print("Введите email: "); String email = scanner.nextLine();
            try {
                system.getUserManager().add(User.create(username, fullName, email));
                logAction("CREATE_USER", system, username, "Успешно создан");
                System.out.println("Пользователь успешно создан!");
            } catch (Exception e) {
                logAction("CREATE_USER_FAIL", system, username, e.getMessage());
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine();
            Optional<User> uOpt = system.getUserManager().findByUsername(username);
            if (uOpt.isEmpty()) { System.out.println("Пользователь не найден."); return; }

            System.out.print("Подтвердить удаление? (введите 'да'): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                system.getUserManager().remove(uOpt.get());
                logAction("DELETE_USER", system, username, "Пользователь удален");
                System.out.println("Пользователь удален.");
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine();
            Optional<User> uOpt = system.getUserManager().findByUsername(username);

            if (uOpt.isPresent()) {
                User u = uOpt.get();
                System.out.println("Пользователь: " + u.username() + " (" + u.fullname() + "), " + u.email());
                System.out.println("Назначенные роли:");
                system.getAssignmentManager().findByUser(u).forEach(a ->
                        System.out.println(" - " + a.role().getName() + " [" + a.assignmentType() + "]")
                );
                logAction("VIEW_USER", system, username, "Успешно просмотрен");
            } else {
                logAction("VIEW_USER_FAIL", system, username, "Пользователь не найден");
                System.out.println("Пользователь не найден.");
            }
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            System.out.print("Введите username: "); String username = scanner.nextLine();
            System.out.print("Новое полное имя: "); String fullName = scanner.nextLine();
            System.out.print("Новый email: "); String email = scanner.nextLine();
            try {
                system.getUserManager().update(username, fullName, email);
                logAction("UPDATE_USER", system, username, "Данные обновлены");
                System.out.println("Данные успешно обновлены.");
            } catch (Exception e) {
                logAction("UPDATE_USER_FAIL", system, username, e.getMessage());
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            System.out.println("Фильтры: 1-По username, 2-По email, 3-По домену email, 4-По полному имени");
            System.out.print("Выберите номер: "); String choice = scanner.nextLine();
            System.out.print("Введите строку поиска: "); String query = scanner.nextLine();

            UserFilter filter = switch (choice) {
                case "1" -> UserFilters.byUsernameContains(query);
                case "2" -> UserFilters.byEmail(query);
                case "3" -> UserFilters.byEmailDomain(query);
                case "4" -> UserFilters.byFullNameContains(query);
                default -> null;
            };

            if (filter != null) {
                List<User> results = system.getUserManager().findByFilter(filter);
                results.forEach(u -> System.out.println(u.username() + " - " + u.fullname()));
                logAction("SEARCH_USER", system, query, "Найдено результатов: " + results.size());
            } else {
                System.out.println("Неверный выбор фильтра.");
            }
        });
    }

    // 2. КОМАНДЫ УПРАВЛЕНИЯ РОЛЯМИ
    private static void registerRoleCommands(CommandParser parser) {
        parser.registerCommand("role-list", "Вывести список всех ролей", (scanner, system) -> {
            System.out.printf("%-20s | %-15s | %s\n", "Название", "Кол-во прав", "ID");
            System.out.println("---------------------------------------------------------");
            for (Role r : system.getRoleManager().findAll()) {
                System.out.printf("%-20s | %-15d | %s\n", r.getName(), r.getPermissions().size(), r.getId());
            }
            logAction("LIST_ROLES", system, "system", "Вывод списка ролей");
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.print("Название роли: "); String name = scanner.nextLine();
            System.out.print("Описание роли: "); String desc = scanner.nextLine();
            try {
                Role role = new Role(name, desc);
                system.getRoleManager().add(role);
                logAction("CREATE_ROLE", system, name, "Роль создана");
                System.out.println("Роль создана.");
            } catch (Exception e) {
                logAction("CREATE_ROLE_FAIL", system, name, e.getMessage());
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String name = scanner.nextLine();
            system.getRoleManager().findByName(name).ifPresentOrElse(
                    r -> {
                        System.out.println(r);
                        logAction("VIEW_ROLE", system, name, "Просмотр информации");
                    },
                    () -> {
                        logAction("VIEW_ROLE_FAIL", system, name, "Роль не найдена");
                        System.out.println("Роль не найдена.");
                    }
            );
        });

        parser.registerCommand("role-update", "Обновить роль", (scanner, system) -> {
            System.out.println("Обновление базовых полей роли (имя/описание) недоступно.");
            logAction("UPDATE_ROLE_FAIL", system, "unknown", "Попытка запрещенного обновления");
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String name = scanner.nextLine();
            Optional<Role> rOpt = system.getRoleManager().findByName(name);
            if (rOpt.isEmpty()) {
                logAction("DELETE_ROLE_FAIL", system, name, "Роль не найдена");
                System.out.println("Роль не найдена.");
                return;
            }

            System.out.print("Точно удалить? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                try {
                    system.getRoleManager().remove(rOpt.get());
                    logAction("DELETE_ROLE", system, name, "Роль удалена");
                    System.out.println("Роль удалена.");
                } catch (Exception e) {
                    logAction("DELETE_ROLE_FAIL", system, name, e.getMessage());
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.print("Имя роли: "); String rName = scanner.nextLine();
            System.out.print("Имя права: "); String pName = scanner.nextLine();
            System.out.print("Ресурс: "); String pRes = scanner.nextLine();
            try {
                system.getRoleManager().addPermissionToRole(rName, new Permission(pName, pRes, "Описание"));
                logAction("ADD_PERMISSION", system, rName, pName + " на " + pRes);
                System.out.println("Право добавлено.");
            } catch (Exception e) {
                logAction("ADD_PERMISSION_FAIL", system, rName, e.getMessage());
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            System.out.print("Имя роли: "); String rName = scanner.nextLine();
            Optional<Role> rOpt = system.getRoleManager().findByName(rName);
            if (rOpt.isEmpty()) { System.out.println("Роль не найдена."); return; }

            List<Permission> perms = new ArrayList<>(rOpt.get().getPermissions());
            for (int i = 0; i < perms.size(); i++) {
                System.out.println((i + 1) + ". " + perms.get(i).name() + " on " + perms.get(i).resource());
            }
            System.out.print("Номер для удаления: ");
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < perms.size()) {
                Permission p = perms.get(idx);
                system.getRoleManager().removePermissionFromRole(rName, p);
                logAction("REMOVE_PERMISSION", system, rName, p.name() + " на " + p.resource());
                System.out.println("Право удалено.");
            }
        });

        parser.registerCommand("role-search", "Поиск ролей", (scanner, system) -> {
            System.out.print("Введите часть имени роли: ");
            String query = scanner.nextLine();
            List<Role> results = system.getRoleManager().findByFilter(RoleFilters.byNameContains(query));
            results.forEach(r -> System.out.println(r.getName()));
            logAction("SEARCH_ROLE", system, query, "Найдено: " + results.size());
        });
    }

    // 3. КОМАНДЫ УПРАВЛЕНИЯ НАЗНАЧЕНИЯМИ
    private static void registerAssignmentCommands(CommandParser parser) {
        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            System.out.print("Username: "); String uName = scanner.nextLine();
            System.out.print("Имя роли: "); String rName = scanner.nextLine();
            System.out.print("Тип (1-Постоянное, 2-Временное): "); String type = scanner.nextLine();
            System.out.print("Причина: "); String reason = scanner.nextLine();

            try {
                User u = system.getUserManager().findByUsername(uName)
                        .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
                Role r = system.getRoleManager().findByName(rName)
                        .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));

                AssignmentMetadata meta = AssignmentMetadata.now(system.getCurrentUser(), reason);
                RoleAssignment assignment;

                if (type.equals("2")) {
                    System.out.print("Дата истечения (YYYY-MM-DD): "); String expires = scanner.nextLine();
                    assignment = new TemporaryAssignment(u, r, meta, expires, false);
                } else {
                    assignment = new PermanentAssignment(u, r, meta);
                }

                system.getAssignmentManager().add(assignment);
                logAction("ASSIGN_ROLE", system, uName + "->" + rName, "Причина: " + reason);
                System.out.println("Успешно назначено!");
            } catch (Exception e) {
                logAction("ASSIGN_ROLE_FAIL", system, uName, e.getMessage());
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            String uName = scanner.nextLine();
            Optional<User> uOpt = system.getUserManager().findByUsername(uName);
            if(uOpt.isEmpty()) { System.out.println("Пользователь не найден."); return; }

            List<RoleAssignment> active = system.getAssignmentManager().findByUser(uOpt.get())
                    .stream().filter(RoleAssignment::isActive).toList();

            if(active.isEmpty()) { System.out.println("Нет активных назначений."); return; }

            System.out.println("Активные назначения:");
            for(int i = 0; i < active.size(); i++) {
                System.out.println((i + 1) + ". " + active.get(i).role().getName() + " (ID: " + active.get(i).assignmentId() + ")");
            }
            System.out.print("Номер для отзыва: ");
            try {
                int idx = Integer.parseInt(scanner.nextLine()) - 1;
                String id = active.get(idx).assignmentId();
                system.getAssignmentManager().revokeAssignment(id);
                logAction("REVOKE_ROLE", system, id, "Роль отозвана");
                System.out.println("Назначение отозвано.");
            } catch (Exception e) {
                logAction("REVOKE_ROLE_FAIL", system, uName, e.getMessage());
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, system) -> {
            system.getAssignmentManager().findAll().forEach(a ->
                    System.out.printf("User: %s | Role: %s | Status: %s\n",
                            a.user().username(), a.role().getName(), (a.isActive() ? "ACTIVE" : "INACTIVE"))
            );
        });

        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            system.getUserManager().findByUsername(scanner.nextLine()).ifPresentOrElse(u ->
                            system.getAssignmentManager().findByUser(u).forEach(a ->
                                    System.out.println(a.role().getName() + " [" + (a.isActive() ? "ACTIVE" : "INACTIVE") + "]")),
                    () -> System.out.println("Пользователь не найден.")
            );
        });

        parser.registerCommand("assignment-list-role", "Пользователи с конкретной ролью", (scanner, system) -> {
            System.out.print("Имя роли: ");
            system.getRoleManager().findByName(scanner.nextLine()).ifPresentOrElse(r ->
                            system.getAssignmentManager().findByRole(r).forEach(a ->
                                    System.out.println(a.user().username() + " [" + (a.isActive() ? "ACTIVE" : "INACTIVE") + "]")),
                    () -> System.out.println("Роль не найдена.")
            );
        });

        parser.registerCommand("assignment-active", "Только активные назначения", (scanner, system) -> {
            system.getAssignmentManager().getActiveAssignments().forEach(a ->
                    System.out.println(a.user().username() + " -> " + a.role().getName())
            );
        });

        parser.registerCommand("assignment-expired", "Истёкшие назначения", (scanner, system) -> {
            system.getAssignmentManager().getExpiredAssignments().forEach(a ->
                    System.out.println(a.user().username() + " -> " + a.role().getName() + " (Expired)")
            );
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, system) -> {
            System.out.print("ID назначения: "); String id = scanner.nextLine();
            System.out.print("Новая дата (YYYY-MM-DD): "); String newDate = scanner.nextLine();
            try {
                system.getAssignmentManager().extendTemporaryAssignment(id, newDate);
                logAction("EXTEND_ASSIGNMENT", system, id, "Новая дата: " + newDate);
                System.out.println("Продлено.");
            } catch (Exception e) {
                logAction("EXTEND_ASSIGNMENT_FAIL", system, id, e.getMessage());
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам", (scanner, system) -> {
            System.out.println("1-Активные, 2-Неактивные");
            String choice = scanner.nextLine();
            AssignmentFilter filter = choice.equals("1") ? AssignmentFilters.activeOnly() : AssignmentFilters.inactiveOnly();
            system.getAssignmentManager().findByFilter(filter).forEach(a ->
                    System.out.println(a.user().username() + " -> " + a.role().getName())
            );
        });
    }

    // 4. КОМАНДЫ ПРОСМОТРА ПРАВ
    private static void registerPermissionCommands(CommandParser parser) {
        parser.registerCommand("permissions-user", "Все права конкретного пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            Optional<User> uOpt = system.getUserManager().findByUsername(username);

            if (uOpt.isPresent()) {
                User u = uOpt.get();
                System.out.println("Права пользователя " + username + ":");
                system.getAssignmentManager().getUserPermissions(u).forEach(p ->
                        System.out.println(" - " + p.name() + " (Ресурс: " + p.resource() + ")")
                );
                logAction("VIEW_USER_PERMISSIONS", system, username, "Успешно выведено");
            } else {
                logAction("VIEW_USER_PERMISSIONS_FAIL", system, username, "Пользователь не найден");
                System.out.println("Пользователь не найден.");
            }
        });

        parser.registerCommand("permissions-check", "Проверить право у пользователя", (scanner, system) -> {
            System.out.print("Username: "); String username = scanner.nextLine();
            System.out.print("Имя права (например, READ): "); String pName = scanner.nextLine();
            System.out.print("Ресурс: "); String pRes = scanner.nextLine();

            Optional<User> uOpt = system.getUserManager().findByUsername(username);
            if (uOpt.isPresent()) {
                boolean hasAccess = system.getAssignmentManager().userHasPermission(uOpt.get(), pName, pRes);
                System.out.println("Результат проверки: " + (hasAccess ? "РАЗРЕШЕНО" : "ЗАПРЕЩЕНО"));

                String status = hasAccess ? "ALLOWED" : "DENIED";
                logAction("CHECK_PERMISSION", system, username,
                        "Проверка " + pName + " на " + pRes + " -> " + status);
            } else {
                logAction("CHECK_PERMISSION_FAIL", system, username, "Пользователь не найден");
                System.out.println("Пользователь не найден.");
            }
        });
    }


    // 5. СЛУЖЕБНЫЕ КОМАНДЫ
    private static void registerSystemCommands(CommandParser parser) {
        parser.registerCommand("audit-log", "Просмотр журнала событий", (scanner, system) -> {
            AuditLog.getInstance().printLog();
            logAction("VIEW_AUDIT_LOG", system, "system", "Просмотр журнала аудита");
        });

        parser.registerCommand("help", "Справка по командам", (scanner, system) -> {
            parser.printHelp();
            logAction("VIEW_HELP", system, "system", "Вывод справки");
        });

        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
            logAction("VIEW_STATS", system, "system", "Просмотр статистики");
        });

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            for(int i = 0; i < 20; i++) System.out.println();
            logAction("CLEAR_SCREEN", system, "console", "Очистка экрана");
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            System.out.print("Вы уверены, что хотите выйти? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                logAction("SYSTEM_EXIT", system, "system", "Выход из системы");
                System.out.println("Завершение работы системы. До свидания!");
                System.exit(0);
            }
        });

        parser.registerCommand("save", "Сохранить данные в файл", (scanner, system) -> {
            System.out.print("Введите имя файла для сохранения: ");
            String filename = scanner.nextLine();
            try {
                try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename))) {
                    for (User u : system.getUserManager().findAll()) {
                        writer.println("USER;" + u.username() + ";" + u.fullname() + ";" + u.email());
                    }
                    for (Role r : system.getRoleManager().findAll()) {
                        writer.println("ROLE;" + r.getName() + ";" + r.getDescription());
                    }
                    for (RoleAssignment a : system.getAssignmentManager().findAll()) {
                        writer.println("ASSIGN;" + a.user().username() + ";" + a.role().getName() + ";" + a.assignmentType());
                    }
                }
                logAction("SAVE_DATA", system, filename, "Успешно");
                System.out.println("Данные успешно сохранены в " + filename);
            } catch (Exception e) {
                logAction("SAVE_FAIL", system, filename, e.getMessage());
                System.out.println("Ошибка при сохранении: " + e.getMessage());
            }
        });

        parser.registerCommand("load", "Загрузить данные из файла", (scanner, system) -> {
            System.out.print("Введите имя файла для загрузки: ");
            String filename = scanner.nextLine();
            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filename));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    switch (parts[0]) {
                        case "USER" -> system.getUserManager().add(User.create(parts[1], parts[2], parts[3]));
                        case "ROLE" -> system.getRoleManager().add(new Role(parts[1], parts[2]));
                    }
                }
                reader.close();
                logAction("LOAD_DATA", system, filename, "Успешно");
                System.out.println("Данные загружены.");
            } catch (Exception e) {
                logAction("LOAD_FAIL", system, filename, e.getMessage());
                System.out.println("Ошибка при загрузке: " + e.getMessage());
            }
        });
    }
}