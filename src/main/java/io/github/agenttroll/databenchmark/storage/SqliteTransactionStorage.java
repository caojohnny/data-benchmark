package io.github.agenttroll.databenchmark.storage;

import io.github.agenttroll.databenchmark.generator.GeneratedData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;

/**
 * Represents an SQLite data storage medium that wraps all
 * {@link PreparedStatement}s with a transaction so as to attempt to reduce the
 * number of disk writes to improve storage performance in addition to using
 * indexing to improve query performance.
 */
public class SqliteTransactionStorage extends SqliteStorage {
    @Override
    public @NonNull String getName() {
        return "SQLite Transaction";
    }

    @Override
    public void setup(@NonNull Collection<GeneratedData> dataset)
            throws Exception {
        String path = this.databasePath.toAbsolutePath().toString();
        String jdbcUrl = "jdbc:sqlite:" + path;

        SQLiteDataSource dataSource = (SQLiteDataSource) this.dataSource;
        dataSource.setUrl(jdbcUrl);

        String sql = "CREATE TABLE IF NOT EXISTS `test` (" +
                "`str` VARCHAR(36) PRIMARY KEY, " +
                "`int` INT, " +
                "`double` DOUBLE, " +
                "`float` FLOAT, " +
                "`long` BIGINT" +
                ")";
        try (Connection con = this.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }

        this.storeData(dataset);
    }

    @Override
    public void storeData(@NonNull Collection<GeneratedData> dataCollection) throws Exception {
        String sql = "REPLACE INTO `test` (`str`, `int`, `double`, `float`, `long`) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = this.dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
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

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }
}
