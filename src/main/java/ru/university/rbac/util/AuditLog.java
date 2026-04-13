package ru.university.rbac.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class AuditLog {
    private static final AuditLog instance = new AuditLog();
    public static AuditLog getInstance() { return instance; }

    public record AuditEntry(String timestamp, String action, String performer, String target, String details) {}

    private final List<AuditEntry> entries = Collections.synchronizedList(new ArrayList<>());

    private final BlockingQueue<AuditEntry> logQueue = new LinkedBlockingQueue<>();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditLog() {
        Thread worker = new Thread(() -> {
            try {
                while (true) {
                    AuditEntry entry = logQueue.take();
                    entries.add(entry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);

        logQueue.offer(entry);
    }

    public List<AuditEntry> getAll() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public List<AuditEntry> getByPerformer(String performer) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.performer().equals(performer))
                    .collect(Collectors.toList());
        }
    }

    public List<AuditEntry> getByAction(String action) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.action().equals(action))
                    .collect(Collectors.toList());
        }
    }

    public void printLog() {
        synchronized (entries) {
            entries.forEach(e -> System.out.printf("[%s] %s | Performer: %s | Target: %s | Details: %s%n",
                    e.timestamp(), e.action(), e.performer(), e.target(), e.details()));
        }
    }

    public void saveToFile(String filename) {
        synchronized (entries) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                for (AuditEntry e : entries) {
                    writer.printf("[%s] %s | Performer: %s | Target: %s | Details: %s%n",
                            e.timestamp(), e.action(), e.performer(), e.target(), e.details());
                }
            } catch (IOException e) {
                System.err.println("Ошибка сохранения лога: " + e.getMessage());
            }
        }
    }
}