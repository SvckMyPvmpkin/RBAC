package ru.university.rbac.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTests {

    @Test
    public void testDateComparison() {
        assertTrue(DateUtils.isBefore("01-01-2026", "02-01-2026"));
        assertTrue(DateUtils.isAfter("05-01-2026", "01-01-2026"));
    }

    @Test
    public void testAddDays() {
        assertEquals("11-01-2026", DateUtils.addDays("01-01-2026", 10));
    }

    @Test
    public void testRelativeTime() {
        String today = DateUtils.getCurrentDate();
        assertEquals("today", DateUtils.formatRelativeTime(today));
    }
}