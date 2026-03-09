package ru.university.rbac.command;

import ru.university.rbac.filter.*;
import ru.university.rbac.model.*;
import ru.university.rbac.util.AuditLog;
import ru.university.rbac.util.ConsoleUtils;
import ru.university.rbac.util.FormatUtils;
import ru.university.rbac.util.ReportGenerator;

import java.util.*;

public class CommandRegistry {

    public static void registerAll(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerSystemCommands(parser);
        registerReportCommands(parser);
    }

    private static void logAction(String action, RBACSystem system, String target, String details) {
        AuditLog.getInstance().log(action, system.getCurrentUser(), target, details);
    }

    // 1. КОМАНДЫ УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ
    private static void registerUserCommands(CommandParser parser) {
        parser.registerCommand("user-list", "Вывести список всех пользователей", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Список пользователей"));
            System.out.printf("%-15s | %-20s | %s\n", "Username", "Full Name", "Email");
            System.out.println("--------------------------------------------------------------");
            for (User u : system.getUserManager().findAll()) {
                System.out.printf("%-15s | %-20s | %s\n", u.username(), u.fullname(), u.email());
            }
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Создание пользователя"));
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);
            String fullName = ConsoleUtils.promptString(scanner, "Введите полное имя", true);
            String email = ConsoleUtils.promptString(scanner, "Введите email", true);
            try {
                system.getUserManager().add(User.create(username, fullName, email));
                logAction("CREATE_USER", system, username, "Успешно создан");
                System.out.println(ConsoleUtils.GREEN + "Пользователь успешно создан!" + ConsoleUtils.RESET);
            } catch (Exception e) {
                logAction("CREATE_USER_FAIL", system, username, e.getMessage());
                System.out.println(ConsoleUtils.RED + "Ошибка: " + e.getMessage() + ConsoleUtils.RESET);
            }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Удаление пользователя"));
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);
            Optional<User> uOpt = system.getUserManager().findByUsername(username);

            if (uOpt.isEmpty()) {
                System.out.println(ConsoleUtils.RED + "Пользователь не найден." + ConsoleUtils.RESET);
                return;
            }

