package ru.university.rbac;

import ru.university.rbac.model.User;
import ru.university.rbac.model.Permission;
import ru.university.rbac.model.Role;
import ru.university.rbac.model.AssignmentMetadata;

public class Main {
    public static void main(String[] args) {
        System.out.println("--ТЕСТ СОЗДАНИЯ ПОЛЬЗОВАТЕЛЯ--");

        testUser("Успешное создание", "admin_2024", "Иван Иванов", "ivan@example.com");

        //тест username
        testUser("Username слишком короткий (2 символа)", "id", "Имя", "test@mail.ru");
        testUser("Username слишком длинный (>20)", "very_long_username_that_is_invalid", "Имя", "test@mail.ru");
        testUser("Username с запрещенными символами", "user!name", "Имя", "test@mail.ru");

        //тест email
        testUser("Email без символа @", "worker1", "Петр", "petr-mail.ru");
        testUser("Email без точки после @", "worker2", "Петр", "petr@mailru");

        //тест на "пустые поля"
        testUser("Пустое имя (blank)", "manager", "   ", "manager@work.com");
        testUser("Null в полях", null, "Name", "email@test.com");

        System.out.println("\n--ТЕСТ ДОСТУПА ПОЛЬЗОВАТЕЛЯ--\n");

        // тест успешного создания
        Permission p1 = new Permission("read ", "USERS", "Доступ на чтение");
        System.out.println("Нормализация: " + p1.format());

        // тест метода matches
        System.out.println("Match 'READ' и 'user': " + p1.matches("READ", "user"));
        System.out.println("Match 'WRITE': " + p1.matches("WRITE", "user"));

        // тест на пустоту в поле description
        testPermission("DELETE","settings", " " );

        System.out.println("\n--ТЕСТ РОЛЕЙ ПОЛЬЗОВАТЕЛЯ--");

        // тест создания пользователя с ролью
        Role adminRole = new Role("Administrator", "Full system access");
        Permission read = new Permission("READ", "users", "Can view user list");
        Permission write = new Permission("WRITE", "users", "Can edit users");
        Permission delete = new Permission("DELETE", "users", "Can delete users");

        adminRole.addPermission(read);
        adminRole.addPermission(write);
        adminRole.addPermission(delete);

        System.out.println("\n" + adminRole.format());

        // тест удаления роли
        adminRole.removePermission(read);
        System.out.println("После удаления READ:\n" + adminRole.format());

        System.out.println("--ТЕСТ МЕТАДАННЫХ--\n");

        // создание метаданных
        AssignmentMetadata meta = AssignmentMetadata.now("admin_user", "Initial setup");
        System.out.println("Metadata: " + meta.format());

        // тест на пустую причину
        AssignmentMetadata metaEmpty = AssignmentMetadata.now("system", "");
        System.out.println("Metadata (Без указания причины): " + metaEmpty.format());
    }

    public static void testUser(String testName, String username, String fullname, String email) {
        System.out.println("\n[" + testName + "]");

        try {
            User user = User.create(username, fullname, email);
            System.out.println("УСПЕШНО: " + user.format());
        } catch (IllegalArgumentException err) {
            System.out.println("ОЖИДАЕМАЯ ОШИБКА: " + err.getMessage());
        } catch (Exception err) {
            System.out.println("НЕПРЕДВИДЕННАЯ ОШИБКА: " + err.toString());
        }
    }

    public static void testPermission(String name ,  String resource, String description) {
        try {
            new Permission("DELETE", "settings", "   ");
        } catch (IllegalArgumentException err) {
            System.out.println("ОЖИДАЕМАЯ ОШИБКА: " + err.getMessage());
        }
    }
}