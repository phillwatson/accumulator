/**
 * [Phillip Watson] ("COMPANY") CONFIDENTIAL Unpublished Copyright © 2019-2021 Phillip Watson,
 * All Rights Reserved.
 * <p>
 * NOTICE: All information contained herein is, and remains the property of COMPANY. The
 * intellectual and technical concepts contained herein are proprietary to COMPANY and may be
 * covered by U.K. and Foreign Patents, patents in process, and are protected by trade secret or
 * copyright law. Dissemination of this information or reproduction of this material is strictly
 * forbidden unless prior written permission is obtained from COMPANY. Access to the source code
 * contained herein is hereby forbidden to anyone except current COMPANY employees, managers or
 * contractors who have executed Confidentiality and Non-disclosure agreements explicitly covering
 * such access.
 * <p>
 * The copyright notice above does not evidence any actual or intended publication or disclosure of
 * this source code, which includes information that is confidential and/or proprietary, and is a
 * trade secret, of COMPANY. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR
 * PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF
 * COMPANY IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES.
 * THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY
 * ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL
 * ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.hillayes.accumulator;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * A utility to load data at a given resolution over a given date range. It relies on
 * a ResolutionRepository class to supply and persist the actual data.
 * <p>
 * Data at each resolution will be accumulated from that of lower resolutions, supplied
 * by the repository class. If no data of the lowest resolution is available, the repository
 * will be asked to fetch the data from a remote repository (the warehouse).
 * <p>
 * At each resolution covered, the repository will be asked to persist the accumulated
 * data in the local repository. Thus, subsequent requests for the same data ranges
 * will avoid the overhead of trips to the remote repository and accumulation.
 * <p>
 * IMPORTANT: The start dates mentioned in this class are inclusive. Whereas, the end
 * dates are exclusive.
 *
 * @param <T> the class of DateRangedData to be loaded.
 */
@Slf4j
public class ResolutionLoader<T extends DateRangedData<T>> {
    private final ResolutionRepository<T> repository;

    public ResolutionLoader(ResolutionRepository<T> aRepository) {
        repository = aRepository;
    }

    /**
     * Returns the data, at the given resolution, covering the given date range.
     * <p>
     * If the given end date exceeds the current time, the current time will be used.
     * <p>
     * If the given start and/or end date don't fall exactly on the boundaries of the
     * given resolution, the leading and/or trailing elements will be taken from lower
     * resolutions, but marked with the given resolution. So, for example, a leading
     * or trailing, data element of the resolution HOUR may contain only part of that
     * hour's data.
     *
     * @param aResolution the resolution at which the data is required
     * @param aStartDate the start of the date range to be retrieved, inclusive.
     * @param aEndDate the end of the date range to be retrieved, exclusive.
     * @return the list of data elements covering the given date range at the requested
     * resolution, in ascending date order
     */
    public List<T> load(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        if (log.isDebugEnabled()) {
            log.debug("Beginning loading data [resolution: {}, start: {}, end: {}]",
                aResolution, aStartDate, aEndDate);
        }

        long started = System.currentTimeMillis();
        List<T> result = loadOrFetch(aResolution, aStartDate, min(aEndDate, Instant.now()));

        if (log.isDebugEnabled()) {
            log.debug("Completed loading data [resolution: {}, size: {}, duration: {}ms]",
                result.size(), aResolution, System.currentTimeMillis() - started);
        }
        return result;
    }

