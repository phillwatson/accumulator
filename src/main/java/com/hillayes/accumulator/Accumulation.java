package com.hillayes.accumulator;

/**
 * When aggregating data from lower-resolutions to higher-resolutions, the
 * ResolutionLoader will perform an accumulation of the values in the lower
 * resolution records.
 * <p>
 * This interface allows the ResolutionLoader to be agnostic as to how the data
 * it processes is accumulated. The accumulation of each set of records begins
 * with a call to the Helper.startAccumulation() method. Typically, the
 * Helper will create a new Accumulator instance. A call is made to add()
 * for each lower-resolution record encountered. When no more lower-resolution
 * record are available, the ResolutionLoader will call complete() and a new
 * higher-resolution record will be created with the accumulated values.
 *
 * @param <T> the class of DateRangedData that the Accumulator can process.
 */
public interface Accumulation<T extends DateRangedData<T>> {
    /**
     * Called repeatedly to add each element to be included the accumulation.
     * The implementation should maintain a 'total' of whatever properties of
     * the given element are used to construct a final accumulated result.
     *
     * @param aLowerResRecord the lower-resolution element to be added to the
     * higher-resolution element.
     */
    void add(T aLowerResRecord);

    /**
     * Called to construct and return an element that is the higher-resolution
     * accumulation of the preceding elements passed to the add() method.
     * <p>
     * On calling this method, the Accumulator will be discarded.
     *
     * @return the final, higher resolution equivalent of the lower-resolution
     * elements.
     */
    T complete();
}
