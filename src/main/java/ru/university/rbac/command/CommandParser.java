package ru.university.rbac.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {
    private final Map<String, Command> commands = new HashMap<>();

    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name, command);
        commandDescriptions.put(name, description);
    }

    public void printHelp() {
        System.out.println("\n=== Доступные команды ===");
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf("%-25s - %s\n", entry.getKey(), entry.getValue());
        }
        System.out.println("=========================\n");
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName);

        if (command == null) {
            System.out.println("Ошибка: Команда '" + commandName + "' не найдена. Введите 'help' для списка команд.");
            return;
        }

        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("Ошибка при выполнении команды: " + e.getMessage());
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        String trimmedInput = input.trim();

        if (trimmedInput.isEmpty()) {
            return;
        }

        String[] parts = trimmedInput.split("\\s+");

        String commandName = parts[0].toLowerCase();

        executeCommand(commandName, scanner, system);
    }
}