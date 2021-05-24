package io.github.caojohnny.databenchmark.storage;

import io.github.caojohnny.databenchmark.generator.GeneratedData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Represents a YAML flat-file that uses SnakeYAML to serialize and write data
 * to a {@link BufferedWriter}.
 */
public class YamlStorage implements Storage {
    /**
     * The path to the YAML file
     */
    private final Path filePath;

    /**
     * The initial dataset passed through the {@link #setup(Collection)} method
     * to include in the storage output
     */
    private Collection<GeneratedData> dataset;

    public YamlStorage() {
        String workingDir = System.getProperty("user.dir");
        requireNonNull(workingDir, "Cannot resolve current working directory");

        this.filePath = Paths.get(workingDir, "test.yml");
    }

    @Override
    public @NonNull String getName() {
        return "YAML";
    }

    /**
     * Inserts the items in the given {@code dataCollection} into a YAML map
     * containing entries with a nested YAML map to the {@link GeneratedData}
     * and populates the {@code yamlMap} with the root.
     *
     * @param yamlMap        the map to populate with the YAML entries
     * @param dataCollection the collection of data to insert into the
     *                       {@code yamlMap}
     */
    private static void toYamlMap(@NonNull Map<String, Object> yamlMap,
                                  @NonNull Collection<GeneratedData> dataCollection) {
        for (GeneratedData data : dataCollection) {
            Map<String, Object> dataSection =
                    new LinkedHashMap<>(data.getLength());
            dataSection.put("int", data.getDataAt(1, int.class));
            dataSection.put("double", data.getDataAt(2, double.class));
            dataSection.put("float", data.getDataAt(3, float.class));
            dataSection.put("long", data.getDataAt(4, long.class));

            String str = data.getDataAt(0, String.class);
            yamlMap.put(str, dataSection);
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
    public void storeData(@NonNull Collection<GeneratedData> dataCollection) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>(
                this.dataset.size() + dataCollection.size());
        toYamlMap(root, this.dataset);
        toYamlMap(root, dataCollection);

        Yaml yaml = new Yaml();
        try (BufferedWriter bw = Files.newBufferedWriter(this.filePath)) {
            yaml.dump(root, bw);
        }
    }

    @Override
    public boolean queryData(@NonNull GeneratedData randomData) throws Exception {
        String str = randomData.getDataAt(0, String.class);

        Yaml yaml = new Yaml();
        try (BufferedReader br = Files.newBufferedReader(this.filePath)) {
            Map<String, Object> root = yaml.load(br);
            return root.get(str) != null;
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
