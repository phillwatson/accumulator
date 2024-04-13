package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.resolutions.DefaultResolution;

import java.time.Instant;

public class LocalReader implements WarehouseReader<LocalData> {
    @Override
    public LocalData readLine(WarehouseRequest aRequest, String aLine, int aIndex) {
        LocalData result = new LocalData();
        result.setStartDate(aRequest.getStartDate());
        result.setEndDate(aRequest.getEndDate());

        String[] parts = aLine.split(",");
        result.setUnits(Long.parseLong(parts[1]));
        result.setBlocks(Long.parseLong(parts[2]));

        // date from warehouse is always in the lowest resolution
        result.setResolution(DefaultResolution.MINUTE);
        return result;
    }
}
