package io.github.agenttroll.databenchmark.storage;

import io.github.agenttroll.databenchmark.generator.GeneratedData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * Represents an SQLite database storage medium which naively executes
 * individual {@link PreparedStatement}s for each {@link GeneratedData} item and
 * does not use indexing.
 */
public class SqliteStorage implements Storage {
    /**
     * The {@link DataSource} for the SQLite connection
     */
    protected DataSource dataSource;

    /**
     * The file system path to the SQLite database file
     */
    protected final Path databasePath;

    public SqliteStorage() {
        this.dataSource = new SQLiteDataSource();

        String workingDir = System.getProperty("user.dir");
        requireNonNull(workingDir, "Cannot resolve working directory");

        this.databasePath = Paths.get(workingDir, "test.db");
    }

    @Override
    public @NonNull String getName() {
        return "SQLite";
    }

    @Override
    public void setup(@NonNull Collection<GeneratedData> dataset)
            throws Exception {
        String path = this.databasePath.toAbsolutePath().toString();
        String jdbcUrl = "jdbc:sqlite:" + path;

        SQLiteDataSource dataSource = (SQLiteDataSource) this.dataSource;
        dataSource.setUrl(jdbcUrl);

        String createTable = "CREATE TABLE IF NOT EXISTS `test` (" +
                "`str` VARCHAR(36), " +
                "`int` INT, " +
                "`double` DOUBLE, " +
                "`float` FLOAT, " +
                "`long` BIGINT" +
                ")";
        try (Connection con = this.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(createTable)) {
            ps.executeUpdate();
        }

        String replace = "REPLACE INTO `test` (`str`, `int`, `double`, `float`, `long`) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = this.dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
                for (GeneratedData data : dataset) {
                    try (PreparedStatement ps = con.prepareStatement(replace)) {
                        ps.setString(1, data.getDataAt(0, String.class));
                        ps.setInt(2, data.getDataAt(1, int.class));
                        ps.setDouble(3, data.getDataAt(2, double.class));
                        ps.setFloat(4, data.getDataAt(3, float.class));
                        ps.setLong(5, data.getDataAt(4, long.class));

                        ps.executeUpdate();
                    }

                }

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void setupIter() {
    }

    @Override
    public void storeData(@NonNull Collection<GeneratedData> dataCollection)
            throws Exception {
        String sql = "REPLACE INTO `test` (`str`, `int`, `double`, `float`, `long`) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = this.dataSource.getConnection()) {
            for (GeneratedData data : dataCollection) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, data.getDataAt(0, String.class));
                    ps.setInt(2, data.getDataAt(1, int.class));
                    ps.setDouble(3, data.getDataAt(2, double.class));
                    ps.setFloat(4, data.getDataAt(3, float.class));
                    ps.setLong(5, data.getDataAt(4, long.class));

                    ps.executeUpdate();
                }
            }
        }
    }

    @Override
    public boolean queryData(@NonNull GeneratedData randomData) throws Exception {
        String str = randomData.getDataAt(0, String.class);
        String sql = "SELECT `int`, `double`, `float`, `long` FROM `test` WHERE `str` = ?";
        try (Connection con = this.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, str);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void cleanupIter(@NonNull Collection<GeneratedData> dataCollection)
            throws Exception {
        /* String sql = "DELETE FROM `test` WHERE `str` = ?";
        try (Connection con = this.dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
                for (GeneratedData data : dataCollection) {
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        String str = data.getDataAt(0, String.class);
                        ps.setString(1, str);

                        ps.executeUpdate();
                    }
                }

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } */
    }

    @Override
    public void cleanup() throws Exception {
        Files.delete(this.databasePath);
    }
}
