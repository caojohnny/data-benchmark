package io.github.agenttroll.databenchmark.storage;

import io.github.agenttroll.databenchmark.generator.GeneratedData;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;

public class MySqlReplaceStorage extends MySqlStorage {
    @Override
    public @NonNull String getName() {
        return "MySQL REPLACE";
    }

    @Override
    public void storeData(@NonNull Collection<GeneratedData> dataCollection)
            throws Exception {
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
