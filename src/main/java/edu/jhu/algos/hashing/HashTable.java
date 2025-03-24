package edu.jhu.algos.hashing;

import edu.jhu.algos.utils.PerformanceMetrics;
import java.util.Arrays;

/**
 * Abstract base class for Hash Tables.
 * - Supports insertion, searching, and printing.
 * - Defines structure for different hashing methods.
 * - Uses PerformanceMetrics to track efficiency.
 */
public abstract class HashTable {
    protected int tableSize;  // Total number of slots in the hash table
    protected int bucketSize; // Defines if buckets hold 1 or 3 elements
    protected Integer[] table; // Hash table storage (now using Integer[] to allow null values)
    protected PerformanceMetrics metrics; // Object for tracking performance statistics
    protected final boolean debugMode; // Enables debug output across subclasses

    /**
     * Constructor for initializing a hash table.
     *
     * @param tableSize  Number of slots in the table (default: 120).
     * @param bucketSize Size of each bucket (1 for most schemes, 3 for schemes 10 & 11).
     * @param debugMode  Enables debug output for subclasses and diagnostics.
     * @throws IllegalArgumentException If bucketSize is not 1 or 3.
     */
    public HashTable(int tableSize, int bucketSize, boolean debugMode) {
        if (bucketSize != 1 && bucketSize != 3) {
            throw new IllegalArgumentException("Error: bucketSize must be 1 or 3.");
        }

        this.tableSize = tableSize;
        this.bucketSize = bucketSize;
        this.debugMode = debugMode;
        this.table = new Integer[tableSize]; // Changed to Integer[] to allow null values
        this.metrics = new PerformanceMetrics(); // Initialize performance tracking

        // Initialize all slots as EMPTY (null instead of Integer.MIN_VALUE)
        Arrays.fill(this.table, null);
    }

    /**
     * Abstract method to compute the hash index for a given key.
     * Each subclass must define its own hashing strategy.
     *
     * @param key The integer key to hash.
     * @return The computed index in the table.
     */
    protected abstract int hash(int key);

    /**
     * Inserts a key into the hash table.
     * - This method is implemented in subclasses (Linear, Quadratic Probing, Chaining).
     *
     * @param key The integer key to insert.
     */
    public abstract void insert(int key);

    /**
     * Searches for a key in the hash table.
     *
     * @param key The integer key to find.
     * @return True if the key is found, otherwise false.
     */
    public abstract boolean search(int key);

    /**
     * Clears the hash table by resetting all values to empty.
     * - Resets the hash table storage.
     * - Clears performance metrics for a fresh test.
     */
    public void clearTable() {
        Arrays.fill(this.table, null); // Mark all slots as empty (null)
        metrics.resetAll(); // Reset tracking counters
    }

    /**
     * Prints the hash table in a formatted manner.
     * - If bucket size = 1, prints 5 items per row.
     * - If bucket size = 3, prints one row per bucket.
     * - Uses "———" to represent empty slots.
     */
    public void printTable() {
        if (bucketSize == 1) {
            // Print 5 items per row for bucket size 1
            for (int i = 0; i < tableSize; i++) {
                System.out.printf("%-10s ", (table[i] == null) ? "———" : table[i]);
                if ((i + 1) % 5 == 0) System.out.println();
            }
        } else {
            // Print one row per bucket for bucket size 3
            for (int i = 0; i < tableSize; i += bucketSize) {
                System.out.printf("%2d ", (i / bucketSize) + 1); // Print bucket index
                for (int j = 0; j < bucketSize; j++) {
                    int slotIndex = i + j;
                    if (slotIndex < tableSize) {
                        System.out.printf("%-10s ", (table[slotIndex] == null) ? "———" : table[slotIndex]);
                    }
                }
                System.out.println();
            }
        }
    }

    /**
     * Prints statistics related to hash table performance.
     */
    public void printStatistics() {
        System.out.println("\nStatistics:");
        System.out.println("Comparisons: " + metrics.getTotalComparisons());
        System.out.println("Collisions: " + metrics.getTotalCollisions());
        System.out.println("Probes: " + metrics.getTotalProbes());
        System.out.println("Insertions: " + metrics.getTotalInsertions());
        System.out.println("Execution Time: " + metrics.getElapsedTimeMs() + " ms");
        System.out.println("Memory Usage: " + metrics.getMemoryUsageMB() + " MB");
    }

    /**
     * Returns the performance metrics associated with this hash table.
     *
     * @return PerformanceMetrics object tracking efficiency and statistics.
     */
    public PerformanceMetrics getMetrics() {
        return this.metrics;
    }

    /**
     * Provides access to the raw table array for output formatting.
     *
     * @return The internal table array as an Object array.
     */
    public Object[] getRawTable() {
        return this.table;
    }
}
