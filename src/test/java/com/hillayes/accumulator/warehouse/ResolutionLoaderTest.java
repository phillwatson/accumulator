package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.Resolution;
import com.hillayes.accumulator.ResolutionLoader;
import com.hillayes.accumulator.resolutions.DefaultResolution;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResolutionLoaderTest {
    @Test
    public void testResolutionConsistency() {
        LocalRepository repository = new LocalRepository(new WarehouseRepository());
        ResolutionLoader<LocalData> loader = new ResolutionLoader<>(repository);

        Resolution resolution = DefaultResolution.DAY;
        Instant end = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(1, ChronoUnit.DAYS);
        Instant start = end.minus(3, ChronoUnit.DAYS);

        List<LocalData> data = loader.load(resolution, start, end);

        assertEquals(3, data.size());

        // sum the values for all elements
        Long total = data.stream()
            .mapToLong(LocalData::getUnits)
            .sum();

        // each resolution should equal the same total
        while (resolution != null) {
            Long result = repository.get(resolution, start, end).stream()
                .mapToLong(LocalData::getUnits)
                .sum();
                assertEquals(total, result, "Resolution: " + resolution);

            // drop down to lower resolution
            resolution = resolution.getLower().orElse(null);
        }
    }

    @Test
    public void testResolutionOrder() {
        LocalRepository repository = new LocalRepository(new WarehouseRepository());
        ResolutionLoader<LocalData> loader = new ResolutionLoader<>(repository);

        Resolution resolution = DefaultResolution.MINUTE;
        Instant end = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(1, ChronoUnit.DAYS);
        Instant start = end.minus(2, ChronoUnit.DAYS);

        List<LocalData> data = loader.load(resolution, start, end);

        // data is in ascending date order
        AtomicReference<LocalData> prev = new AtomicReference<>();
        data.forEach(entry -> {
            if (prev.get() != null) {
                assertTrue(prev.get().getStartDate().isBefore(entry.getStartDate()));
            }
            prev.set(entry);
        });
    }
}
