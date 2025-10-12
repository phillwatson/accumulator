package com.hillayes.accumulator.mocks;

import com.hillayes.accumulator.Accumulation;
import com.hillayes.accumulator.Resolution;
import com.hillayes.accumulator.ResolutionRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class MockResolutionRepository implements ResolutionRepository<MockDateRangedData> {
    private static final Random RANDOM = new Random(100);
    private static final List<MockDateRangedData> EMPTY_RANGE = Collections.emptyList();

    private final Map<Resolution, List<MockDateRangedData>> repository = new HashMap<>();
    private final AtomicInteger pendingBatchCount = new AtomicInteger();

    @Override
    public List<MockDateRangedData> fetch(Instant aStartDate, Instant aEndDate) {
        log.debug("Fetching data from repository [start: {}, end: {}]", aStartDate, aEndDate);
        List<MockDateRangedData> result = new ArrayList<>();
        while (aStartDate.isBefore(aEndDate)) {
            result.add(MockDateRangedData.builder()
                .startDate(aStartDate)
                .endDate(aEndDate)
                .value(RANDOM.nextInt(20))
                .build());

            aStartDate = aStartDate.plusSeconds(60);
        }
        log.debug("Fetched data from repository [start: {}, end: {}, size: {}]", aStartDate, aEndDate, result.size());
        return result;
    }

    public List<MockDateRangedData> getAll(Resolution aResolution) {
        return repository.getOrDefault(aResolution, EMPTY_RANGE);
    }

    @Override
    public List<MockDateRangedData> get(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        return getAll(aResolution).stream()
            .filter(data -> aStartDate.compareTo(data.getStartDate()) >= 0)
            .filter(data -> aEndDate.compareTo(data.getEndDate()) < 0)
            .collect(Collectors.toList());
    }

    @Override
    public void saveBatch(Spliterator<MockDateRangedData> aBatch) {
        pendingBatchCount.incrementAndGet();
        try {
            aBatch.forEachRemaining(element ->
                repository.computeIfAbsent(element.getResolution(), k -> new ArrayList<>())
                    .add(element)
            );
        } finally {
            pendingBatchCount.decrementAndGet();
        }
    }

    @Override
    public boolean isBatchPending() {
        return pendingBatchCount.get() == 0;
    }

    @Override
    public Accumulation<MockDateRangedData> newAccumulation(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        return new MockAccumulation(aResolution, aStartDate, aEndDate);
    }
}
