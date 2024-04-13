
# Accumulator

---
A utility to supply data at various date range resolutions. The data is typically historical
and immutable; such as stock prices, weather or traffic data.

The historical data is retrieved (in its lowest resolution) from a "warehouse" repository.
The warehouse may be a remote repository from which the retrieval is slow or expensive.

Data at each resolution will be accumulated from that of lower resolutions, supplied
by the local repository. If the local repository holds no data of the lower resolution,
it will derive it from that of the next lower resolution. This process will continue
until the lowest resolution is reached. At which point it will retrieve the data from
the warehouse.

As each resolution is derived it will be persisted to the local repository. Thus,
subsequent requests covering the same data ranges will avoid the overhead of trips to the
remote repository and accumulation.

**IMPORTANT:** The start dates are inclusive. Whereas, the end dates are exclusive.