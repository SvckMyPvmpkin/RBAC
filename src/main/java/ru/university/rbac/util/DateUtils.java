package ru.university.rbac.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        return LocalDate.parse(date, DATE_FORMATTER).plusDays(days).format(DATE_FORMATTER);
    }

    public static String formatRelativeTime(String date) {
        LocalDate target = LocalDate.parse(date, DATE_FORMATTER);
        LocalDate now = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(now, target);

        if (daysBetween == 0) return "today";
        if (daysBetween > 0) return "in " + daysBetween + " days";
        return Math.abs(daysBetween) + " days ago";
    }
}