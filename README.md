
# Accumulator

---
A utility to supply data at various date range resolutions. The data is typically
historical and immutable; such as stock prices, weather or traffic data.

The historical data is retrieved (in its lowest resolution) from a "warehouse" repository.
The warehouse may be a remote repository from which the retrieval is slow or expensive.

Data at each resolution will be accumulated from that of lower resolutions, supplied
by the local repository. If the local repository holds no data of the lower resolution,
it will be derived from that of the next lower resolution. This process will continue
until the lowest resolution is reached. At which point it will retrieve the data from
the warehouse.

As each resolution is derived it will be persisted to the local repository. Thus,
subsequent requests covering the same data ranges will avoid the overhead of trips to
the remote repository and accumulation.

**IMPORTANT:** The start dates are inclusive. Whereas, the end dates are exclusive.

### Design
The design allows the use of threads to persist the data as it is accumulated and,
also, to retrieve the data from the warehouse. This reduces the impact of those
blocking I/O actions on the overall performance of the task of accumulating the data.

The abstract class `ConcurrentResolutionRepository` implements `ResolutionRepository`
to use Virtual Threads to persist the accumulated results of each Resolution. The
persistence is intended to be performed without the use of transactions that might,
otherwise, obstruct other threads from simultaneously reading and writing data. Key
constraints can be avoided using the SQL "upsert" method (e.g. for Postgres INSERT ...
ON CONFLICT DO NOTHING).

One issue in any design of this type is that concurrent requests for the same time frame
may perform the same accumulation unnecessarily. One solution might be to use a locking
mechanism (semaphore). However, determining where in the time frame to place the locks
would be over complex, and the overall result would be a negative impact on performance.

Fortunately, as the data is immutable, multiple requests for the same time frame can be
assumed to produce the same result; so write conflicts aren't important. Whichever request
writes it first wins. Without the use of blocking transactions, other requests can read
the data as each batch is completed. If the batches are of a size large enough to make
their persistence efficient yet small enough to allow other concurrent requests to make
use of them, the duplicated effort should not be so significant.

Another place where work can be spread over several threads is demonstrated in the test
class `WarehouseRepository`. This class divides a request for data from the remote
warehouse into multiple requests of smaller time-slices, and submits them to the warehouse
using virtual threads. Using multiple requests, of a smaller size, allows the warehouse
to process the requests in parallel. The class then joins the results as they arrive.

A further improvement would be to place a cache in front of the remote warehouse, and
make finer grained requests to the warehouse. The finer grained requests increasing the
likelihood of a cache hit; improving performance and reducing the load on the warehouse.
