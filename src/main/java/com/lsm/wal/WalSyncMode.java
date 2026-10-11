package com.lsm.wal;

/**
 * Controls how aggressively the Write-Ahead Log is flushed to physical disk
 * after each append.
 *
 * <p>This is the core durability vs. performance tradeoff of the engine:
 * <ul>
 *   <li>{@link #ALWAYS} -- maximum durability, lowest throughput
 *   <li>{@link #OS}     -- balanced, small window of data loss on crash
 *   <li>{@link #NONE}   -- maximum throughput, no durability guarantee
 * </ul>
 *
 * <p>The default is {@link #ALWAYS}. This is the safest choice and the correct
 * default for a storage engine. Users who need more throughput can opt in to
 * weaker modes explicitly.
 */
public enum WalSyncMode {

    /**
     * Call {@code FileChannel.force(true)} after every WAL append.
     *
     * <p>This is the Java equivalent of POSIX {@code fsync}. It blocks until the
     * OS confirms the data has been written to the physical storage device, not
     * just the OS page cache.
     *
     * <p><b>Guarantee:</b> A write acknowledged by {@code put()} or {@code delete()}
     * will survive a JVM crash, OS panic, or sudden power loss.
     *
     * <p><b>Cost:</b> Each write incurs a full disk sync. On spinning HDDs this can
     * be 5-10ms per write (~100-200 ops/sec). On modern SSDs with FUA support
     * this drops to ~0.1ms (~10,000 ops/sec).
     *
     * <p><b>Use when:</b> the data must never be lost -- financial records, audit
     * logs, configuration state.
     */
    ALWAYS,

    /**
     * Let the OS decide when to flush the page cache to disk. No explicit
     * {@code fsync} is called after each append.
     *
     * <p>The OS typically flushes dirty pages every 30 seconds (Linux default for
     * {@code dirty_expire_centisecs}) or when memory pressure requires it.
     *
     * <p><b>Guarantee:</b> A write will survive a JVM crash but may be lost if the
     * OS crashes or the machine loses power before the OS flushes its page cache.
     *
     * <p><b>Cost:</b> No sync overhead per write -- throughput is bounded only by
     * the OS page cache write speed, which is effectively RAM speed.
     *
     * <p><b>Use when:</b> throughput matters more than strict durability -- analytics
     * pipelines, caches, development and testing environments.
     */
    OS,

    /**
     * Never call {@code FileChannel.force()} and do not rely on OS flushing.
     * Data is written to the WAL file but not guaranteed to reach disk at any
     * predictable time.
     *
     * <p><b>Guarantee:</b> None. Data may be lost even across a clean JVM restart
     * if the OS has not flushed its page cache.
     *
     * <p><b>Cost:</b> Effectively zero sync overhead.
     *
     * <p><b>Use when:</b> the WAL is only needed for crash detection in tests, or
     * when the application can afford to lose all recent writes. Do not use in
     * production for data that matters.
     */
    NONE
}
