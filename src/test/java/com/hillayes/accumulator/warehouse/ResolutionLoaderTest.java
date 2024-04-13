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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolutionLoaderTest {
    @Test
    public void test() {
        WarehouseRepository warehouseRepository = new WarehouseRepository();
        LocalRepository repository = new LocalRepository(warehouseRepository);
        ResolutionLoader<LocalData> loader = new ResolutionLoader<>(repository);

        Resolution resolution = DefaultResolution.DAY;
        Instant end = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(1, ChronoUnit.DAYS);
        Instant start = end.minus(3, ChronoUnit.DAYS);

        List<LocalData> data =
            loader.load(resolution, start, end);

        assertEquals(3, data.size());

        // sum the values for all elements
        Long total = data.stream()
            .map(LocalData::getUnits)
            .reduce(0L, Long::sum);

        Awaitility.await().pollInterval(Duration.ofMillis(100)).atMost(Duration.ofSeconds(5)).until(() -> {
            List<LocalData> result = repository.get(DefaultResolution.MINUTE, start, end);
            return result.size() == 4320;
        });

        // each resolution should equal the same total
        while (resolution != null) {
            Long result = repository.get(resolution, start, end).stream()
                .map(LocalData::getUnits)
                .reduce(0L, Long::sum);
            assertEquals(total, result, "Resolution: " + resolution);

            // drop down to lower resolution
            resolution = resolution.getLower().orElse(null);
        }
    }
}
