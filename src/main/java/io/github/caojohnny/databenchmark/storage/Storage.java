package io.github.caojohnny.databenchmark.storage;

import io.github.caojohnny.databenchmark.generator.GeneratedData;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;

/**
 * Represents some data storage target used for persisting data.
 */
public interface Storage {
    /**
     * Obtains the name of this storage.
     *
     * @return the name describing the storage, such as {@code MySQL} or
     * {@code SQLite}, for example.
     */
    @NonNull String getName();

    /**
     * Performs the one-time setup procedure. This creates any necessary
     * databases or file handles to prepare the data storage medium to be
     * measured.
     *
     * @param dataset the initial data that will be stored into this medium
     *                to make the size of the storage more realistic
     * @throws Exception if any error occurs performing the setup
     */
    void setup(@NonNull Collection<GeneratedData> dataset) throws Exception;

    /**
     * Performs setup prior to the iteration loop being run to measure the
     * time which it takes to store data.
     *
     * @throws Exception if any error occurs performing the setup
     */
    void setupIter() throws Exception;

    /**
     * Executes the storage procedure and writes the data from the given
     * {@code dataCollection} to whatever storage medium this represent.
     *
     * @param dataCollection the collection of data to be written.
     * @throws Exception if any error occurs performing the storage
     */
    void storeData(@NonNull Collection<GeneratedData> dataCollection) throws
            Exception;

    /**
     * Executes a query procedure from this storage medium.
     *
     * @param randomData some random data from the {@link GeneratedData}
     *                   collection last provided to the
     *                   {@link #storeData(Collection)} procedure
     * @return {@code true} if the query succeeds and is able to find the
     * random data (should always return true)
     * @throws Exception if any error occurs performing the query
     */
    boolean queryData(@NonNull GeneratedData randomData) throws Exception;

    /**
     * Performs the cleanup subsequent to each iteration iteration.
     *
     * @param dataCollection the same collection of data that was passed to
     *                       the {@link #storeData(Collection)} procedure used
     *                       to remove any data that was stored so as to prevent
     *                       previous iterations from influencing the results of
     *                       subsequent iterations
     * @throws Exception if any error occurs performing the cleanup
     */
    void cleanupIter(@NonNull Collection<GeneratedData> dataCollection)
            throws Exception;

    /**
     * Performs the one-time cleanup procedure at the end of the entire
     * measurement.
     *
     * @throws Exception if any error occurs performing the cleanup
     */
    void cleanup() throws Exception;
}
