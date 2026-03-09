package ru.university.rbac.util;

import java.util.List;

public class FormatUtils {

    public static String formatTable(String[] headers, List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        int[] widths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
            for (String[] row : rows) widths[i] = Math.max(widths[i], row[i].length());
        }

        String line = "+";
        for (int w : widths) line += "-".repeat(w + 2) + "+";
        sb.append(line).append("\n|");
        for (int i = 0; i < headers.length; i++) sb.append(" ").append(String.format("%-" + widths[i] + "s", headers[i])).append(" |");
        sb.append("\n").append(line).append("\n");

        for (String[] row : rows) {
            sb.append("|");
            for (int i = 0; i < row.length; i++) sb.append(" ").append(String.format("%-" + widths[i] + "s", row[i])).append(" |");
            sb.append("\n");
        }
        sb.append(line);
        return sb.toString();
    }

    public static String formatBox(String text) {
        String border = "+" + "-".repeat(text.length() + 2) + "+";
        return border + "\n| " + text + " |\n" + border;
    }

    public static String formatHeader(String text) {
        return "\n>>> " + text.toUpperCase() + " <<<\n";
    }

    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    public static String padRight(String text, int length) {
        return String.format("%-" + length + "s", text != null ? text : "");
    }

    public static String padLeft(String text, int length) {
        return String.format("%" + length + "s", text != null ? text : "");
    }
}