package ru.university.rbac.model;
import java.util.regex.Pattern;

public record User(String username, String fullname, String email) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^.+@.+\\..+$");

    public static User create(String username, String fullname, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Поле уникального имени пользователя не может быть пустым");
        }

        if (fullname == null || fullname.isBlank()) {
            throw new IllegalArgumentException("Поле полного имени пользователя не может быть пустым");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Поле электронной почты пользователя не может быть пустым");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Поле Username не соответствует формату: " +
                    "только латинские буквы, цифры и подчёркивание (от 3 до 20 символов)");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Поле Email не соответствует формату: базовый формат email");
        }

        return new User(username, fullname, email);
    }

    public String format() {
        return String.format("%s (%s) <%s>", username, fullname, email);
    }
}