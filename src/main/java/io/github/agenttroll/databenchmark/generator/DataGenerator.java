package io.github.agenttroll.databenchmark.generator;

import io.github.agenttroll.databenchmark.storage.Storage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * Represents a data source which is used to generate data that will be stored
 * to a {@link Storage}.
 */
public interface DataGenerator {
    /**
     * Obtains the name of this data generator, used to identify it in the
     * logging output.
     *
     * @return the identifier name of this generator
     */
    @NonNull String getName();

    /**
     * Generates some given amount of data.
     *
     * @param amount the number of {@link GeneratedData} items to generate
     * @return a collection of {@code amount} generated data items.
     */
    @NonNull List<GeneratedData> generate(int amount);
}
