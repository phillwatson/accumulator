package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.Accumulation;
import com.hillayes.accumulator.Resolution;

import java.time.Instant;

public class LocalDataAccumulation implements Accumulation<LocalData> {
    private final LocalData result;

    public LocalDataAccumulation(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        result = new LocalData();
        result.setResolution(aResolution);
        result.setStartDate(aStartDate);
        result.setEndDate(aEndDate);
}

    @Override
    public void add(LocalData aLowerResRecord) {
        result.setUnits(result.getUnits() + aLowerResRecord.getUnits());
        result.setBlocks(result.getBlocks() + aLowerResRecord.getBlocks());
    }

    @Override
    public LocalData complete() {
        return result;
    }
}
