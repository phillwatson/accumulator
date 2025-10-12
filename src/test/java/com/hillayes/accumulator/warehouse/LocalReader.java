package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.Resolution;
import com.hillayes.accumulator.resolutions.DefaultResolution;

import java.time.Instant;

public class LocalReader implements WarehouseReader<LocalData> {
    private static final Resolution WAREHOUSE_RESOLUTION = DefaultResolution.MINUTE;

    @Override
    public LocalData readLine(WarehouseRequest aRequest, String aLine, int aIndex) {
        // parse the warehouse data
        String[] parts = aLine.split(",");
        Instant timestamp = Instant.ofEpochSecond(Long.parseLong(parts[0]));
        long units = Long.parseLong(parts[1]);
        long blocks = Long.parseLong(parts[2]);

        // data from warehouse is always in the lowest resolution
        return LocalData.builder()
            .resolution(WAREHOUSE_RESOLUTION)
            .startDate(timestamp)
            .endDate(WAREHOUSE_RESOLUTION.next(timestamp))
            .units(units)
            .blocks(blocks)
            .build();
    }
}
