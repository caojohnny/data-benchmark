package io.github.agenttroll.databenchmark.generator;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * This data generator produces random data, but unlike
 * {@link RandomDataGenerator}, will return the same random data as if it were a
 * singleton across calls to {@link #generate(int)}.
 */
public class SameDataGenerator implements DataGenerator {
    private final DataGenerator delegate = new RandomDataGenerator();

    private List<GeneratedData> sameData;

    @Override
    public @NonNull String getName() {
        return "Same Data";
    }

    @Override
    public @NonNull List<GeneratedData> generate(int amount) {
        if (this.sameData == null) {
            this.sameData = this.delegate.generate(amount);
        }

        return Collections.unmodifiableList(this.sameData);
    }
}
