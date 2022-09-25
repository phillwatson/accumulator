package com.hillayes.accumulator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;

/**
 * An interface that allows the ResolutionLoader to be agnostic as to the form of
 * the data it processes, or how it is derived, accumulated and persisted.
 * <p>
 * The implementation may hold additional state data specific to its purpose. For
 * example; the implementation may restrict the scope of the data it is able to
 * supply by some filtering criteria. The ResolutionLoader does not need to be aware
 * of any such filtering.
 *
 * @param <T> the class of DateRangedData that the Helper supplies and persists.
 */
public interface ResolutionRepository<T extends DateRangedData<T>> {
    /**
     * Governs the max number of entries inserted in each batch. As the data is not
     * written in transactions, other clients can read the data as each batch is
     * completed.
     * <p>
     * This value is a balance between writing data in batches large enough to
     * reduce the number of DB requests and small enough to make data available
     * to other clients asap.
     * <p>
     * Bear in mind that this is max size of any batch. The records will be divided
     * into batches of as equal size as possible. So, given a batchSize of 60, a list
     * of 61 items would be divided into 31 and 30.
     */
    int BATCH_SIZE = 100;

    /**
     * Calls the warehouse repository to retrieve data for the given date range
     * at the lowest resolution.
     *
     * @param aStartDate the start of the date range to be retrieved
     * @param aEndDate the end of the date range to be retrieved
     * @return the list of data elements covering the given date range at the
     * requested resolution, in ascending date order
     */
    List<T> fetch(Instant aStartDate, Instant aEndDate);

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
    List<T> get(Resolution aResolution,
                Instant aStartDate,
                Instant aEndDate);

    /**
     * Calls the local repository to save (insert) the given collection of data,
     * and return the same collection.
     * <p>
     * The default implementation will divide the given collection into batches
     * and call the {@link #saveBatch(Spliterator)} for each batch.
     *
     * @param aDataList the data to be inserted into the local repository.
     * @return the same data, or one that contains the equivalent elements.
     */
    default Collection<T> save(Collection<T> aDataList) {
        // divide list into batches of no more than batchSize
        Spliterator<T> split = aDataList.spliterator();
        List<Spliterator<T>> batches = new ArrayList<>();
        batches.add(split);
        while (split.estimateSize() > BATCH_SIZE) {
            batches.addAll(batches.stream()
                .map(Spliterator::trySplit)
                .collect(Collectors.toList()));
        }

        // save each batch
        batches.forEach(this::saveBatch);

        // return results immediately
        return aDataList;
    }

    /**
     * Calls the local repository to save (insert) the given batch of data. This is only
     * called by the default implementation of {@link #save(Collection)}.
     * <p>
     * The implementor may choose to save the batch asynchronously, using an ExecutionService,
     * and without the use of transactions.
     * <p>
     * As the data is immutable, and multiple threads will produce the same data, conflicts
     * aren't important. Whoever writes it first wins.
     * <p>
     * Without transactions, other threads can read the data as each batch is completed.
     *
     * @param aBatch the batch of data to be inserted into the local repository.
     */
    void saveBatch(Spliterator<T> aBatch);

    /**
     * Marks the beginning of the accumulation of elements that together form the
     * element of the given resolution covering the given date range.
     * <p>
     * The implementation must return an instance of Accumulation whose add() method
     * will be repeatedly called to add elements of the lower resolution values.
     * <p>
     * When no more lower-resolution record are available with the given data range
     * the ResolutionLoader will call {@link Accumulation#complete()}. The Accumulation
     * can then create a new record of the given resolution with the accumulated values.
     * <p>
     * The ResolutionLoader gather these new records and, at some point, call the
     * {@link ResolutionRepository#save(Collection)} to persist them.
     *
     * @param aResolution the resolution of the new data element to be created.
     * @param aStartDate the start date of the new data element.
     * @param aEndDate the end date of the new data element.
     * @return an Accumulation that is initialised to start a new data element.
     */
    Accumulation<T> newAccumulation(Resolution aResolution,
                                    Instant aStartDate,
                                    Instant aEndDate);
}
