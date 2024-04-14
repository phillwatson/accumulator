package com.hillayes.accumulator;

import java.time.Instant;

/**
 * When aggregating data for a given resolutions, the {@link ResolutionLoader}
 * will accumulate the values in the lower resolution records.
 * <p>
 * The accumulation of data for each unit of the resolution begins with a call to
 * {@link ResolutionRepository#newAccumulation(Resolution, Instant, Instant)} to
 * create a new Accumulation instance.
 * <p>
 * As each element within the lower-resolution is obtained it will be passed to the
 * {@link #add(DateRangedData)} method. When desired resolution's date range is
 * complete, the {@link ResolutionLoader} will call {@link #complete()} and a new
 * record for the desired resolution will be created with the accumulated values of
 * the lower-resolution.
 * <p>
 * This interface allows the ResolutionLoader to be agnostic as to what the data
 * contains and how it is to be aggregated. All the ResolutionLoader needs to know
 * is that each element of the data has a start and end date.
 *
 * @param <T> the class of DateRangedData that the Accumulation can process.
 */
public interface Accumulation<T extends DateRangedData> {
    /**
     * Called repeatedly to add each element to be included the accumulation.
     * The implementation should maintain a 'total' of whatever properties of
     * the given element are used to construct a final accumulated result.
     *
     * @param aLowerResRecord the lower-resolution element to be added to the
     * current resolution element.
     */
    void add(T aLowerResRecord);

    /**
     * Called to construct and return an element that is the accumulation of
     * the preceding elements passed to the add() method.
     * <p>
     * On calling this method, the Accumulator should be discarded - or reset.
     *
     * @return the accumulation of the resolution passed to the add() method.
     */
    T complete();
}
