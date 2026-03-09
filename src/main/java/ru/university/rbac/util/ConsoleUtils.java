package ru.university.rbac.util;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(YELLOW + message + ": " + RESET);
            String input = scanner.nextLine().trim();
            if (required && input.isEmpty()) {
                System.out.println(RED + "Ошибка: поле обязательно для заполнения!" + RESET);
                continue;
            }
            return input;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(YELLOW + message + " (" + min + "-" + max + "): " + RESET);
            try {
                int val = Integer.parseInt(scanner.nextLine());
                if (val >= min && val <= max) return val;
                System.out.println(RED + "Ошибка: число вне диапазона!" + RESET);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Ошибка: введите корректное число!" + RESET);
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(YELLOW + message + " (да/нет): " + RESET);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("да")) return true;
            if (input.equals("нет")) return false;
            System.out.println(RED + "Ошибка: введите 'да' или 'нет'." + RESET);
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        System.out.println(CYAN + message + RESET);
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i).toString());
        }
        int choice = promptInt(scanner, "Выберите номер", 1, options.size());
        return options.get(choice - 1);
    }
}