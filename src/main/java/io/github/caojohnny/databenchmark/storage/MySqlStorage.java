package io.github.caojohnny.databenchmark.storage;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.github.caojohnny.databenchmark.generator.GeneratedData;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * Represents a MySQL database hosted locally that utilizes a transaction to
 * send insertion statements and indexing to improve query performance.
 */
public class MySqlStorage implements Storage {
    /**
     * The system property key for the MySQL username
     */
    private static final String MYSQL_USER_PROP = "data-benchmark.mysql.user";
    /**
     * The system property key for the MySQL password
     */
    private static final String MYSQL_PASS_PROP = "data-benchmark.mysql.pass";

    /**
     * The {@link DataSource} providing the connection to MySQL
     */
    protected final MysqlDataSource dataSource = new MysqlDataSource();

    @Override
    public @NonNull String getName() {
        return "MySQL";
    }

    @Override
    public void setup(@NonNull Collection<GeneratedData> dataset) throws Exception {
        this.dataSource.setUrl("jdbc:mysql://localhost:3306?serverTimezone=UTC");

        String user = requireNonNull(System.getProperty(MYSQL_USER_PROP),
                "System property unset: -D" + MYSQL_USER_PROP);
        String pass = requireNonNull(System.getProperty(MYSQL_PASS_PROP),
                "System property unset: -D" + MYSQL_PASS_PROP);
        this.dataSource.setUser(user);
        this.dataSource.setPassword(pass);

        String createDb = "CREATE DATABASE IF NOT EXISTS `test`";
        try (Connection con = this.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(createDb)) {
            ps.executeUpdate();
        }
        this.dataSource.setUrl("jdbc:mysql://localhost:3306/test?serverTimezone=UTC");

        String createTable = "CREATE TABLE IF NOT EXISTS `test` (" +
                "`str` VARCHAR(36) PRIMARY KEY, " +
                "`int` INT, " +
                "`double` DOUBLE, " +
                "`float` FLOAT, " +
                "`long` BIGINT" +
                ")";
        try (Connection con = this.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(createTable)) {
            ps.executeUpdate();
        }

        this.storeData(dataset);
    }

    @Override
    public void setupIter() {
    }

    @Override
    public void storeData(@NonNull Collection<GeneratedData> dataCollection)
            throws Exception {
        String sql = "INSERT INTO `test` (`str`, `int`, `double`, `float`, `long`) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE `int` = ?, `double` = ?, `float` = ?, `long` = ?";
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

                        ps.setInt(6, data.getDataAt(1, int.class));
                        ps.setDouble(7, data.getDataAt(2, double.class));
                        ps.setFloat(8, data.getDataAt(3, float.class));
                        ps.setLong(9, data.getDataAt(4, long.class));

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
    public void cleanupIter(@NonNull Collection<GeneratedData> dataCollection) {
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
        String sql = "DROP TABLE `test`";
        try (Connection con = this.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
