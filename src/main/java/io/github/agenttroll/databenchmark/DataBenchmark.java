package io.github.agenttroll.databenchmark;

import io.github.agenttroll.databenchmark.generator.DataGenerator;
import io.github.agenttroll.databenchmark.generator.GeneratedData;
import io.github.agenttroll.databenchmark.generator.RandomDataGenerator;
import io.github.agenttroll.databenchmark.generator.SameKeyGenerator;
import io.github.agenttroll.databenchmark.statistics.BenchmarkResult;
import io.github.agenttroll.databenchmark.storage.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.agenttroll.databenchmark.statistics.BenchmarkResult.NS_PER_MS;
import static java.lang.String.format;

/**
 * The benchmark suite main class/entry point. This is not designed to be a
 * microbenchmark, see a suite like JMH if that is the desired goal. The purpose
 * of data-benchmark is to evaluate the entire system performance of utilizing
 * various different means of storing and persisting data in addition to
 * querying that data later on. This includes time from performing
 * serialization, overhead from casting, autoboxing, etc. in addition to
 * uncontrolled variables such as file system service time.
 */
public class DataBenchmark {
    /**
     * The number of times to run the warm-up for the prior to measurement
     */
    private static final int N_WARMUP = 10;
    /**
     * The number of iterations to perform per benchmark to repeat the test
     */
    private static final int N_ITERATIONS = 100;
    /**
     * The number of existing data in each storage medium before the benchmark
     * data is gathered
     */
    private static final int N_DATASET = 5000;
    /**
     * The number of {@link GeneratedData} entries to store in each benchmark
     */
    private static final int N_ENTRIES = 1000;

    public static void main(String[] args) throws Exception {
        log("Starting DataBenchmark test suite...");
        log("N_WARMUP = %d", N_WARMUP);
        log("N_ITERATIONS = %d", N_ITERATIONS);
        log("N_DATASET = %d", N_DATASET);
        log("N_ENTRIES = %d", N_ENTRIES);
        log("");

        List<DataGenerator> generators = new ArrayList<>();
        // generators.add(new RandomDataGenerator());
        // generators.add(new SameDataGenerator());
        generators.add(new SameKeyGenerator());

        List<Storage> storages = new ArrayList<>();
        storages.add(new YamlStorage());
        storages.add(new JsonStorage());
        storages.add(new SqliteUnsafeStorage());
        storages.add(new MySqlStorage());
        storages.add(new MySqlReplaceStorage());
        // storages.add(new SqliteStorage());
        storages.add(new SqliteTransactionStorage());

        Map<String, BenchmarkResult> results = new LinkedHashMap<>(
                generators.size() * storages.size());
        runSuite(generators, storages, results);

        if (storages.size() > 1) {
            log("Reversing storages to reduce skew...");
            log("");

            Collections.reverse(storages);
            runSuite(generators, storages, results);
        }

        log("--- Results ---");
        for (Map.Entry<String, BenchmarkResult> entry : results.entrySet()) {
            BenchmarkResult result = entry.getValue();

            log("%s - STORE = %.3f ms", entry.getKey(),
                    result.getTotalStoreMs() / N_ITERATIONS / 2);
            log("%s - QUERY = %.3f ms", entry.getKey(),
                    result.getTotalQueryMs() / N_ITERATIONS / 2);
        }
    }

    /**
     * Runs the entire suite of benchmarks with the given collection of
     * {@link DataGenerator}s and {@link Storage}s.
     *
     * @param generators the generators which to use for obtaining data
     * @param storages   the storage mediums which to benchmark
     * @param results    the collection of results to populate with the
     *                   collected benchmark data
     * @throws Exception if the benchmark threw an exception while running
     */
    private static void runSuite(@NonNull Collection<DataGenerator> generators,
                                 @NonNull Collection<Storage> storages,
                                 @NonNull Map<String, BenchmarkResult> results)
            throws Exception {
        for (DataGenerator generator : generators) {
            for (Storage storage : storages) {
                String resultId = format("(%s) %s", generator.getName(), storage.getName());
                BenchmarkResult result = results.computeIfAbsent(resultId,
                        k -> new BenchmarkResult());
                benchmark(storage, generator, result);

                log("");
            }
        }
    }

    /**
     * Performs the benchmark procedure that attempts to store data from the
     * given {@code generator} into the given {@code storage} medium.
     *
     * @param storage   the destination for the generated data.
     * @param generator source of data that shall be stored.
     * @param result    the results from running the benchmark that will be
     *                  populated by the data collected
     * @throws Exception if the benchmark threw an exception while running
     */
    private static void benchmark(@NonNull Storage storage,
                                  @NonNull DataGenerator generator,
                                  @NonNull BenchmarkResult result)
            throws Exception {
        log("Starting benchmark...");
        log("Storage = '%s'", storage.getName());
        log("Data = '%s'", generator.getName());

        DataGenerator rdg = new RandomDataGenerator();
        List<GeneratedData> dataset = rdg.generate(N_DATASET);
        storage.setup(dataset);

        log("Starting warmup...");
        for (int i = 0; i < N_WARMUP; i++) {
            List<GeneratedData> dataCollection = generator.generate(N_ENTRIES);

            int randIdx = ThreadLocalRandom
                    .current()
                    .nextInt(dataCollection.size());
            GeneratedData randData = dataCollection.get(randIdx);

            storage.setupIter();
            storage.storeData(dataCollection);
            boolean querySuccess = storage.queryData(randData);
            if (!querySuccess) {
                throw new IllegalStateException("Failed to query data");
            }

            storage.cleanupIter(dataCollection);
        }

        log("Starting measurement...");
        for (int i = 0; i < N_ITERATIONS; i++) {
            logp("Starting iteration %d... ", i + 1);
            List<GeneratedData> dataCollection = generator.generate(N_ENTRIES);

            storage.setupIter();

            long storeStart = System.nanoTime();
            storage.storeData(dataCollection);
            long storeNs = System.nanoTime() - storeStart;
            result.addStoreNs(storeNs);

            int randIdx = ThreadLocalRandom
                    .current()
                    .nextInt(dataCollection.size());
            GeneratedData randData = dataCollection.get(randIdx);

            long queryStart = System.nanoTime();
            boolean querySuccess = storage.queryData(randData);
            long queryNs = System.nanoTime() - queryStart;
            if (!querySuccess) {
                throw new IllegalStateException("Failed to query data");
            }
            result.addQueryNs(queryNs);

            storage.cleanupIter(dataCollection);

            log("STORE = %.3f ms, QUERY = %.3f ms",
                    (double) storeNs / NS_PER_MS,
                    (double) queryNs / NS_PER_MS);
        }

        storage.cleanup();
    }

    /**
     * Short-cut logging method for printing a full line to {@link System#out}.
     *
     * @param fmt     the format string.
     * @param objects the objects to fill any placeholders in the format
     *                string.
     * @see String#format(String, Object...)
     */
    private static void log(String fmt, Object... objects) {
        logp(fmt, objects);
        System.out.println();
    }

    /**
     * Short-cut logging method for printing messages to {@link System#out}
     * without appending a new line.
     *
     * @param fmt     the format string.
     * @param objects the objects to fill any placeholders in the format
     *                string.
     * @see String#format(String, Object...)
     */
    private static void logp(String fmt, Object... objects) {
        System.out.printf(fmt, objects);
    }
}
