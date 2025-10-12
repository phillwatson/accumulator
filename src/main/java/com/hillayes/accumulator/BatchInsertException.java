package com.hillayes.accumulator;

import java.util.Collection;

/**
 * Raised when an upset of a batch of records fails unexpectedly.
 */
public class BatchInsertException extends RuntimeException {
    private final Collection<?> batch;

    public BatchInsertException(Collection<?> aBatch, Throwable cause) {
        super("Failed to insert batch of accumulation data.", cause);
        batch = aBatch;
    }

    /**
     * Returns the batch that failed.
     */
    public Collection<?> getBatch() {
        return batch;
    }
}
