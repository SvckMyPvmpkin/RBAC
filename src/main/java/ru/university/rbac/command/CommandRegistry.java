package ru.university.rbac.command;

import ru.university.rbac.filter.*;
import ru.university.rbac.model.*;

import java.util.*;

public class CommandRegistry {

    public static void registerAll(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerSystemCommands(parser);
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
                System.out.println("Пользователь успешно создан!");
            } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            System.out.print("Введите username: ");
            system.getUserManager().findByUsername(scanner.nextLine()).ifPresentOrElse(u -> {
                System.out.println("Пользователь: " + u.username() + " (" + u.fullname() + "), " + u.email());
                System.out.println("Назначенные роли:");
                system.getAssignmentManager().findByUser(u).forEach(a ->
                        System.out.println(" - " + a.role().getName() + " [" + a.assignmentType() + "]")
                );
                System.out.println("Все права:");
                system.getAssignmentManager().getUserPermissions(u).forEach(p ->
                        System.out.println(" - " + p.name() + " (" + p.resource() + ")")
                );
            }, () -> System.out.println("Пользователь не найден."));
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            System.out.print("Введите username: "); String username = scanner.nextLine();
            System.out.print("Новое полное имя: "); String fullName = scanner.nextLine();
            System.out.print("Новый email: "); String email = scanner.nextLine();
            try {
                system.getUserManager().update(username, fullName, email);
                System.out.println("Данные успешно обновлены.");
            } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            Optional<User> uOpt = system.getUserManager().findByUsername(scanner.nextLine());
            if (uOpt.isEmpty()) { System.out.println("Пользователь не найден."); return; }

