package io.github.caojohnny.databenchmark.generator;

import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * This class represents generated data simply as an {@link Object} array.
 */
public class ArrayGeneratedData implements GeneratedData {
    /**
     * The array of generated data given through the constructor
     */
    private final Object[] dataArray;

    /**
     * Creates a new array of generated data initialized with the given values.
     *
     * <p>The array is not allowed to have {@code null elements} and will be
     * cloned upon construction.</p>
     *
     * @param dataArray the array of values
     */
    public ArrayGeneratedData(Object[] dataArray) {
        this.dataArray = dataArray.clone();
    }

    @Override
    public int getLength() {
        return this.dataArray.length;
    }

    @Override
    public <T> @NonNull T getDataAt(int index, @NonNull Class<T> type) {
        if (index < 0 || index >= this.getLength()) {
            throw new IndexOutOfBoundsException("Index is not within bounds");
        }

        return (T) requireNonNull(this.dataArray[index]);
    }
}
