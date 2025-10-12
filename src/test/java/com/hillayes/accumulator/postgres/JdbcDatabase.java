package com.hillayes.accumulator.postgres;

import com.hillayes.accumulator.BatchUpsertException;
import com.hillayes.accumulator.ConcurrentResolutionRepository;
import com.hillayes.accumulator.Resolution;
import com.hillayes.accumulator.warehouse.LocalData;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class JdbcDatabase implements ConcurrentResolutionRepository.ThreadedDatabase<LocalData> {
    private static final String GET_STATEMENT =
        "SELECT resolution, start_date, end_date, units, blocks " +
            "FROM test.accumulation " +
            "WHERE resolution = ? AND ? <= end_date AND ? >= start_date " +
            "ORDER BY resolution, start_date ASC;";

    @Override
    public List<LocalData> get(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        log.debug("Looking for data [resolution: {}, startDate: {}, endDate: {}]",
            aResolution, aStartDate, aEndDate);
        ArrayList<LocalData> entries = new ArrayList<>();
        ConnectionSource.withConnection(con -> {
            try (PreparedStatement statement = con.prepareStatement(GET_STATEMENT)) {
                statement.setString(1, aResolution.name());
                statement.setTimestamp(2, Timestamp.from(aStartDate));
                statement.setTimestamp(3, Timestamp.from(aEndDate));

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        entries.add(LocalData.builder()
                            .resolution(aResolution)
                            .startDate(resultSet.getTimestamp(2).toInstant())
                            .endDate(resultSet.getTimestamp(3).toInstant())
                            .units(resultSet.getLong(4))
                            .blocks(resultSet.getLong(5))
                            .build());
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return entries;
    }

    @Override
    public void saveBatch(Spliterator<LocalData> aBatch) {
        ArrayList<LocalData> dataList = new ArrayList<>();
        aBatch.forEachRemaining(dataList::add);

        saveBatch(dataList);
    }

    public void saveBatch(Collection<LocalData> aBatch) {
        try {
            saveAll(aBatch);
        } catch (Exception e) {
            log.warn("Failed to save batch [size: {}]", aBatch.size());
            throw new BatchUpsertException(aBatch, e);
        }
    }

    private void saveAll(Collection<LocalData> aBatch) {
        if ((aBatch == null) || (aBatch.isEmpty())) {
            log.debug("Skipping empty batch");
            return;
        }
        log.debug("Saving batch [size: {}, resolution: {}]",
            aBatch.size(), aBatch.stream().findFirst().get().getResolution());

        // construct insert statement with placeholders for each row
        String sql = "INSERT INTO test.accumulation (resolution, start_date, end_date, units, blocks) VALUES " +
            createRowPlaceholders(aBatch.size()) +
            " ON CONFLICT DO NOTHING;";

        ConnectionSource.withConnection(con -> {
            try {
                boolean autoCommit = con.getAutoCommit();
                con.setAutoCommit(true);
                try (PreparedStatement statement = con.prepareStatement(sql)) {
                    // add each row to the statement
                    AtomicInteger index = new AtomicInteger(0);
                    for (LocalData row : aBatch) {
                        int offset = index.get() * 5;
                        statement.setString(offset + 1, row.getResolution().name());
                        statement.setTimestamp(offset + 2, Timestamp.from(row.getStartDate()));
                        statement.setTimestamp(offset + 3, Timestamp.from(row.getEndDate()));
                        statement.setLong(offset + 4, row.getUnits());
                        statement.setLong(offset + 5, row.getBlocks());

                        index.incrementAndGet();
                    }

                    statement.execute();
                } finally {
                    con.setAutoCommit(autoCommit);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String createRowPlaceholders(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) { result.append(','); }
            result.append("(?,?,?,?,?)");
        }
        return result.toString();
    }
}
