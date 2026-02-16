package ru.university.rbac.model;

public record Permission(String name, String resource, String description) {
    public Permission {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название права не может быть пустым");
        }
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }

        name = name.toUpperCase().replace(" ", "");
        resource = resource.toLowerCase();
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatch = name.contains(namePattern.toUpperCase());
        boolean resourceMatch = resource.contains(resourcePattern.toLowerCase());

        return nameMatch && resourceMatch;
    }
}
