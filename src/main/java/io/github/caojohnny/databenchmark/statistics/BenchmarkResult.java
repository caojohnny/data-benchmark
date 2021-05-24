package io.github.caojohnny.databenchmark.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of running a benchmark trial.
 */
public class BenchmarkResult {
    /**
     * The number of nanoseconds per millisecond
     */
    public static final long NS_PER_MS = 1_000_000;

    /**
     * A list of all the store times that were recorded by the benchmark, in
     * nanoseconds.
     */
    private final List<Long> iterationStoreNs = new ArrayList<>();
    /**
     * A list of all the query times that were recorded by the benchmark in
     * nanoseconds.
     */
    private final List<Long> iterationQueryNs = new ArrayList<>();

    /**
     * A running total of the time taken to run all store iterations, in
     * nanoseconds
     */
    private double totalStoreNs;
    /**
     * A running total of the time taken to run all query iterations, in
     * nanoseconds
     */
    private double totalQueryNs;

    /**
     * Records the given number of elapsed nanoseconds to run a store operation
     * to this benchmark result.
     *
     * @param nanos the elapsed nanoseconds for a single store iteration
     */
    public void addStoreNs(long nanos) {
        this.iterationStoreNs.add(nanos);
        this.totalStoreNs += nanos;
    }

    /**
     * Records the given number of elapsed nanoseconds to run a query operation
     * to this benchmark result.
     *
     * @param nanos the elapsed nanoseconds for a single query iteration
     */
    public void addQueryNs(long nanos) {
        this.iterationQueryNs.add(nanos);
        this.totalQueryNs += nanos;
    }

    /**
     * Obtains the accumulated time taken to run all store operations,
     * converted to milliseconds.
     *
     * @return the number of milliseconds for the stores to occur
     */
    public double getTotalStoreMs() {
        return this.totalStoreNs / NS_PER_MS;
    }

    /**
     * Obtains the accumulated time taken to run all query operations,
     * converted to milliseconds.
     *
     * @return the number of milliseconds for the queries to occur
     */
    public double getTotalQueryMs() {
        return this.totalQueryNs / NS_PER_MS;
    }
}
