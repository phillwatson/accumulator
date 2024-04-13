package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.Accumulation;
import com.hillayes.accumulator.Resolution;
import com.hillayes.accumulator.ResolutionRepository;
import com.hillayes.accumulator.resolutions.DefaultResolution;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class LocalRepository implements ResolutionRepository<LocalData> {
    private static final List<LocalData> EMPTY_RANGE = List.of();

    // the remote source from which data of the lowest resolution is fetched
    private final WarehouseRepository warehouseRepository;

    // the reader that will convert the fetched data into the local data type
    private final WarehouseReader<LocalData> reader = new LocalReader();

    // the repository that will store the fetched data - could be a database
    private final Map<Resolution, List<LocalData>> repository = new HashMap<>();

    public LocalRepository(WarehouseRepository warehouseRepository) {
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

    public List<LocalData> getAll(Resolution aResolution) {
        return repository.getOrDefault(aResolution, EMPTY_RANGE);
    }

    @Override
    public List<LocalData> get(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        return getAll(aResolution).stream()
            .filter(data -> aStartDate.compareTo(data.getStartDate()) >= 0)
            .filter(data -> aEndDate.compareTo(data.getEndDate()) < 0)
            .collect(Collectors.toList());
    }

    @Override
    public void saveBatch(Spliterator<LocalData> aBatch) {
        aBatch.forEachRemaining(element ->
            repository.computeIfAbsent(element.getResolution(), k -> new ArrayList<>())
                .add(element)
        );
    }

    @Override
    public Accumulation<LocalData> newAccumulation(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        return new LocalDataAccumulation(aResolution, aStartDate, aEndDate);
    }
}
