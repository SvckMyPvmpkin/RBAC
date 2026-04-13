package ru.university.rbac;

import ru.university.rbac.command.CommandParser;
import ru.university.rbac.command.CommandRegistry;
import ru.university.rbac.command.RBACSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RBACSystem system = new RBACSystem();
        system.initialize();

        system.startScheduledTasks();

        CommandParser parser = new CommandParser();
        CommandRegistry.registerAll(parser);

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nДобро пожаловать в RBAC System! Введите 'help' для списка команд.");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine();
            parser.parseAndExecute(input, scanner, system);
        }
    }
}