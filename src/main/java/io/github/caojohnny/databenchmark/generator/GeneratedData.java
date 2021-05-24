package io.github.caojohnny.databenchmark.generator;

import io.github.caojohnny.databenchmark.storage.Storage;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents data that was generated from a {@link DataGenerator} which will be
 * stored into a {@link Storage}.
 *
 * <p>Generated data can be thought of as array of data. It is intended to mimic
 * some object containing multiple fields of data of potentially different types
 * that might be stored in different columns of a database table or in a single
 * JSON object, for example.</p>
 */
public interface GeneratedData {
    /**
     * Obtains the length of the data.
     *
     * @return the number of elements in this generated data
     */
    int getLength();

    /**
     * Obtains the data at the given index.
     *
     * @param <T>   the data type
     * @param index the index, between 0 and {@link #getLength()}{@code - 1}.
     * @param type  the class representing the target type of data to cast.
     * @return the data at the given {@code index}, casted to the given
     * {@code type}
     * @throws IllegalArgumentException if the index is out of the bounds of the
     *                                  data.
     * @throws ClassCastException       if the data does not match the given
     *                                  {@code type}.
     */
    @NonNull <T> T getDataAt(int index, @NonNull Class<T> type);
}
