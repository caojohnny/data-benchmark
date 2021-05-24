package io.github.caojohnny.databenchmark.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.caojohnny.databenchmark.generator.GeneratedData;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

/**
 * Represents a JSON storage medium that utilizes GSON to serialize input and
 * write to a {@link BufferedWriter}.
 */
public class JsonStorage implements Storage {
    /**
     * The path to the YAML file
     */
    private final Path filePath;

    /**
     * The initial dataset passed through the {@link #setup(Collection)} method
     * to include in the storage output
     */
    private Collection<GeneratedData> dataset;

    public JsonStorage() {
        String workingDir = System.getProperty("user.dir");
        requireNonNull(workingDir, "Cannot resolve current working directory");

        this.filePath = Paths.get(workingDir, "test.json");
    }

    @Override
    public @NonNull String getName() {
        return "JSON";
    }

    /**
     * Stores the given {@code dataCollection} into the {@code root}
     * {@link JsonObject}.
     *
     * @param root           the JSON output which to store the given data
     * @param dataCollection the data which to store in JSON
     */
    private static void toJson(@NonNull JsonObject root,
                               @NonNull Collection<GeneratedData> dataCollection) {
        for (GeneratedData data : dataCollection) {
            JsonObject dataJson = new JsonObject();
            dataJson.addProperty("int", data.getDataAt(1, int.class));
            dataJson.addProperty("double", data.getDataAt(2, double.class));
            dataJson.addProperty("float", data.getDataAt(3, float.class));
            dataJson.addProperty("long", data.getDataAt(4, long.class));

            String str = data.getDataAt(0, String.class);
            root.add(str, dataJson);
        }
    }

    @Override
    public void setup(@NonNull Collection<GeneratedData> dataset) throws Exception {
        Files.createFile(this.filePath);

        this.dataset = Collections.unmodifiableCollection(dataset);
    }

    @Override
    public void setupIter() {
    }

    @Override
    public void storeData(@NonNull Collection<GeneratedData> dataCollection)
            throws Exception {
        JsonObject root = new JsonObject();
        toJson(root, this.dataset);
        toJson(root, dataCollection);

        try (BufferedWriter bw = Files.newBufferedWriter(this.filePath)) {
            bw.write(root.toString());
        }
    }

    @Override
    public boolean queryData(@NonNull GeneratedData randomData) throws Exception {
        String str = randomData.getDataAt(0, String.class);

        Gson gson = new Gson();
        try (BufferedReader br = Files.newBufferedReader(this.filePath)) {
            JsonObject root = gson.fromJson(br, JsonObject.class);
            return root.getAsJsonObject(str) != null;
        }
    }

    @Override
    public void cleanupIter(@NonNull Collection<GeneratedData> dataCollection) {
    }

    @Override
    public void cleanup() throws Exception {
        Files.delete(this.filePath);
    }
}
