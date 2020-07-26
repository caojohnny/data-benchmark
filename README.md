# `data-benchmark`

I commonly see questions asked regarding the best method of storing or
persisting data. Generally, for Bukkit plugins, this will probably be between
something like MySQL, YAML, JSON or a cross between a database and flat-file
like SQLite. `data-benchmark` is a benchmark suite for gathering empirical
performance data on different ways of data persistence to help inform your
decision.

# Implementation

The two main abstractions are `DataGenerator`, which is the source of data to be
inserted and `Storage`, which represents some method of storing data that will
be measured by the benchmark. The different generators and storage methods are
combined and each tested. The test procedure first inserts some specified
number of data into the storage first to simulate there being existing data.
The test performs a warm-up and then performs a second measurement round. Both
will insert/upsert a constant number of entries and then select a single random
entry for the store and query operations, respectively.

This is not designed as a microbenchmark. The benchmark results are supposed to
be an estimate of the "system" performance, that is, how different storage
methods compare relative to one another rather than being an absolute time.
[JMH](https://openjdk.java.net/projects/code-tools/jmh/) is a tool you should
use if that is something that you are concerned with. `data-benchmark` includes
parsing and serialization times in addition to external influences such as
garbage collection and file system response times. These values change from
system to system depending on your hardware such as CPU and your physical
storage mediums like an SSD or HDD. The inclusion of these other variables in
the final time is intentional. With that being said, for tests that include
more than one `Storage` method, the tests will be inverted and run a second time
and then averaged minimize (but not eliminate) skew due to running the tests on
the same JVM.

# Build

```
git clone git@github.com:AgentTroll/data-benchmark.git
cd data-benchmark
./gradlew clean shadowJar
```

# Usage

The benchmark executable is bundled as a standard JAR file, which can be found
in the `build/libs` folder after following the build instructions above. The
benchmark utilizes 2 JVM variables for the credentials to a localhost MySQL
server, which can be specified in the following manner:

```
java -Ddata-benchmark.mysql.user=root -Ddata-benchmark.mysql.pass=password -jar DataBenchmark.jar 
```

Where `root` ill be replaced with the MySQL username and `password` will be
replaced with the MySQL password.

# Demo

``` 
N_WARMUP = 10
N_ITERATIONS = 100
N_DATASET = 5000
N_ENTRIES = 1000

--- Results ---
(Same Key) YAML - STORE = 133.976 ms
(Same Key) YAML - QUERY = 166.254 ms
(Same Key) JSON - STORE = 15.065 ms
(Same Key) JSON - QUERY = 11.155 ms
(Same Key) SQLite Unsafe - STORE = 13.992 ms
(Same Key) SQLite Unsafe - QUERY = 0.464 ms
(Same Key) MySQL - STORE = 310.930 ms
(Same Key) MySQL - QUERY = 3.873 ms
(Same Key) MySQL REPLACE - STORE = 292.531 ms
(Same Key) MySQL REPLACE - QUERY = 3.325 ms
(Same Key) SQLite Transaction - STORE = 246.823 ms
(Same Key) SQLite Transaction - QUERY = 0.748 ms
```

# Discussion

The results from the demo were obtained on my desktop computer, which runs an
Intel i3 and a standard 7200 RPM HDD. Between the two flat-file formats, it
seems that using JSON is significantly faster than YAML. It is worth noting
that this may be down simply to how *much* data is being produced rather than
any inherent differences in library performance, although the latter could
possibly be the case as well. It could be that GSON simply emits fewer
characters for the same data compared to SnakeYAML, though I have attempted to
write as similar serializing code as I could between the two. If you are limited
between these two choices, I would select JSON.

Between the databases, things get a little more interesting. The way I've
written the JDBC code changes across the board for the databases, so let's run
through the differences real quick:

  * `MySQL` - Uses indexing, uses transactions, uses 
  `INSERT INTO ... ON DUPLICATE KEY UPDATE`
  * `MySQL REPLACE` - Uses indexing, uses transactions, uses `REPLACE` instead
  * `SQLite` (not in the results) - No indexing, no transactions, uses `REPLACE`
  * `SQLite Unsafe` - Uses indexing, uses transactions, uses `REPLACE`, disables
  any journaling and file system synchronization
  * `SQLite Transaction` - Uses indexing, uses transactions, uses `REPLACE`,
  uses journaling and synchronizes with the file system
  
The reason I've decided to use `REPLACE` is that it is really common for vanilla
Spigot plugins to support Spigot 1.8.8, which has an old SQLite driver (like 3.7
ish old), that does not support `UPSERT` yet. That being said, if the results
between `MySQL` and `MySQL REPLACE` say anything, this might not really matter
anyways.

Either way, between using `MySQL` and `MySQL REPLACE`, it seems that there is
practically no difference. Though the times suggest that the
`INSERT INTO ... ON DUPLICATE KEY UPDATE` version is slightly slower, I would
be hesitant to say that would be the case with just 19 ms separating the two.
It is well within the margin of error considering the amount of jitter the
measurement times indicate - this is why it is important to look at all of the
data!

Between MySQL and SQLite, SQLite is a clear winner. SQLite's measurement times
were consistently hitting 250 ms each store and the query times were ludicrously
low; much lower than even the flat-file query times! We can see that when
getting rid of any safety features built-into SQLite, both storage and query
times are capable of outperforming flat-file formats, although I would be
hesitant to use this in production.

SQLite without using transactions was many orders of a magnitude slower than
with transactions, so I cut it short at the 30 minute mark and re-ran the test
without it. The same effect was repeated with MySQL, so the takeaway here is
always use a transaction to wrap bulk insertions.

Long story short, if I had to select one out of the list of options, I would
honestly probably go with SQLite, with JSON coming up a close second *from
purely a performance standpoint*. Although JSON is compellingly faster when it
comes to storage performance, I would argue that for the safety SQLite offers,
the ability to modify in bulk using the SQLite client and the fact that the
storage performance doesn't actually matter as much as load performance, I would
say that the performance hit is worth it for the query performance. The fact is,
people notice when you have long load times, so if you have a large amount of
data to query such as from a database worth of player data, then I would rather
retrieve that faster than I can store it.

# Credits

Built with [IntelliJ IDEA](https://www.jetbrains.com/idea/)

Utilizes:

  * [shadow](https://github.com/johnrengelman/shadow)
  * [gson](https://github.com/google/gson)
  * [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc)
  * [SnakeYAML](https://bitbucket.org/asomov/snakeyaml/)
  * [MySQL Connector](https://github.com/mysql/mysql-connector-j)
  * [Checker Framework](https://checkerframework.org/)
  * [error-prone](https://errorprone.info/)
