package io.github.caojohnny.databenchmark.generator;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a data generator which generates a random array of 5 objects in
 * the following order: a {@link String}, {@code int}, {@code double},
 * {@code float} and {@code long}.
 */
public class RandomDataGenerator implements DataGenerator {
    @Override
    public @NonNull String getName() {
        return "Random Data";
    }

    @Override
    public @NonNull List<GeneratedData> generate(int amount) {
        List<GeneratedData> dataCollection = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            ThreadLocalRandom tlr = ThreadLocalRandom.current();

            String randUuid = UUID.randomUUID().toString();
            int randInteger = tlr.nextInt();
            double randDouble = tlr.nextDouble();
            float randFloat = tlr.nextFloat();
            long randLong = tlr.nextLong();

            Object[] dataArray = {randUuid, randInteger, randDouble, randFloat,
                    randLong};
            GeneratedData data = new ArrayGeneratedData(dataArray);
            dataCollection.add(data);
        }

        return Collections.unmodifiableList(dataCollection);
    }
}
