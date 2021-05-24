package io.github.caojohnny.databenchmark.generator;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A {@link RandomDataGenerator}, but instead of generating a random
 * {@link String} as the first element, will re-use the same "key" across calls
 * to {@link #generate(int)}.
 */
public class SameKeyGenerator implements DataGenerator {
    private List<String> keys;

    @Override
    public @NonNull String getName() {
        return "Same Key";
    }

    @Override
    public @NonNull List<GeneratedData> generate(int amount) {
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        if (this.keys == null) {
            this.keys = new ArrayList<>(amount);
            for (int i = 0; i < amount; i++) {
                this.keys.add(UUID.randomUUID().toString());
            }
        }

        List<GeneratedData> dataCollection = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            String randUuid = this.keys.get(i);
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
