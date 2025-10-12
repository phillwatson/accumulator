package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.Accumulation;
import com.hillayes.accumulator.Resolution;
import com.hillayes.accumulator.ConcurrentResolutionRepository;
import com.hillayes.accumulator.resolutions.DefaultResolution;

import java.time.Instant;
import java.util.*;

public class LocalRepository extends ConcurrentResolutionRepository<LocalData> {
    // the remote source from which data of the lowest resolution is fetched
    private final WarehouseRepository warehouseRepository;

    // the reader that will convert the fetched data into the local data type
    private final WarehouseReader<LocalData> reader = new LocalReader();

    public LocalRepository(ConcurrentResolutionRepository.ThreadedDatabase<LocalData> database,
                           WarehouseRepository warehouseRepository) {
        super(database);
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public List<LocalData> fetch(Instant aStartDate, Instant aEndDate) {
        WarehouseRequest request = WarehouseRequest.builder()
            .resolution(DefaultResolution.MINUTE)
            .startDate(aStartDate)
            .endDate(aEndDate)
            .build();
        return warehouseRepository.get(request, reader);
    }

    @Override
    public Accumulation<LocalData> newAccumulation(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        return new LocalDataAccumulation(aResolution, aStartDate, aEndDate);
    }
}
