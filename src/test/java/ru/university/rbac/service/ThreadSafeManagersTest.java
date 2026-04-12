package ru.university.rbac.service;

import org.junit.jupiter.api.Test;
import ru.university.rbac.model.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadSafeManagersTest {

    @Test
    public void testUserManagerThreadSafety() throws InterruptedException {
        UserManager userManager = new UserManager();
        int threadCount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    userManager.add(User.create("user_" + index, "Name " + index, "email" + index + "@test.com"));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(100, userManager.count(), "Должно быть ровно 100 пользователей, добавленных параллельно");
    }

    @Test
    public void testRoleManagerThreadSafety() throws InterruptedException {
        RoleManager roleManager = new RoleManager();
        Role adminRole = new Role("Admin", "Description");
        roleManager.add(adminRole);

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    roleManager.addPermissionToRole("Admin", new Permission("READ", "resource_" + index, "desc"));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        Role updatedRole = roleManager.findByName("Admin").get();
        assertEquals(100, updatedRole.getPermissions().size(), "Должно быть ровно 100 прав, добавленных параллельно");
    }

    @Test
    public void testAssignmentManagerThreadSafety() throws InterruptedException {
        UserManager um = new UserManager();
        RoleManager rm = new RoleManager();
        AssignmentManager am = new AssignmentManager(um, rm);

        User user = User.create("test_user", "Test", "test@mail.com");
        um.add(user);

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Role tempRole = new Role("Role_" + index, "Desc");
                    rm.add(tempRole);
                    am.add(new PermanentAssignment(user, tempRole, AssignmentMetadata.now("system", "test")));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(50, am.findByUser(user).size(), "У пользователя должно быть ровно 50 параллельно назначенных ролей");
    }
}