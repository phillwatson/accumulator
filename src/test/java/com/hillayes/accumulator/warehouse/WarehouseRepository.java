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
package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.DateRangedData;
import com.hillayes.accumulator.Resolution;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A repository for raw data that is to be fed into the accumulation process.
 * Simulates a data warehouse that returns data elements as comma-delimited
 * strings, in the form:
 * {epoch-long},{count-a},{count-b}
 */
@Slf4j
public class WarehouseRepository {
    /**
     * The desired duration to which requests will be divided before submitting
     * them to the warehouse.
     */
    private static final Duration REQUEST_SIZE = Duration.ofMinutes(120);

    /**
     * Requests to the warehouse are divided into smaller requests and issued, as
     * individual tasks, to this executor service.
     * <p>
     * The number of threads in this service should be governed by the number of
     * concurrent requests the warehouse is able to fulfil, and the number of
     * clients it serves.
     */
    private final ExecutorService executorService;

    public WarehouseRepository() {
        this(Executors.newFixedThreadPool(6));
    }

    public WarehouseRepository(ExecutorService aExecutorService) {
        executorService = aExecutorService;
    }

    /**
     * Retrieves the data for the given request from the warehouse, and parses it using the
     * given WarehouseReader before returning the result.
     * <p>
     * The request may be broken into smaller temporal units and a processed by the configured
     * ExecutorService. This is an attempt to reduce the overall time taken to process the
     * whole request, but relies heavily on the Warehouse's ability to process requests
     * concurrently.
     *
     * @param aRequest the request to be completed.
     * @param aReader the reader used to parse the warehouse data.
     * @param <T> the class of Warehouse data to be parsed.
     * @return the ordered collection of data retrieved.
     */
    public <T extends DateRangedData<T>> List<T> get(WarehouseRequest aRequest, WarehouseReader<T> aReader) {
        log.debug("Get warehouse data [request: {}]", aRequest);
        long timer = System.currentTimeMillis();

        // a queue to which futures are gathered on completion
        ExecutorCompletionService<ResponsePart<T>> completionQueue
            = new ExecutorCompletionService<>(executorService);

        // divide request into smaller portions of configured temporal units
        // submit them to the executor service
        // and record how many requests were submitted
        List<WarehouseRequest> requests = aRequest.divide(REQUEST_SIZE);

        // create a callable task to process the request
        // add task to executor and move to completion queue when complete
        requests.forEach(r -> completionQueue.submit(new WarehouseTask<>(r, aReader)));

        int count = requests.size();
        log.debug("Get warehouse data: divided request into parts [count: {}]", count);

        // gather the results of each request
        List<ResponsePart<T>> results = new ArrayList<>(count);
        int totalCount = 0;
        while (count > 0) {
            try {
                // wait for the next request to complete
                log.debug("Get warehouse data: waiting for next part");
                Future<ResponsePart<T>> data = completionQueue.take();
                --count;

                // add request's results to overall results
                ResponsePart<T> part = data.get();
                log.debug("Get warehouse data: retrieved part [size: {}]", part.size());

                // maintain response totals
                totalCount += part.size();
                results.add(part);
            } catch (InterruptedException e) {
                // Preserve interrupt status
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                // the cause is the exception raised by the WarehouseTask
                throw new RuntimeException(e.getCause());
            }
        }

        // return the joined result sets in correct order
        List<T> result = results.stream()
            .sorted() // sort on leading start date
            .map(ResponsePart::getData) // extract the part's data
            .reduce(new ArrayList<>(totalCount), (a, b) -> { // accumulate parts in date order
                a.addAll(b);
                return a;
            });

        log.debug("Got warehouse data [request: {}, size: {}, in: {}ms]", aRequest, totalCount,
            System.currentTimeMillis() - timer);
        return result;
    }

    /**
     * A callable task to retrieve a portion of data from the warehouse, parse it
     * using the given reader and return the resulting collection.
     * <p>
     * Ideally there would be a proxy cache in front of the warehouse so that responses
     * could be cached. This would prevent duplicate, in-flight requests from adding
     * unnecessary load to the warehouse. Alternatively, some sort of caching could be
     * introduced by the WarehouseTask itself - keyed on the WarehouseRequest object.
     *
     * @param <T> the class of data to be retrieved and parsed.
     */
    private static class WarehouseTask<T extends DateRangedData<T>> implements Callable<ResponsePart<T>> {
        private final WarehouseRequest request;
        private final WarehouseReader<T> reader;

        public WarehouseTask(WarehouseRequest aRequest,
                             WarehouseReader<T> aReader) {
            request = aRequest;
            reader = aReader;
        }

        @Override
        public ResponsePart<T> call() {
            log.debug("Fetching warehouse data [request: {}]", request);
            long timer = System.currentTimeMillis();

            Resolution resolution = request.getResolution();
            Instant start = request.getStartDate();
            Instant end = request.getEndDate();

            List<T> result = new ArrayList<>(60);

            // to generate random mock data
            // the seed means requests with the same parameters will get the same data
            Random random = new Random(request.hashCode());

            // create a load of mock data to fill the date range
            StringBuilder line = new StringBuilder();
            while (start.isBefore(end)) {
                int requestCount = random.nextInt(200) + 100;
                int blockCount = random.nextInt(100);

                // create a random line of data
                line.setLength(0);
                line.append(start.getEpochSecond()).append(',')
                    .append(requestCount).append(',')
                    .append(blockCount);

                // parse the data and add to result
                result.add(reader.readLine(request, line.toString(), result.size()));

                // move to next period
                start = resolution.next(start);
            }

            try {
                synchronized (this) {
                    // sleep to simulate latency
                    this.wait(500 + (long) random.nextInt(5) * result.size());
                }
            } catch (InterruptedException ignore) {
            }

            log.debug("Fetched warehouse data [request: {}, size: {}, in: {}ms]", request, result.size(),
                System.currentTimeMillis() - timer);
            return new ResponsePart<>(result);
        }
    }

    /**
     * Records a sub-set of warehouse response data. As the parts must be collated in
     * their date order, this class is intended to improve the sort performance.
     * Implements comparable to compare data sets using the first start date of each
     * set, allowing sets to be sorted in their parts rather than the individual elements.
     *
     * @param <T> the class of DateRangeData held in the parts.
     */
    private static class ResponsePart<T extends DateRangedData<T>> implements Comparable<ResponsePart<T>> {
        private final Instant startDate;
        private final List<T> data;

        public ResponsePart(List<T> aData) {
            startDate = aData.get(0).getStartDate();
            data = aData;
        }

        public List<T> getData() {
            return data;
        }

        public int size() {
            return data.size();
        }

        @Override
        public int compareTo(ResponsePart aOther) {
            return (aOther == null) ? 1 : this.startDate.compareTo(aOther.startDate);
        }
    }
}
