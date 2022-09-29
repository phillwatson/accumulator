package com.hillayes.accumulator.resolutions;

import com.hillayes.accumulator.Resolution;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public enum CalendarResolution implements Resolution {
    DAY(null),
    MONTH(DAY),
    YEAR(MONTH);

    private final Optional<Resolution> lower;

    CalendarResolution(CalendarResolution aLower) {
        lower = Optional.ofNullable(aLower);
    }

    @Override
    public Optional<Resolution> getLower() {
        return lower;
    }

    private ZonedDateTime _roundDown(Instant aInstant) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(aInstant, ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.DAYS);
        switch (this) {
            case YEAR:
                zonedDateTime = zonedDateTime.withMonth(1);
                // fall thru to month
            case MONTH:
                zonedDateTime = zonedDateTime.withDayOfMonth(1);
        }
        return zonedDateTime;
    }

    public ZonedDateTime _roundUp(Instant aInstant, boolean toNext) {
        ZonedDateTime zonedDateTime = _roundDown(aInstant);
        if ((!toNext) && (zonedDateTime.toInstant().equals(aInstant))) {
            return zonedDateTime;
        }

        if (this == YEAR) {
            return zonedDateTime.plusYears(1);
        }
        if (this == MONTH) {
            return zonedDateTime.plusMonths(1);
        }
        return zonedDateTime.plusDays(1);
    }

    @Override
    public Instant roundDown(Instant aInstant) {
        return _roundDown(aInstant).toInstant();
    }

    @Override
    public Instant roundUp(Instant aInstant) {
        return _roundUp(aInstant, false).toInstant();
    }

    @Override
    public Instant next(Instant aInstant) {
        return _roundUp(aInstant, true).toInstant();
    }
}