            if (ConsoleUtils.promptYesNo(scanner, "Подтвердите удаление пользователя " + username)) {
                system.getUserManager().remove(uOpt.get());
                logAction("DELETE_USER", system, username, "Пользователь удален");
                System.out.println(ConsoleUtils.GREEN + "Пользователь удален." + ConsoleUtils.RESET);
            } else {
                System.out.println("Удаление отменено.");
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Просмотр пользователя"));
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);
            system.getUserManager().findByUsername(username).ifPresentOrElse(u -> {
                System.out.println("Пользователь: " + u.username() + " (" + u.fullname() + "), " + u.email());
                System.out.println("Назначенные роли:");
                system.getAssignmentManager().findByUser(u).forEach(a ->
                        System.out.println(" - " + a.role().getName() + " [" + a.assignmentType() + "]")
                );
                logAction("VIEW_USER", system, username, "Успешно просмотрен");
            }, () -> {
                logAction("VIEW_USER_FAIL", system, username, "Пользователь не найден");
                System.out.println(ConsoleUtils.RED + "Пользователь не найден." + ConsoleUtils.RESET);
            });
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Обновление пользователя"));
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);
            String fullName = ConsoleUtils.promptString(scanner, "Новое полное имя", true);
            String email = ConsoleUtils.promptString(scanner, "Новый email", true);
            try {
                system.getUserManager().update(username, fullName, email);
                logAction("UPDATE_USER", system, username, "Данные обновлены");
                System.out.println(ConsoleUtils.GREEN + "Данные успешно обновлены." + ConsoleUtils.RESET);
            } catch (Exception e) {
                logAction("UPDATE_USER_FAIL", system, username, e.getMessage());
                System.out.println(ConsoleUtils.RED + "Ошибка: " + e.getMessage() + ConsoleUtils.RESET);
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Поиск пользователей"));
            System.out.println("Фильтры: 1-Username, 2-Email, 3-Домен, 4-Полное имя");
            int choice = ConsoleUtils.promptInt(scanner, "Выберите номер", 1, 4);
            String query = ConsoleUtils.promptString(scanner, "Введите строку поиска", true);

            UserFilter filter = switch (choice) {
                case 1 -> UserFilters.byUsernameContains(query);
                case 2 -> UserFilters.byEmail(query);
                case 3 -> UserFilters.byEmailDomain(query);
                case 4 -> UserFilters.byFullNameContains(query);
                default -> null;
            };

            List<User> results = system.getUserManager().findByFilter(filter);
            results.forEach(u -> System.out.println(u.username() + " - " + u.fullname()));
            logAction("SEARCH_USER", system, query, "Найдено: " + results.size());
        });
    }

    // 2. КОМАНДЫ УПРАВЛЕНИЯ РОЛЯМИ
    private static void registerRoleCommands(CommandParser parser) {
        parser.registerCommand("role-list", "Вывести список всех ролей", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Список ролей"));
            System.out.printf("%-20s | %-15s | %s\n", "Название", "Кол-во прав", "ID");
            System.out.println("---------------------------------------------------------");
            for (Role r : system.getRoleManager().findAll()) {
                System.out.printf("%-20s | %-15d | %s\n", r.getName(), r.getPermissions().size(), r.getId());
            }
            logAction("LIST_ROLES", system, "system", "Вывод списка ролей");
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Создание роли"));
            String name = ConsoleUtils.promptString(scanner, "Название роли", true);
            String desc = ConsoleUtils.promptString(scanner, "Описание роли", true);
            try {
                Role role = new Role(name, desc);
                while (ConsoleUtils.promptYesNo(scanner, "Добавить право к этой роли?")) {
                    String pName = ConsoleUtils.promptString(scanner, "Имя права (READ/WRITE...)", true);
                    String pRes = ConsoleUtils.promptString(scanner, "Ресурс", true);
                    String pDesc = ConsoleUtils.promptString(scanner, "Описание права", true);
                    role.addPermission(new Permission(pName, pRes, pDesc));
                }
                system.getRoleManager().add(role);
                logAction("CREATE_ROLE", system, name, "Роль создана");
                System.out.println(ConsoleUtils.GREEN + "Роль создана." + ConsoleUtils.RESET);
            } catch (Exception e) {
                logAction("CREATE_ROLE_FAIL", system, name, e.getMessage());
                System.out.println(ConsoleUtils.RED + "Ошибка: " + e.getMessage() + ConsoleUtils.RESET);
            }
        });

        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Просмотр роли"));
            String name = ConsoleUtils.promptString(scanner, "Введите имя роли", true);
            system.getRoleManager().findByName(name).ifPresentOrElse(
                    r -> {
                        System.out.println(r); // Использует переопределенный toString()
                        logAction("VIEW_ROLE", system, name, "Просмотр информации");
                    },
                    () -> {
                        logAction("VIEW_ROLE_FAIL", system, name, "Роль не найдена");
                        System.out.println(ConsoleUtils.RED + "Роль не найдена." + ConsoleUtils.RESET);
                    }
            );
        });

        parser.registerCommand("role-update", "Обновить роль", (scanner, system) -> {
            System.out.println(ConsoleUtils.YELLOW + "Обновление базовых полей недоступно." + ConsoleUtils.RESET);
            logAction("UPDATE_ROLE_FAIL", system, "unknown", "Попытка запрещенного обновления");
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Удаление роли"));
            String name = ConsoleUtils.promptString(scanner, "Введите имя роли", true);
            Optional<Role> rOpt = system.getRoleManager().findByName(name);

            if (rOpt.isEmpty()) {
                logAction("DELETE_ROLE_FAIL", system, name, "Роль не найдена");
                System.out.println(ConsoleUtils.RED + "Роль не найдена." + ConsoleUtils.RESET);
                return;
            }

            if (ConsoleUtils.promptYesNo(scanner, "Точно удалить роль " + name + "?")) {
                try {
                    system.getRoleManager().remove(rOpt.get());
                    logAction("DELETE_ROLE", system, name, "Роль удалена");
                    System.out.println(ConsoleUtils.GREEN + "Роль удалена." + ConsoleUtils.RESET);
                } catch (Exception e) {
                    logAction("DELETE_ROLE_FAIL", system, name, e.getMessage());
                    System.out.println(ConsoleUtils.RED + "Ошибка: " + e.getMessage() + ConsoleUtils.RESET);
                }
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Добавление права"));
            String rName = ConsoleUtils.promptString(scanner, "Имя роли", true);
            String pName = ConsoleUtils.promptString(scanner, "Имя права (READ/WRITE...)", true);
            String pRes = ConsoleUtils.promptString(scanner, "Ресурс (users/reports...)", true);
            try {
                system.getRoleManager().addPermissionToRole(rName, new Permission(pName, pRes, "Описание"));
                logAction("ADD_PERMISSION", system, rName, pName + " на " + pRes);
                System.out.println(ConsoleUtils.GREEN + "Право добавлено." + ConsoleUtils.RESET);
            } catch (Exception e) {
                logAction("ADD_PERMISSION_FAIL", system, rName, e.getMessage());
                System.out.println(ConsoleUtils.RED + "Ошибка: " + e.getMessage() + ConsoleUtils.RESET);
            }
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Удаление права"));
            String rName = ConsoleUtils.promptString(scanner, "Имя роли", true);
            Optional<Role> rOpt = system.getRoleManager().findByName(rName);
            if (rOpt.isEmpty()) { System.out.println("Роль не найдена."); return; }

            List<Permission> perms = new ArrayList<>(rOpt.get().getPermissions());
            for (int i = 0; i < perms.size(); i++) {
                System.out.println((i + 1) + ". " + perms.get(i).name() + " on " + perms.get(i).resource());
            }
            int idx = ConsoleUtils.promptInt(scanner, "Номер права для удаления", 1, perms.size()) - 1;

            Permission p = perms.get(idx);
            system.getRoleManager().removePermissionFromRole(rName, p);
            logAction("REMOVE_PERMISSION", system, rName, p.name() + " на " + p.resource());
            System.out.println(ConsoleUtils.GREEN + "Право удалено." + ConsoleUtils.RESET);
        });

        parser.registerCommand("role-search", "Поиск ролей", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Поиск ролей"));
            String query = ConsoleUtils.promptString(scanner, "Введите часть имени роли", true);
            List<Role> results = system.getRoleManager().findByFilter(RoleFilters.byNameContains(query));
            results.forEach(r -> System.out.println(" - " + r.getName()));
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
            System.out.println(FormatUtils.formatHeader("Права пользователя"));
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            system.getUserManager().findByUsername(username).ifPresentOrElse(u -> {
                System.out.println(ConsoleUtils.CYAN + "Список прав для " + username + ":" + ConsoleUtils.RESET);
                system.getAssignmentManager().getUserPermissions(u).forEach(p ->
                        System.out.println(" - " + p.name() + " (Ресурс: " + p.resource() + ")")
                );
                logAction("VIEW_USER_PERMISSIONS", system, username, "Успешно выведено");
            }, () -> {
                logAction("VIEW_USER_PERMISSIONS_FAIL", system, username, "Пользователь не найден");
                System.out.println(ConsoleUtils.RED + "Пользователь не найден." + ConsoleUtils.RESET);
            });
        });

        parser.registerCommand("permissions-check", "Проверить право у пользователя", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Проверка права"));
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);
            String pName = ConsoleUtils.promptString(scanner, "Имя права (например, READ)", true);
            String pRes = ConsoleUtils.promptString(scanner, "Ресурс", true);

            system.getUserManager().findByUsername(username).ifPresentOrElse(u -> {
                boolean hasAccess = system.getAssignmentManager().userHasPermission(u, pName, pRes);
                String status = hasAccess ? "РАЗРЕШЕНО" : "ЗАПРЕЩЕНО";

                System.out.println("Результат: " + (hasAccess ? ConsoleUtils.GREEN : ConsoleUtils.RED) + status + ConsoleUtils.RESET);

                logAction("CHECK_PERMISSION", system, username,
                        "Проверка " + pName + " на " + pRes + " -> " + status);
            }, () -> {
                logAction("CHECK_PERMISSION_FAIL", system, username, "Пользователь не найден");
                System.out.println(ConsoleUtils.RED + "Пользователь не найден." + ConsoleUtils.RESET);
            });
        });
    }


    // 5. СЛУЖЕБНЫЕ КОМАНДЫ
    private static void registerSystemCommands(CommandParser parser) {
        parser.registerCommand("audit-log", "Просмотр журнала событий", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Журнал аудита"));
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
            logAction("CLEAR_SCREEN", system, "console", "Очистка экрана");
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            if (ConsoleUtils.promptYesNo(scanner, "Вы уверены, что хотите выйти?")) {
                logAction("SYSTEM_EXIT", system, "system", "Выход из системы");
                System.out.println("Завершение работы системы. До свидания!");
                System.exit(0);
            }
        });

        parser.registerCommand("save", "Сохранить данные в файл", (scanner, system) -> {
            String filename = ConsoleUtils.promptString(scanner, "Введите имя файла для сохранения", true);
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename))) {
                system.getUserManager().findAll().forEach(u -> writer.println("USER;" + u.username() + ";" + u.fullname() + ";" + u.email()));
                system.getRoleManager().findAll().forEach(r -> writer.println("ROLE;" + r.getName() + ";" + r.getDescription()));
                system.getAssignmentManager().findAll().forEach(a -> writer.println("ASSIGN;" + a.user().username() + ";" + a.role().getName() + ";" + a.assignmentType()));

                logAction("SAVE_DATA", system, filename, "Успешно");
                System.out.println(ConsoleUtils.GREEN + "Данные успешно сохранены." + ConsoleUtils.RESET);
            } catch (Exception e) {
                logAction("SAVE_FAIL", system, filename, e.getMessage());
                System.out.println(ConsoleUtils.RED + "Ошибка: " + e.getMessage() + ConsoleUtils.RESET);
            }
        });

        parser.registerCommand("load", "Загрузить данные из файла", (scanner, system) -> {
            String filename = ConsoleUtils.promptString(scanner, "Введите имя файла для загрузки", true);
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts[0].equals("USER")) system.getUserManager().add(User.create(parts[1], parts[2], parts[3]));
                    else if (parts[0].equals("ROLE")) system.getRoleManager().add(new Role(parts[1], parts[2]));
                }
                logAction("LOAD_DATA", system, filename, "Успешно");
                System.out.println(ConsoleUtils.GREEN + "Данные загружены." + ConsoleUtils.RESET);
            } catch (Exception e) {
                logAction("LOAD_FAIL", system, filename, e.getMessage());
                System.out.println(ConsoleUtils.RED + "Ошибка: " + e.getMessage() + ConsoleUtils.RESET);
            }
        });
    }

    // 6. КОМАНДЫ ОТЧЕТОВ
    private static void registerReportCommands(CommandParser parser) {
        ReportGenerator generator = new ReportGenerator();

        parser.registerCommand("report-users", "Отчет по пользователям", (scanner, system) -> {
            String report = generator.generateUserReport(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);
            handleExport(scanner, system, report, "USER_REPORT");
        });

        parser.registerCommand("report-roles", "Отчет по ролям", (scanner, system) -> {
            String report = generator.generateRoleReport(system.getRoleManager(), system.getAssignmentManager());
            System.out.println(report);
            handleExport(scanner, system, report, "ROLE_REPORT");
        });

        parser.registerCommand("report-matrix", "Матрица прав", (scanner, system) -> {
            String report = generator.generatePermissionMatrix(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);
            handleExport(scanner, system, report, "PERMISSION_MATRIX");
        });
    }

    private static void handleExport(Scanner scanner, RBACSystem system, String report, String actionName) {
        if (ConsoleUtils.promptYesNo(scanner, "Сохранить отчет в файл?")) {
            String filename = ConsoleUtils.promptString(scanner, "Введите имя файла", true);
            new ReportGenerator().exportToFile(report, filename);
            logAction(actionName + "_EXPORT", system, filename, "Успешно");
        } else {
            logAction(actionName + "_VIEW", system, "console", "Просмотр в консоли");
        }
    }
}