    /**
     * A recursive method to retrieve the data for the given date range.
     * <p>
     * Will attempt to read the data, at the given resolution, from the local repository.
     * If the data is not found at that resolution, it will generate the data by recursively
     * reading data at a lower resolution. At the lowest resolution, it will fetch the data
     * from the warehouse repository, and persist that to the local repository.
     *
     * @param aResolution the resolution at which the data is required
     * @param aStartDate the start of the date range to be retrieved, inclusive.
     * @param aEndDate the end of the date range to be retrieved, exclusive.
     * @return the list of data elements covering the given date range at the requested
     * resolution, in ascending date order
     */
    private List<T> loadOrFetch(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        if (log.isDebugEnabled()) {
            log.debug("Loading data [resolution: {}, start: {}, end: {}]",
                aResolution, aStartDate, aEndDate);
        }

        if (aResolution == null) {
            // fetch the lowest resolution data from the warehouse
            log.debug("Asking repository to fetch data [start: {}, end: {}]", aStartDate, aEndDate);
            return repository.fetch(aStartDate, aEndDate);
        }

        long started = System.currentTimeMillis();
        Resolution lowerResolution = aResolution.getLower().orElse(null);

        // adjust requested dates to fit resolution boundaries
        Instant resolutionStartDate = aResolution.roundUp(aStartDate);
        Instant resolutionEndDate = aResolution.roundDown(aEndDate);

        List<T> result = new ArrayList<>();

        // load partial leading data at lower resolution
        if ((lowerResolution != null) && (resolutionStartDate.isAfter(aStartDate))) {
            log.debug("Loading partial leader");
            result.addAll(accumulate(aResolution, aStartDate, resolutionStartDate));
        }

        // if there are any whole boundaries
        if (resolutionStartDate.isBefore(resolutionEndDate)) {
            log.debug("Loading whole boundaries");

            // read main body of period from any data we have in the local database
            if (log.isDebugEnabled()) {
                log.debug("Asking repository to get data [resolution: {}, start: {}, end: {}]",
                    aResolution, resolutionStartDate, resolutionEndDate);
            }
            List<T> body = repository.get(aResolution, resolutionStartDate, resolutionEndDate);

            // add the main body of data to the result
            result.addAll(body);

            // fetch missing periods from lower resolution
            List<T> missing = new ArrayList<>();
            Instant periodStart = resolutionStartDate;
            for (T next : body) {
                if (next.getStartDate().isAfter(periodStart)) {
                    // fetch data for that gap from the lower resolution
                    missing.addAll(accumulate(aResolution, periodStart, next.getStartDate()));
                }

                periodStart = next.getEndDate();
            }

            // if we're missing some from the end
            if (periodStart.isBefore(resolutionEndDate)) {
                // fetch data for that gap from the lower resolution
                missing.addAll(accumulate(aResolution, periodStart, resolutionEndDate));
            }

            // if we filled any gaps
            if (!missing.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Saving data [size: {}, resolution: {}, start: {}, end: {}]",
                        missing.size(), aResolution, resolutionStartDate, resolutionEndDate);
                }
                // save them and add to the overall results
                result.addAll(repository.save(missing));
                Collections.sort(result);
            }
        }

        // load partial trailing data at lower resolution
        if ((lowerResolution != null) && (resolutionEndDate.isBefore(aEndDate))) {
            log.debug("Loading partial trailer");
            result.addAll(accumulate(aResolution, resolutionEndDate, aEndDate));
        }

        if (log.isDebugEnabled()) {
            log.debug("Loaded data entries [resolution: {}, size: {}, duration: {}ms]",
                result.size(), aResolution, System.currentTimeMillis() - started);
        }
        return result;
    }

    /**
     * Retrieves the data, covering the given date range, at the given resolution.
     * It does this by aggregating the data at the resolution immediately lower than
     * the given resolution.
     *
     * @param aResolution the resolution to which we want to aggregate the data
     * @param aStartDate the start of the date range to be aggregated, inclusive.
     * @param aEndDate the end of the date range to be aggregated, exclusive.
     * @return the given data aggregated over the given date range at the given resolution
     */
    private List<T> accumulate(Resolution aResolution, Instant aStartDate, Instant aEndDate) {
        Resolution lowerRes = aResolution.getLower().orElse(null);

        if (log.isDebugEnabled()) {
            log.debug("Accumulating data items [to: {}, from: {}, start: {}, end: {}]",
                aResolution, lowerRes, aStartDate, aEndDate);
        }

        // fetch data from the lower resolution
        // take a 'rewindable' iterator of the lower-res elements
        ListIterator<T> lowerResItems = loadOrFetch(lowerRes, aStartDate, aEndDate).listIterator();

        List<T> result = new ArrayList<>();

        // determine the range of one resolution period
        Instant periodStart = aResolution.roundDown(aStartDate);
        Instant periodEnd = aResolution.next(periodStart);

        // while we haven't reached the end
        while (periodStart.isBefore(aEndDate)) {
            // start a new accumulator for this period
            Accumulation<T> accumulation = repository.newAccumulation(aResolution,
                max(aStartDate, periodStart),
                min(aEndDate, periodEnd));

            // accumulate the lower-res elements that fit within this resolution
            while (lowerResItems.hasNext()) {
                T next = lowerResItems.next();

                // if this data is before this boundary
                if (next.getEndDate().isBefore(periodStart)) {
                    continue;
                }

                // if we've exceeded this period
                if (next.getStartDate().isAfter(periodEnd)) {
                    // rewind iterator for next loop
                    lowerResItems.previous();
                    break;
                }

                accumulation.add(next);
            }

            // set the values in the accumulated entry and add to result
            result.add(accumulation.complete());

            // move one to next resolution period
            periodStart = periodEnd;
            periodEnd = aResolution.next(periodStart);
        }

        if (log.isDebugEnabled()) {
            log.debug("Accumulating data items [to: {}, from: {}, start: {}, end: {}, size: {}]",
                aResolution, lowerRes, aStartDate, aEndDate, result.size());
        }
        return result;
    }

    /**
     * Returns the max of two Instant values.
     */
    private Instant max(Instant a, Instant b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    /**
     * Returns the min of two Instant values.
     */
    private Instant min(Instant a, Instant b) {
        return a.compareTo(b) < 0 ? a : b;
    }
}
