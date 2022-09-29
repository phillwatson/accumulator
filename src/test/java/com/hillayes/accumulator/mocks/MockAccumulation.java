package com.hillayes.accumulator.mocks;

import com.hillayes.accumulator.Accumulation;
import com.hillayes.accumulator.Resolution;

import java.time.Instant;

public class MockAccumulation implements Accumulation<MockDateRangedData> {
    private final MockDateRangedData.MockDateRangedDataBuilder builder;
    private long accumulatedValue;

    public MockAccumulation(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        builder = MockDateRangedData.builder()
            .resolution(aResolution)
            .startDate(aStartDate)
            .endDate(aEndDate);
    }

    @Override
    public void add(MockDateRangedData aLowerResRecord) {
        accumulatedValue += aLowerResRecord.getValue();
    }

    @Override
    public MockDateRangedData complete() {
        return builder
            .value(accumulatedValue)
            .build();
    }
}
