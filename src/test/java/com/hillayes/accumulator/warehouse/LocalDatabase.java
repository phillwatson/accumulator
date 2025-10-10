package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.Resolution;
import com.hillayes.accumulator.ConcurrentResolutionRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
public class LocalDatabase implements ConcurrentResolutionRepository.ThreadedDatabase<LocalData> {
    private static final List<LocalData> EMPTY_RANGE = List.of();

    // the repository that will store the fetched data - could be a database
    private final Map<Resolution, List<LocalData>> repository = new HashMap<>();

    @Override
    public void saveBatch(Spliterator<LocalData> aBatch) {
        log.debug("Saving batch of size: {}", aBatch.estimateSize());
        long size = aBatch.estimateSize();
        List<LocalData> list = new ArrayList<>((int) size);
        aBatch.forEachRemaining(list::add);

        if (list.isEmpty()) {
            return;
        }

        synchronized (repository) {
            List<LocalData> resolution = repository
                .computeIfAbsent(list.get(0).getResolution(), k -> new ArrayList<>(list.size()));
            resolution.addAll(list);
        }

        // sleep to simulate latency
        try {
            Thread.sleep(Duration.ofMillis(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  //set the flag back to true
            throw new RuntimeException(e);
        }
        log.debug("Batch Saved");
    }

    @Override
    public List<LocalData> get(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        synchronized (repository) {
            log.debug("Looking for data [resolution: {}, startDate: {}, endDate: {}]",
                aResolution, aStartDate, aEndDate);
            return repository.getOrDefault(aResolution, EMPTY_RANGE).stream()
                .filter(data -> aStartDate.compareTo(data.getEndDate()) <= 0)
                .filter(data -> aEndDate.compareTo(data.getStartDate()) >= 0)
                .sorted(Comparator.comparing(LocalData::getStartDate))
                .toList();
        }
    }
}
