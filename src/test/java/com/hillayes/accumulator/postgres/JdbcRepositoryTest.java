package com.hillayes.accumulator.postgres;

import com.hillayes.accumulator.Resolution;
import com.hillayes.accumulator.ResolutionLoader;
import com.hillayes.accumulator.resolutions.DefaultResolution;
import com.hillayes.accumulator.warehouse.LocalData;
import com.hillayes.accumulator.warehouse.LocalRepository;
import com.hillayes.accumulator.warehouse.WarehouseRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcRepositoryTest {
    private static final String SCHEMA_SQL = """
        CREATE SCHEMA IF NOT EXISTS test;
        CREATE TABLE test.accumulation (
            resolution varchar(256) NOT NULL,
            start_date timestamp NOT NULL,
            end_date timestamp NOT NULL,
            units bigint NOT NULL DEFAULT 0,
            blocks bigint NOT NULL DEFAULT 0
        );
        CREATE INDEX idx_resolution_date ON test.accumulation (resolution, start_date);
    """.stripIndent();

    @BeforeAll
    static void setUp() {
        ConnectionSource.init();

        ConnectionSource.withConnection(con -> {
            try (Statement statement = con.createStatement()) {
                statement.execute(SCHEMA_SQL);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterAll
    static void tearDown() {
        ConnectionSource.close();
    }

    @Test
    public void testResolutionConsistency() throws Exception {
        LocalRepository repository = new LocalRepository(new JdbcDatabase(), new WarehouseRepository());
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

        // wait for batches to be saved
        Thread.sleep(Duration.ofSeconds(2));

        // each resolution should equal the same total
        while (resolution != null) {
            Long result = loader.load(resolution, start, end).stream()
                .mapToLong(LocalData::getUnits)
                .sum();
            assertEquals(total, result, "Resolution: " + resolution);

            // drop down to lower resolution
            resolution = resolution.getLower().orElse(null);
        }
    }
}
