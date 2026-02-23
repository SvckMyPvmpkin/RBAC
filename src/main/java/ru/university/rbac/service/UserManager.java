package ru.university.rbac.service;

import ru.university.rbac.model.User;
import ru.university.rbac.filter.UserFilter;
import java.util.*;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User user) {
        if (users.containsKey(user.username())) {
            throw new IllegalArgumentException("Такой пользователь уже есть: " + user.username());
        }
        users.put(user.username(), user);
    }

    @Override
    public boolean remove(User user) {
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

    public void update(String username, String newFullName, String newEmail) {
        if (!exists(username)) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        User updated = User.create(username, newFullName, newEmail);
        users.put(username, updated);
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    @Override public int count() {
        return users.size();
    }
    @Override public void clear() {
        users.clear();
    }
}