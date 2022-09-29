package com.hillayes.accumulator.resolutions;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultResolutionTest {
    @Test
    public void testMinute_roundDown() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundDown = DefaultResolution.MINUTE.roundDown(instant);

        assertEquals(Instant.parse("2022-09-26T10:12:00Z"), roundDown);
        assertEquals(roundDown, DefaultResolution.MINUTE.roundDown(roundDown));
    }

    @Test
    public void testMinute_roundUp() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundUp = DefaultResolution.MINUTE.roundUp(instant);

        assertEquals(Instant.parse("2022-09-26T10:13:00Z"), roundUp);
        assertEquals(roundUp, DefaultResolution.MINUTE.roundUp(roundUp));
    }

    @Test
    public void testMinute_next() {
        Instant instant = Instant.parse("2022-09-26T23:58:20Z");

        Instant next = DefaultResolution.MINUTE.next(instant);
        assertEquals(Instant.parse("2022-09-26T23:59:00Z"), next);

        next = DefaultResolution.MINUTE.next(next);
        assertEquals(Instant.parse("2022-09-27T00:00:00Z"), next);
    }

    @Test
    public void testHour_roundDown() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundDown = DefaultResolution.HOUR.roundDown(instant);

        assertEquals(Instant.parse("2022-09-26T10:00:00Z"), roundDown);
        assertEquals(roundDown, DefaultResolution.HOUR.roundDown(roundDown));
    }

    @Test
    public void testHour_roundUp() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundUp = DefaultResolution.HOUR.roundUp(instant);

        assertEquals(Instant.parse("2022-09-26T11:00:00Z"), roundUp);
        assertEquals(roundUp, DefaultResolution.HOUR.roundUp(roundUp));
    }

    @Test
    public void testHour_next() {
        Instant instant = Instant.parse("2022-09-26T22:58:20Z");

        Instant next = DefaultResolution.HOUR.next(instant);
        assertEquals(Instant.parse("2022-09-26T23:00:00Z"), next);

        next = DefaultResolution.HOUR.next(next);
        assertEquals(Instant.parse("2022-09-27T00:00:00Z"), next);
    }

    @Test
    public void testDay_roundDown() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundDown = DefaultResolution.DAY.roundDown(instant);

        assertEquals(Instant.parse("2022-09-26T00:00:00Z"), roundDown);
        assertEquals(roundDown, DefaultResolution.DAY.roundDown(roundDown));
    }

    @Test
    public void testDay_roundUp() {
        Instant instant = Instant.parse("2022-09-26T10:12:20Z");
        Instant roundUp = DefaultResolution.DAY.roundUp(instant);

        assertEquals(Instant.parse("2022-09-27T00:00:00Z"), roundUp);
        assertEquals(roundUp, DefaultResolution.DAY.roundUp(roundUp));
    }

    @Test
    public void testDay_next() {
        Instant instant = Instant.parse("2022-09-29T10:12:20Z");

        Instant next = DefaultResolution.DAY.next(instant);
        assertEquals(Instant.parse("2022-09-30T00:00:00Z"), next);

        next = DefaultResolution.DAY.next(next);
        assertEquals(Instant.parse("2022-10-01T00:00:00Z"), next);
    }

    @Test
    public void testWeek_roundDown() {
        Instant instant = Instant.parse("2022-09-28T10:12:20Z");
        Instant roundDown = DefaultResolution.WEEK.roundDown(instant);

        assertEquals(Instant.parse("2022-09-26T00:00:00Z"), roundDown);
        assertEquals(roundDown, DefaultResolution.WEEK.roundDown(roundDown));
    }

    @Test
    public void testWeek_roundUp() {
        Instant instant = Instant.parse("2022-09-28T10:12:20Z");
        Instant roundUp = DefaultResolution.WEEK.roundUp(instant);

        assertEquals(Instant.parse("2022-10-03T00:00:00Z"), roundUp);
        assertEquals(roundUp, DefaultResolution.WEEK.roundUp(roundUp));
    }

    @Test
    public void testWeek_next() {
        Instant instant = Instant.parse("2022-09-28T10:12:20Z");

        Instant next = DefaultResolution.WEEK.next(instant);
        assertEquals(Instant.parse("2022-10-03T00:00:00Z"), next);

        next = DefaultResolution.WEEK.next(next);
        assertEquals(Instant.parse("2022-10-10T00:00:00Z"), next);
    }
}
