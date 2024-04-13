package com.hillayes.accumulator;

import com.hillayes.accumulator.mocks.MockDateRangedData;
import com.hillayes.accumulator.mocks.MockResolutionRepository;
import com.hillayes.accumulator.resolutions.DefaultResolution;
import com.hillayes.accumulator.warehouse.WarehouseRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolutionLoaderTest {
    @Test
    public void test() {
        MockResolutionRepository repository = new MockResolutionRepository();
        ResolutionLoader<MockDateRangedData> loader = new ResolutionLoader<>(repository);

        Resolution resolution = DefaultResolution.DAY;
        Instant end = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(1, ChronoUnit.DAYS);
        Instant start = end.minus(3, ChronoUnit.DAYS);

        List<MockDateRangedData> data =
            loader.load(resolution, start, end);

        assertEquals(3, data.size());

        // sum the values for all elements
        Long total = data.stream()
            .map(MockDateRangedData::getValue)
            .reduce(0L, Long::sum);

        // each resolution should equal the same total
        while (resolution != null) {
            Long result = repository.getAll(resolution).stream()
                .map(MockDateRangedData::getValue)
                .reduce(0L, Long::sum);
            assertEquals(total, result);

            // drop down to lower resolution
            resolution = resolution.getLower().orElse(null);
        }
    }
}
