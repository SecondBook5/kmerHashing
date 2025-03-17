package edu.jhu.algos.hashing;

import java.util.Arrays;

/**
 * Abstract base class for Hash Tables.
 * - Supports insertion, searching, and printing.
 * - Defines structure for different hashing methods.
 */
public abstract class HashTable {
    protected int tableSize;  // Total number of slots in the hash table
    protected int bucketSize; // 1 (default) or 3 (for schemes 10 & 11)
    protected int[] table;    // Hash table storage (direct array)

    protected int comparisons;  // Tracks comparisons made during insertion
    protected int collisions;   // Tracks collision count
    protected int failedInsertions; // Tracks unsuccessful insertions

    /**
     * Constructor for initializing a hash table.
     * @param tableSize Number of slots in the table (120 default).
     * @param bucketSize Size of each bucket (1 for most schemes, 3 for schemes 10 & 11).
     */
    public HashTable(int tableSize, int bucketSize) {
        this.tableSize = tableSize;
        this.bucketSize = bucketSize;
        this.table = new int[tableSize];
        Arrays.fill(this.table, -1); // Initialize all slots as empty (-1)

        // Initialize performance metrics
        this.comparisons = 0;
        this.collisions = 0;
        this.failedInsertions = 0;
    }

    /**
     * Abstract method to compute the hash index for a given key.
     * @param key The integer key to hash.
     * @return The computed index in the table.
     */
    protected abstract int hash(int key);

    /**
     * Inserts a key into the hash table.
     * @param key The integer key to insert.
     */
    public abstract void insert(int key);

    /**
     * Prints the hash table in a formatted manner.
     * - If bucket size = 1, prints 5 per row.
     * - If bucket size = 3, prints one row per bucket.
     */
    public void printTable() {
        if (bucketSize == 1) {
            // Print 5 items per row for bucket size 1
            for (int i = 0; i < tableSize; i++) {
                System.out.printf("%-7s ", (table[i] == -1) ? "———" : table[i]);
                if ((i + 1) % 5 == 0) System.out.println();
            }
        } else {
            // Print one row per bucket for bucket size 3
            for (int i = 0; i < tableSize; i += bucketSize) {
                System.out.printf("%2d ", (i / bucketSize) + 1); // Print bucket index
                for (int j = 0; j < bucketSize; j++) {
                    int slotIndex = i + j;
                    if (slotIndex < tableSize) {
                        System.out.printf("%-7s ", (table[slotIndex] == -1) ? "———" : table[slotIndex]);
                    }
                }
                System.out.println();
            }
        }
    }

    /**
     * Tracks statistics for performance analysis.
     */
    public void incrementComparisons() { comparisons++; }
    public void incrementCollisions() { collisions++; }
    public void incrementFailedInsertions() { failedInsertions++; }

    /**
     * Prints insertion statistics.
     */
    public void printStatistics() {
        System.out.println("\nStatistics:");
        System.out.println("Comparisons: " + comparisons);
        System.out.println("Collisions: " + collisions);
        System.out.println("Failed Insertions: " + failedInsertions);
    }
}
