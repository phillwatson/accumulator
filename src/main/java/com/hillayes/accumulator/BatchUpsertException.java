package com.hillayes.accumulator;

import java.util.Collection;

/**
 * Raised when an upset of a batch of records fails unexpectedly.
 */
public class BatchUpsertException extends RuntimeException {
    private final Collection<?> batch;

    public BatchUpsertException(Collection<?> aBatch, Throwable cause) {
        super("Failed to upset batch.", cause);
        batch = aBatch;
    }

    /**
     * Returns the batch that failed.
     */
    public Collection<?> getBatch() {
        return batch;
    }
}
