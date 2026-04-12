package ru.university.rbac.service;

import ru.university.rbac.model.User;
import ru.university.rbac.filter.UserFilter;
import java.util.*;
import ru.university.rbac.util.ValidationUtils;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public synchronized void add(User user) {
        ValidationUtils.requireNonEmpty(user.username(), "Username");
        ValidationUtils.requireNonEmpty(user.email(), "Email");

        if (!ValidationUtils.isValidUsername(user.username())) {
            throw new IllegalArgumentException("Неверный формат username!");
        }
        if (!ValidationUtils.isValidEmail(user.email())) {
            throw new IllegalArgumentException("Неверный формат email!");
        }

        if (users.containsKey(user.username())) {
            throw new IllegalArgumentException("Такой пользователь уже есть: " + user.username());
        }
        users.put(user.username(), user);
    }

    @Override
    public synchronized boolean remove(User user) {
        return users.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.email().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        return users.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .toList();
    }

    public List<User> findByFilter(UserFilter filter) {
        return users.values().stream()
                .filter(filter::test)
                .toList();
    }

    public List<User> findByFilterParallel(UserFilter filter) {
        return users.values().parallelStream()
                .filter(filter::test)
                .toList();
    }

    public synchronized void update(String username, String newFullName, String newEmail) {
        if (!exists(username)) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        ValidationUtils.requireNonEmpty(newFullName, "Full Name");
        if (!ValidationUtils.isValidEmail(newEmail)) {
            throw new IllegalArgumentException("Неверный формат email!");
        }

        String normalizedFullName = ValidationUtils.normalizeString(newFullName);

        User updated = User.create(username, normalizedFullName, newEmail);
        users.put(username, updated);
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager userManager = (UserManager) o;
        return users.equals(userManager.users);
    }

    @Override
    public int hashCode(){
        return users.hashCode();
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public synchronized void clear() {
        users.clear();
    }
}