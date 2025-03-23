package edu.jhu.algos.hashing;

import edu.jhu.algos.utils.PerformanceMetrics;
import edu.jhu.algos.data_structures.LinkedListChain;

/**
 * ProbingStrategy provides static utility methods for resolving hash table collisions.
 * This class supports:
 * - Linear Probing
 * - Quadratic Probing (with configurable c1 and c2)
 * - In-table Chaining using LinkedListChain
 * All methods accept PerformanceMetrics to track probing behavior and comparisons.
 */
public class ProbingStrategy {

    /**
     * Attempts to insert a key into a hash table using either linear or quadratic probing.
     * This method handles collisions by probing subsequent slots until an empty one is found.
     *
     * @param table        The hash table (array of Integer) where keys are stored.
     * @param key          The key to insert into the table.
     * @param startIndex   The index produced by the hash function (i.e., home position).
     * @param tableSize    The total number of slots in the hash table.
     * @param useQuadratic If true, use quadratic probing; otherwise, use linear probing.
     * @param metrics      A PerformanceMetrics object to track collisions, probes, and comparisons.
     * @param c1           Constant c1 for quadratic probing (typically 0.5 as per assignment).
     * @param c2           Constant c2 for quadratic probing (typically 0.5 as per assignment).
     * @param debug        If true, print debug information.
     */
    public static void insertWithProbing(
            Integer[] table,
            int key,
            int startIndex,
            int tableSize,
            boolean useQuadratic,
            PerformanceMetrics metrics,
            double c1,
            double c2,
            boolean debug
    ) {
        int i = 0;  // Probe attempt counter

        // Continue probing until a slot is found or table is full
        while (i < tableSize) {
            int probeIndex;

            // Compute next probe index
            if (useQuadratic) {
                // Apply quadratic probing formula: (home + c1*i + c2*i^2) % tableSize
                probeIndex = (int) ((startIndex + c1 * i + c2 * i * i) % tableSize);
            } else {
                // Apply linear probing formula: (home + i) % tableSize
                probeIndex = (startIndex + i) % tableSize;
            }

            // Defensive check in case modulo wraps around
            if (probeIndex < 0) probeIndex += tableSize;

            // Print debug information for probe index
            if (debug) {
                System.out.printf("[DEBUG] Probing attempt %d for key %d → index %d%n", i, key, probeIndex);
            }

            // Always count a comparison attempt
            metrics.addComparison();

            // Check if the slot is empty (null)
            if (table[probeIndex] == null) {
                table[probeIndex] = key;     // Insert the key
                metrics.addInsertion();      // Record successful insertion

                if (debug) {
                    System.out.printf("[DEBUG] Inserted key %d at index %d after %d probe(s)%n", key, probeIndex, i);
                }
                return; // Exit after successful insert
            } else {
                // Collision occurred, determine type
                if (i == 0) {
                    // Primary collision: first probe (home slot) is occupied
                    metrics.addPrimaryCollision();
                } else {
                    // Secondary collision: additional probe is also occupied
                    metrics.addSecondaryCollision();
                }

                metrics.addProbe();  // Count this probing step
                i++;                 // Move to the next probe
            }
        }

        // If loop completes, no available slot was found
        System.err.println("Error: Hash table is full. Could not insert key: " + key);
    }

    /**
     * Attempts to insert a key using in-table chaining with a LinkedListChain structure.
     * This assumes each slot in the table is a LinkedListChain initialized beforehand.
     *
     * @param chainTable   The array of LinkedListChain representing the table.
     * @param key          The key to insert.
     * @param index        The index where the key should be inserted.
     * @param metrics      A PerformanceMetrics object to track insertions.
     * @param debug        If true, print debug information.
     */
    public static void insertWithChaining(
            LinkedListChain[] chainTable,
            int key,
            int index,
            PerformanceMetrics metrics,
            boolean debug
    ) {
        // Defensive check: chain at this index must be initialized
        if (chainTable[index] == null) {
            System.err.println("Error: Chaining slot at index " + index + " is not initialized.");
            return;
        }

        // Insert into the linked list chain at the specified index
        chainTable[index].insert(key);
        metrics.addInsertion(); // Record successful chaining insert

        // Debug output for chaining
        if (debug) {
            System.out.printf("[DEBUG] Inserted key %d into chain at index %d%n", key, index);
        }
    }
}
