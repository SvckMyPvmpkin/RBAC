package ru.university.rbac.filter;

import ru.university.rbac.model.User;

public class UserFilters {
    public static UserFilter byUsername(String username) {
        return u -> u.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        return u -> u.username().toLowerCase().contains(substring.toLowerCase());
    }

    public static UserFilter byEmail(String email) {
        return u -> u.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        return u -> u.email().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String substring) {
        return u -> u.fullname().toLowerCase().contains(substring.toLowerCase());
    }
}