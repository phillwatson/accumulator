package com.hillayes.accumulator;

import java.time.Instant;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An implementation of ResolutionRepository that offloads the persistence of
 * the batch of accumulated data to a separate thread.
 *
 * @param <T> the data type of the batch to be persisted.
 */
public abstract class ConcurrentResolutionRepository<T extends DateRangedData> implements ResolutionRepository<T> {
    private final ExecutorService executorService;
    protected final ThreadedDatabase<T> database;

    public ConcurrentResolutionRepository(ThreadedDatabase<T> aBatchWriter) {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.database = aBatchWriter;
    }

    @Override
    public List<T> get(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        return database.get(aResolution, aStartDate, aEndDate);
    }

    @Override
    public final void saveBatch(Spliterator<T> aBatch) {
        executorService.submit(() -> database.saveBatch(aBatch));
    }

    /**
     * An interface that allows the ResolutionRepository offload the persistence
     * of the batch of accumulated data to a separate thread.
     *
     * @param <D> the data type of the batch to be persisted.
     */
    public interface ThreadedDatabase<D> {
        /**
         * Calls the local repository to retrieve data for the given date range
         * at the given resolution.
         *
         * @param aResolution the resolution at which the data is required
         * @param aStartDate the start of the date range to be retrieved
         * @param aEndDate the end of the date range to be retrieved
         * @return the list of data elements covering the given date range at the
         * requested resolution, in ascending date order
         */
        List<D> get(Resolution aResolution, Instant aStartDate, Instant aEndDate);

        /**
         * Calls the local repository to save (insert) the given batch of data.
         * This will be called within a thread managed by the ResolutionRepository.
         * For performance reasons the intention is that it persist the data without
         * using a transaction.
         *
         * @param aBatch the batch of data to be inserted into the local repository.
         */
        void saveBatch(Spliterator<D> aBatch);
    }
}
