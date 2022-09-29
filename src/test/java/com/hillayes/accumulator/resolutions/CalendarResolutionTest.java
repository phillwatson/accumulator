package com.hillayes.accumulator.resolutions;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalendarResolutionTest {
    @Test
    public void testDay_roundDown() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundDown = CalendarResolution.DAY.roundDown(instant);

        assertEquals(Instant.parse("2022-09-26T00:00:00Z"), roundDown);
        assertEquals(roundDown, CalendarResolution.DAY.roundDown(roundDown));
    }

    @Test
    public void testDay_roundUp() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundUp = CalendarResolution.DAY.roundUp(instant);

        assertEquals(Instant.parse("2022-09-27T00:00:00Z"), roundUp);
        assertEquals(roundUp, CalendarResolution.DAY.roundDown(roundUp));
    }

    @Test
    public void testDay_next() {
        Instant instant = Instant.parse("2022-09-29T10:12:20Z");

        Instant next = CalendarResolution.DAY.next(instant);
        assertEquals(Instant.parse("2022-09-30T00:00:00Z"), next);

        next = CalendarResolution.DAY.next(next);
        assertEquals(Instant.parse("2022-10-01T00:00:00Z"), next);
    }

    @Test
    public void testMonth_roundDown() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundDown = CalendarResolution.MONTH.roundDown(instant);

        assertEquals(Instant.parse("2022-09-01T00:00:00Z"), roundDown);
        assertEquals(roundDown, CalendarResolution.MONTH.roundDown(roundDown));

    }

    @Test
    public void testMonth_roundUp() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundUp = CalendarResolution.MONTH.roundUp(instant);

        assertEquals(Instant.parse("2022-10-01T00:00:00Z"), roundUp);
        assertEquals(roundUp, CalendarResolution.MONTH.roundDown(roundUp));
    }

    @Test
    public void testMonth_next() {
        Instant instant = Instant.parse("2022-11-26T10:12:20Z");

        Instant next = CalendarResolution.MONTH.next(instant);
        assertEquals(Instant.parse("2022-12-01T00:00:00Z"), next);

        next = CalendarResolution.MONTH.next(next);
        assertEquals(Instant.parse("2023-01-01T00:00:00Z"), next);
    }

    @Test
    public void testYear_roundDown() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundDown = CalendarResolution.YEAR.roundDown(instant);

        assertEquals(Instant.parse("2022-01-01T00:00:00Z"), roundDown);
        assertEquals(roundDown, CalendarResolution.YEAR.roundDown(roundDown));
    }

    @Test
    public void testYear_roundUp() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundUp = CalendarResolution.YEAR.roundUp(instant);

        assertEquals(Instant.parse("2023-01-01T00:00:00Z"), roundUp);
        assertEquals(roundUp, CalendarResolution.YEAR.roundUp(roundUp));
    }
}
