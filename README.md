# jLSM: Embedded LSM-Tree Key-Value Storage Engine

A crash-resilient, embeddable Log-Structured Merge-tree (LSM) key-value storage engine implemented in pure Java. Designed for write-heavy workloads, `jLSM` substitutes expensive random disk mutations with sequential appends, in-memory SkipLists, and immutable on-disk sorted runs.

---

## Architecture Overview

```
                      Write Path (put / delete)
                                  │
                  ┌───────────────┴───────────────┐
                  ▼                               ▼
        [ Write-Ahead Log (WAL) ]       [ In-Memory MemTable ]
        Sequential disk append          ConcurrentSkipListMap
        (ACID crash durability)         (O(log N) sorted buffer)
                                                  │
                                                  │ Flush threshold reached
                                                  │ (e.g., 32 MB)
                                                  ▼
                                      [ On-Disk SSTable Files ]
                                      ├── In-Memory Bloom Filter (O(1) checks)
                                      ├── Sparse Index Block (Binary search)
                                      └── Data Block (Sorted key-value chunks)
                                                  │
                                                  ▼
                                      [ Background Compaction ]
                                      K-Way merge sort, purges tombstones

```

### Core Design Principles

* **Sequential Write Performance:** Writes bypass immediate disk reorganization. Every operation appends to a WAL and inserts into an in-memory `ConcurrentSkipListMap`.
* **Zero Random In-Place Updates:** Deletions are appended as soft-deletion markers (**tombstones**) and resolved during compaction.
* **Bounded Read Amplification:** Point lookups verify an in-memory **Bloom Filter** before touching disk. An in-memory **Sparse Index** bounds disk reads to a single target block seek.
* **Crash Resilience:** Every write can be forced to physical non-volatile storage via OS `fsync` (`FileChannel.force()`), enabling exact state reconstruction across unexpected JVM termination.

---

## Features

* **Embedded Engine:** Runs in-process within your application JVM; no external database daemons required.
* **Lock-Free Concurrency:** Utilizes `ConcurrentSkipListMap` and atomic primitives for concurrent write and read operations.
* **Durable Write-Ahead Log (WAL):** Binary-encoded append-only log with configurable sync guarantees.
* **SSTable Storage:** Immutable binary files containing sorted key-value pairs, sparse indexing, and block offsets.
* **Probabilistic Filtering:** Per-SSTable Bloom filters with low false-positive rates to eliminate superfluous disk I/O.
* **Size-Tiered Compaction:** Background worker merging multiple sorted runs to reclaim disk space and purge obsolete tombstones.

---

## Getting Started

### Prerequisites

* **Java Development Kit (JDK):** Version 17 or higher
* **Build Tool:** Maven 3.8+ or Gradle 8.0+

### Installation

Clone the repository and build the project:

```bash
git clone https://github.com/RoshanKumar404/LSM-Tree.git
cd jLSM
mvn clean package

```

---

## Quickstart & Usage

```java
import com.lsm.api.KVEngine;
import com.lsm.config.Config;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        Path dbPath = Paths.get("./data/test_db");
        
        // 1. Open database instance
        Config config = new Config()
            .setMemTableSizeMB(32)
            .setSparseIndexInterval(128); // Index every 128th key
            
        KVEngine db = KVEngine.open(dbPath, config);

        // 2. Put entries
        byte[] key = "user:101".getBytes(StandardCharsets.UTF_8);
        byte[] val = "{\"name\":\"Roshan\",\"role\":\"Engineer\"}".getBytes(StandardCharsets.UTF_8);
        db.put(key, val);

        // 3. Get entry
        byte[] retrieved = db.get(key);
        if (retrieved != null) {
            System.out.println("Found: " + new String(retrieved, StandardCharsets.UTF_8));
        }

        // 4. Delete entry (writes a tombstone)
        db.delete(key);
        assert db.get(key) == null;

        // 5. Clean close (flushes active state, releases file locks)
        db.close();
    }
}

```

---

## Internal Specifications

### 1. Write-Ahead Log (WAL) Binary Format

Every write appends a framed binary record to the WAL file before modifying memory:

| Field | Size | Type | Description |
| --- | --- | --- | --- |
| `CRC32` | 4 bytes | uint32 | Checksum of payload for corruption detection |
| `Key Length` | 4 bytes | int32 | Length of key ($K$) |
| `Value Length` | 4 bytes | int32 | Length of value ($V$), `-1` denotes Tombstone |
| `Key Bytes` | $K$ bytes | byte[] | Raw key payload |
| `Value Bytes` | $V$ bytes | byte[] | Raw value payload (omitted if tombstone) |

### 2. SSTable (Sorted String Table) Format

When a MemTable fills, it flushes sequentially to disk as an immutable SSTable file:

```
┌────────────────────────────────────────────────────────┐
│                      DATA BLOCK                        │
│  [Record 1] [Record 2] [Record 3] ... [Record N]       │
├────────────────────────────────────────────────────────┤
│                     SPARSE INDEX                       │
│  [Key 0 -> Offset 0] [Key 128 -> Offset 4096] ...      │
├────────────────────────────────────────────────────────┤
│                     BLOOM FILTER                       │
│  BitSet array + Hash seed count                        │
├────────────────────────────────────────────────────────┤
│                        FOOTER                          │
│  Index Offset (8B) | Filter Offset (8B) | Magic (4B)   │
└────────────────────────────────────────────────────────┘

```

---

## Directory Structure

```
jLSM/
├── src/
│   ├── main/java/com/lsm/
│   │   ├── api/             # Public user API and interfaces
│   │   ├── config/          # Engine operational parameters
│   │   ├── memtable/        # In-memory ConcurrentSkipListMap wrapper
│   │   ├── wal/             # Append-only Write-Ahead Log & crash recovery
│   │   ├── sstable/         # SSTable writer, reader, and sparse index
│   │   ├── filter/          # Bloom filter implementation
│   │   └── compaction/      # Background K-Way merge worker
│   └── test/java/com/lsm/   # Unit, fault injection, and crash simulation suites
├── docs/                    # Technical specs and architecture deep-dives
├── pom.xml
└── README.md

```

---

## Benchmarks & Performance Goals

* **Sequential Writes:** $40,000+$ ops/sec (SSD, WAL enabled).
* **Point Lookups:** Sub-millisecond reads via Bloom filter short-circuiting and sparse index seeks.
* **Crash Resilience:** 100% data recovery verified via simulated sudden JVM kill/restart tests.

---

## License

This project is licensed under the MIT License - see the [LICENSE](https://www.google.com/search?q=LICENSE) file for details.