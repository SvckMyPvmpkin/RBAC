package ru.university.rbac.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.university.rbac.command.RBACSystem;
import ru.university.rbac.filter.UserFilters;
import ru.university.rbac.model.*;
import ru.university.rbac.util.AuditLog;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RbacLoadTest {

    @Test
    public void runHeavyLoadTest() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        system.initialize();

        int threadCount = 10;
        int operationsPerThread = 50;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String uniqueSuffix = threadId + "_" + j;

                        User user = User.create("user_" + uniqueSuffix, "Name " + uniqueSuffix, "email" + uniqueSuffix + "@test.com");
                        system.getUserManager().add(user);

                        Role role = new Role("Role_" + uniqueSuffix, "Description");
                        system.getRoleManager().add(role);

                        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Load Test");
                        system.getAssignmentManager().add(new PermanentAssignment(user, role, meta));

                        system.getUserManager().findByFilterParallel(UserFilters.byUsernameContains("user_" + threadId));

                        system.getUserManager().update(user.username(), "Updated Name", user.email());
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка в потоке " + threadId + ": " + e.getMessage());
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Нагрузочный тест завершен за " + duration + " мс");
        System.out.println("Всего ошибок: " + errorCount.get());

        int expectedTotal = threadCount * operationsPerThread;

        assertEquals(expectedTotal + 1, system.getUserManager().count(), "Потоки потеряли пользователей!");

        assertEquals(expectedTotal + 3, system.getRoleManager().count(), "Потоки потеряли роли!");

        assertEquals(expectedTotal + 1, system.getAssignmentManager().count(), "Потоки потеряли назначения!");

        assertEquals(0, errorCount.get(), "Во время теста возникли исключения!");
    }
}