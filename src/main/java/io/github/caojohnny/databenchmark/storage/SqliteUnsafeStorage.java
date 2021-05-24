package io.github.caojohnny.databenchmark.storage;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * Hypothetical SQLite database using transactions and indexing to improve write
 * and read performance but also turns off journaling and synchronous pragma
 * modes to trade reliability for speeed.
 */
public class SqliteUnsafeStorage extends SqliteTransactionStorage {
    public SqliteUnsafeStorage() {
        SQLiteConfig config = new SQLiteConfig();
        config.setPragma(SQLiteConfig.Pragma.JOURNAL_MODE, "OFF");
        config.setPragma(SQLiteConfig.Pragma.SYNCHRONOUS, "OFF");

        this.dataSource = new SQLiteDataSource(config);
    }

    @Override
    public @NonNull String getName() {
        return "SQLite Unsafe";
    }
}
