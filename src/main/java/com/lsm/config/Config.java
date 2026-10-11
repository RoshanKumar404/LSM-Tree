package com.lsm.config;

import com.lsm.wal.WalSyncMode;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Immutable-by-convention configuration for a {@code KVEngine} instance.
 *
 * <p>All setters return {@code this} for fluent chaining:
 * <pre>
 *   Config config = new Config()
 *       .setMemTableSizeMB(64)
 *       .setWalSyncMode(WalSyncMode.ALWAYS);
 * </pre>
 *
 * <p>Config should be built once before calling {@code KVEngine.open()} and
 * not modified after that. The engine does not observe config changes at runtime.
 */
public final class Config {

    // ---------- MemTable ----------

    /** Default MemTable size: 32 MB. Same default as RocksDB and LevelDB. */
    public static final long DEFAULT_MEM_TABLE_SIZE_BYTES = 32L * 1024L * 1024L;

    private long memTableSizeBytes = DEFAULT_MEM_TABLE_SIZE_BYTES;

    // ---------- WAL ----------

    /**
     * Default sync mode: ALWAYS.
     * Safest choice -- every acknowledged write survives a crash.
     */
    public static final WalSyncMode DEFAULT_WAL_SYNC_MODE = WalSyncMode.ALWAYS;

    private WalSyncMode walSyncMode = DEFAULT_WAL_SYNC_MODE;

    /**
     * The directory where the engine stores its WAL and (in later phases)
     * SSTables. Defaults to null, meaning the engine uses the {@code dbPath}
     * passed to {@code KVEngine.open()} as the data directory.
     */
    private Path dataDirectory = null;

    // =========================================================================
    // MemTable setters
    // =========================================================================

    /**
     * Returns the configured MemTable size threshold in bytes.
     * When the active MemTable exceeds this size, it will be flushed to disk.
     */
    public long memTableSizeBytes() {
        return memTableSizeBytes;
    }

    /**
     * Sets the MemTable size threshold in bytes.
     *
     * @param memTableSizeBytes must be a positive value
     * @return this, for fluent chaining
     */
    public Config setMemTableSizeBytes(long memTableSizeBytes) {
        if (memTableSizeBytes <= 0) {
            throw new IllegalArgumentException("memTableSizeBytes must be positive, got: " + memTableSizeBytes);
        }
        this.memTableSizeBytes = memTableSizeBytes;
        return this;
    }

    /**
     * Convenience setter: sets the MemTable size threshold in megabytes.
     *
     * @param memTableSizeMB must be a positive value
     * @return this, for fluent chaining
     */
    public Config setMemTableSizeMB(long memTableSizeMB) {
        if (memTableSizeMB <= 0) {
            throw new IllegalArgumentException("memTableSizeMB must be positive, got: " + memTableSizeMB);
        }
        return setMemTableSizeBytes(memTableSizeMB * 1024L * 1024L);
    }

    // =========================================================================
    // WAL setters
    // =========================================================================

    /**
     * Returns the configured WAL sync mode.
     * Controls how aggressively WAL writes are flushed to physical disk.
     */
    public WalSyncMode walSyncMode() {
        return walSyncMode;
    }

    /**
     * Sets the WAL sync mode.
     *
     * <ul>
     *   <li>{@link WalSyncMode#ALWAYS} -- {@code fsync} after every write.
     *       Maximum durability, lower throughput. Use for production data.
     *   <li>{@link WalSyncMode#OS} -- rely on OS page cache flushing.
     *       Higher throughput; a crash may lose the last ~30s of writes.
     *   <li>{@link WalSyncMode#NONE} -- no syncing. For tests or ephemeral data.
     * </ul>
     *
     * @param walSyncMode must not be null
     * @return this, for fluent chaining
     */
    public Config setWalSyncMode(WalSyncMode walSyncMode) {
        this.walSyncMode = Objects.requireNonNull(walSyncMode, "walSyncMode must not be null");
        return this;
    }

    /**
     * Returns the configured data directory, or {@code null} if not set.
     * When null, the engine uses the {@code dbPath} passed to
     * {@code KVEngine.open()} as the data directory.
     */
    public Path dataDirectory() {
        return dataDirectory;
    }

    /**
     * Overrides the directory used to store WAL and SSTable files.
     *
     * <p>This is useful when you want the database index (the {@code dbPath}
     * passed to open) to live on a fast NVMe drive and the bulk data files to
     * live on a larger but slower disk.
     *
     * <p>If not set, defaults to the {@code dbPath} passed to
     * {@code KVEngine.open()}.
     *
     * @param dataDirectory the directory to use; must not be null
     * @return this, for fluent chaining
     */
    public Config setDataDirectory(Path dataDirectory) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory must not be null");
        return this;
    }
}
