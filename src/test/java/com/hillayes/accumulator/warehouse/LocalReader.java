package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.resolutions.DefaultResolution;

import java.time.Instant;

public class LocalReader implements WarehouseReader<LocalData> {
    @Override
    public LocalData readLine(WarehouseRequest aRequest, String aLine, int aIndex) {
        String[] parts = aLine.split(",");

        LocalData result = new LocalData();

        // date from warehouse is always in the lowest resolution
        result.setResolution(DefaultResolution.MINUTE);
        result.setStartDate(Instant.ofEpochSecond(Long.parseLong(parts[0])));
        result.setEndDate(DefaultResolution.MINUTE.next(result.getStartDate()));

        result.setUnits(Long.parseLong(parts[1]));
        result.setBlocks(Long.parseLong(parts[2]));

        return result;
    }
}