            System.out.print("Подтвердить удаление? (введите 'да'): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(uOpt.get());
                assignments.forEach(system.getAssignmentManager()::remove);
                system.getUserManager().remove(uOpt.get());
                System.out.println("Пользователь и его назначения удалены.");
            } else { System.out.println("Удаление отменено."); }
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
                system.getUserManager().findByFilter(filter).forEach(u ->
                        System.out.println(u.username() + " - " + u.fullname())
                );
            } else { System.out.println("Неверный выбор фильтра."); }
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
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.print("Название роли: "); String name = scanner.nextLine();
            System.out.print("Описание роли: "); String desc = scanner.nextLine();
            Role role = new Role(name, desc);

            while (true) {
                System.out.print("Добавить право? (да/нет): ");
                if (!scanner.nextLine().equalsIgnoreCase("да")) break;
                System.out.print("Имя права (READ/WRITE...): "); String pName = scanner.nextLine();
                System.out.print("Ресурс (users/reports...): "); String pRes = scanner.nextLine();
                System.out.print("Описание права: "); String pDesc = scanner.nextLine();
                role.addPermission(new Permission(pName, pRes, pDesc));
            }
            system.getRoleManager().add(role);
            System.out.println("Роль создана.");
        });

        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            system.getRoleManager().findByName(scanner.nextLine()).ifPresentOrElse(
                    r -> System.out.println(r),
                    () -> System.out.println("Роль не найдена.")
            );
        });

        parser.registerCommand("role-update", "Обновить роль", (scanner, system) -> {
            System.out.println("Обновление базовых полей роли (имя/описание) недоступно.");
            System.out.println("Используйте команды role-add-permission / role-remove-permission для изменения прав.");
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            Optional<Role> rOpt = system.getRoleManager().findByName(scanner.nextLine());
            if (rOpt.isEmpty()) { System.out.println("Роль не найдена."); return; }

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(rOpt.get());
            if (!assignments.isEmpty()) {
                System.out.println("ВНИМАНИЕ! Эта роль назначена пользователям:");
                assignments.forEach(a -> System.out.println("- " + a.user().username()));
            }
            System.out.print("Точно удалить? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                try {
                    system.getRoleManager().remove(rOpt.get());
                    System.out.println("Роль удалена.");
                } catch (Exception e) { System.out.println(e.getMessage()); }
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.print("Имя роли: "); String rName = scanner.nextLine();
            System.out.print("Право (READ/WRITE...): "); String pName = scanner.nextLine();
            System.out.print("Ресурс (users...): "); String pRes = scanner.nextLine();
            System.out.print("Описание: "); String pDesc = scanner.nextLine();
            try {
                system.getRoleManager().addPermissionToRole(rName, new Permission(pName, pRes, pDesc));
                System.out.println("Право добавлено.");
            } catch (Exception e) { System.out.println("Ошибка: Роль не найдена."); }
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            System.out.print("Имя роли: ");
            Optional<Role> rOpt = system.getRoleManager().findByName(scanner.nextLine());
            if (rOpt.isEmpty()) { System.out.println("Роль не найдена."); return; }

            List<Permission> perms = new ArrayList<>(rOpt.get().getPermissions());
            for (int i = 0; i < perms.size(); i++) {
                System.out.println((i + 1) + ". " + perms.get(i).name() + " on " + perms.get(i).resource());
            }
            System.out.print("Номер для удаления: ");
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < perms.size()) {
                system.getRoleManager().removePermissionFromRole(rOpt.get().getName(), perms.get(idx));
                System.out.println("Право удалено.");
            }
        });

        parser.registerCommand("role-search", "Поиск ролей", (scanner, system) -> {
            System.out.print("Введите часть имени роли для поиска: ");
            system.getRoleManager().findByFilter(RoleFilters.byNameContains(scanner.nextLine()))
                    .forEach(r -> System.out.println(r.getName()));
        });
    }

    // 3. КОМАНДЫ УПРАВЛЕНИЯ НАЗНАЧЕНИЯМИ
    private static void registerAssignmentCommands(CommandParser parser) {
        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            System.out.print("Username: "); String username = scanner.nextLine();
            Optional<User> uOpt = system.getUserManager().findByUsername(username);
            if(uOpt.isEmpty()) { System.out.println("Пользователь не найден."); return; }

            System.out.println("Доступные роли:");
            system.getRoleManager().findAll().forEach(r -> System.out.println("- " + r.getName()));

            System.out.print("Имя роли: "); String rName = scanner.nextLine();
            Optional<Role> rOpt = system.getRoleManager().findByName(rName);
            if(rOpt.isEmpty()) { System.out.println("Роль не найдена."); return; }

            System.out.print("Тип (1-ПОСТОЯННОЕ, 2-ВРЕМЕННОЕ): "); String type = scanner.nextLine();
            System.out.print("Причина: "); String reason = scanner.nextLine();
            AssignmentMetadata meta = AssignmentMetadata.now(system.getCurrentUser(), reason);

            try {
                if (type.equals("2")) {
                    System.out.print("Дата истечения (строка): "); String expires = scanner.nextLine();
                    system.getAssignmentManager().add(new TemporaryAssignment(uOpt.get(), rOpt.get(), meta, expires, false));
                } else {
                    system.getAssignmentManager().add(new PermanentAssignment(uOpt.get(), rOpt.get(), meta));
                }
                System.out.println("Успешно назначено!");
            } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            Optional<User> uOpt = system.getUserManager().findByUsername(scanner.nextLine());
            if(uOpt.isEmpty()) return;

            List<RoleAssignment> active = system.getAssignmentManager().findByUser(uOpt.get())
                    .stream().filter(RoleAssignment::isActive).toList();

            if(active.isEmpty()) { System.out.println("Нет активных назначений."); return; }

            for(int i = 0; i < active.size(); i++) {
                System.out.println((i + 1) + ". " + active.get(i).role().getName() + " (ID: " + active.get(i).assignmentId() + ")");
            }
            System.out.print("Номер для отзыва: ");
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if(idx >= 0 && idx < active.size()) {
                try {
                    system.getAssignmentManager().revokeAssignment(active.get(idx).assignmentId());
                    System.out.println("Назначение отозвано.");
                } catch(Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
            }
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, system) -> {
            System.out.printf("%-10s | %-10s | %-10s | %-10s | %s\n", "User", "Role", "Type", "Status", "Assigned At");
            System.out.println("----------------------------------------------------------------------");
            for(RoleAssignment a : system.getAssignmentManager().findAll()) {
                System.out.printf("%-10s | %-10s | %-10s | %-10s | %s\n",
                        a.user().username(), a.role().getName(), a.assignmentType(),
                        (a.isActive() ? "ACTIVE" : "INACTIVE"), a.metadata().assignedAt());
            }
        });

        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            Optional<User> uOpt = system.getUserManager().findByUsername(scanner.nextLine());
            if(uOpt.isPresent()) {
                system.getAssignmentManager().findByUser(uOpt.get()).forEach(a ->
                        System.out.println(a.role().getName() + "[" + (a.isActive()?"ACTIVE":"INACTIVE") + "]")
                );
            }
        });

        parser.registerCommand("assignment-list-role", "Пользователи с конкретной ролью", (scanner, system) -> {
            System.out.print("Имя роли: ");
            Optional<Role> rOpt = system.getRoleManager().findByName(scanner.nextLine());
            if(rOpt.isPresent()) {
                system.getAssignmentManager().findByRole(rOpt.get()).forEach(a ->
                        System.out.println(a.user().username() + " [" + (a.isActive()?"ACTIVE":"INACTIVE") + "]")
                );
            }
        });

        parser.registerCommand("assignment-active", "Только активные назначения", (scanner, system) -> {
            system.getAssignmentManager().getActiveAssignments().forEach(a ->
                    System.out.println(a.user().username() + " -> " + a.role().getName())
            );
        });

        parser.registerCommand("assignment-expired", "Истёкшие временные назначения", (scanner, system) -> {
            system.getAssignmentManager().getExpiredAssignments().forEach(a ->
                    System.out.println(a.user().username() + " -> " + a.role().getName() + " (Expired)")
            );
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, system) -> {
            System.out.print("Введите ID назначения: "); String id = scanner.nextLine();
            System.out.print("Новая дата: "); String newDate = scanner.nextLine();
            try {
                system.getAssignmentManager().extendTemporaryAssignment(id, newDate);
                System.out.println("Продлено.");
            } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
        });

        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам", (scanner, system) -> {
            System.out.println("Меню фильтров: 1-Активные, 2-Неактивные");
            System.out.print("Выбор: ");
            AssignmentFilter filter = scanner.nextLine().equals("1") ? AssignmentFilters.activeOnly() : AssignmentFilters.inactiveOnly();
            system.getAssignmentManager().findByFilter(filter).forEach(a ->
                    System.out.println(a.user().username() + " -> " + a.role().getName())
            );
        });
    }

    // 4. КОМАНДЫ ПРОСМОТРА ПРАВ
    private static void registerPermissionCommands(CommandParser parser) {
        parser.registerCommand("permissions-user", "Все права конкретного пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            system.getUserManager().findByUsername(scanner.nextLine()).ifPresent(u -> {
                system.getAssignmentManager().getUserPermissions(u).forEach(p ->
                        System.out.println("Ресурс: " + p.resource() + " | Право: " + p.name())
                );
            });
        });

        parser.registerCommand("permissions-check", "Проверить право у пользователя", (scanner, system) -> {
            System.out.print("Username: "); String username = scanner.nextLine();
            System.out.print("Право (READ/WRITE...): "); String pName = scanner.nextLine();
            System.out.print("Ресурс: "); String pRes = scanner.nextLine();

            Optional<User> uOpt = system.getUserManager().findByUsername(username);
            if (uOpt.isPresent()) {
                boolean hasAccess = system.getAssignmentManager().userHasPermission(uOpt.get(), pName, pRes);
                System.out.println("Результат проверки: " + (hasAccess ? "РАЗРЕШЕНО" : "ЗАПРЕЩЕНО"));
            }
        });
    }

    // 5. СЛУЖЕБНЫЕ КОМАНДЫ
    private static void registerSystemCommands(CommandParser parser) {
        parser.registerCommand("help", "Справка по командам", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            for(int i = 0; i < 20; i++) System.out.println();
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            System.out.print("Вы уверены, что хотите выйти? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                System.out.println("Завершение работы системы. До свидания!");
                System.exit(0);
            }
        });

        parser.registerCommand("save", "Сохранить данные в файл (опционально)", (scanner, system) -> {
            System.out.println("Функция сериализации файлов в разработке.");
        });

        parser.registerCommand("load", "Загрузить данные из файла (опционально)", (scanner, system) -> {
            System.out.println("Функция чтения файлов в разработке.");
        });
    }
}