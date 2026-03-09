package ru.university.rbac.command;

import java.util.Scanner;

@FunctionalInterface
public interface Command {
    void execute(Scanner scanner, RBACSystem system);
}