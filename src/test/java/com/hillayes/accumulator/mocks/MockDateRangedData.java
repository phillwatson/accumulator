package com.hillayes.accumulator.mocks;

import com.hillayes.accumulator.DateRangedData;
import com.hillayes.accumulator.Resolution;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Builder
@ToString
public class MockDateRangedData implements DateRangedData<MockDateRangedData> {
    private final Resolution resolution;
    private final Instant startDate;
    private final Instant endDate;
    private final long value;

    @Override
    public Instant getStartDate() {
        return startDate;
    }

    @Override
    public Instant getEndDate() {
        return endDate;
    }
}
