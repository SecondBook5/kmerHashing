package edu.jhu.algos;

import edu.jhu.algos.hashing.DivisionHashTable;
import edu.jhu.algos.hashing.CustomHashTable;
import edu.jhu.algos.hashing.HashTable;
import edu.jhu.algos.hashing.HashingScheme;
import edu.jhu.algos.io.OutputFormatter;

import java.util.List;

/**
 * HashingDriver handles execution of hash table schemes 1–14.
 * This driver is responsible for dispatching the appropriate hash table implementation
 * based on the scheme number and outputting the results to a file.
 */
public class HashingDriver {

    /**
     * Executes a specific hashing scheme.
     *
     * @param schemeNumber   The scheme number (1–14) defined in HashingScheme.
     * @param keys           List of integer keys to insert into the hash table.
     * @param outputFilePath Path to the output file where results will be written.
     * @param debug          If true, enables detailed console debugging output.
     */
    public static void runScheme(int schemeNumber, List<Integer> keys, String outputFilePath, boolean debug) {

        // Step 1: Input validation
        if (keys == null || keys.isEmpty()) {
            System.err.println("Error: Input key list is empty or null.");
            return;
        }

        if (schemeNumber < 1 || schemeNumber > 14) {
            System.err.println("Error: Invalid scheme number → " + schemeNumber);
            return;
        }

        final int tableSize = 120;  // Fixed table size across all schemes
        HashingScheme scheme = HashingScheme.fromNumber(schemeNumber);  // Lookup scheme config

        // Step 2: Print debug information about scheme
        if (debug) {
            if (scheme == null) {
                System.err.printf("[DEBUG] No matching scheme found for number %d%n", schemeNumber);
            } else {
                System.out.printf(
                        "[DEBUG] Running scheme %d → method: %s, strategy: %s, mod: %d%n",
                        schemeNumber,
                        scheme.hashingMethod,
                        scheme.strategy,
                        scheme.modValue
                );
            }
        }

        // Step 3: Run division hashing strategy (schemes 1–11)
        if (scheme != null && scheme.hashingMethod.equals("division")) {
            HashTable table = new DivisionHashTable(
                    tableSize,              // Total number of slots in the table
                    scheme.bucketSize,      // Either 1 or 3 depending on the scheme
                    scheme.modValue,        // Modulo divisor for division hashing
                    scheme.strategy,        // Collision strategy: linear, quadratic, chaining
                    debug                   // Enable verbose debug output
            );

            // Insert all keys
            for (int key : keys) {
                if (debug) {
                    System.out.printf("[DEBUG] Inserting key: %d%n", key);
                }
                table.insert(key);
            }

            // Write formatted results to output file
            OutputFormatter.writeOutput(
                    scheme.schemeNumber,
                    scheme.hashingMethod,
                    scheme.modValue,
                    scheme.bucketSize,
                    tableSize,
                    scheme.strategy,
                    table,
                    keys,
                    outputFilePath
            );

            // Print debug stats if enabled
            if (debug) {
                table.printStatistics();
            }

            return;
        }

        // Step 4: Run custom hashing strategy (schemes 12–14, Fibonacci hashing)
        if (scheme != null && scheme.hashingMethod.equals("custom")) {
            HashTable table = new CustomHashTable(
                    tableSize,              // Fixed hash table size
                    scheme.bucketSize,      // Bucket size is 1 in all custom schemes
                    scheme.strategy,        // Strategy: linear, quadratic, or chaining
                    debug                   // Enable debug logs
            );

            // Insert each key using Fibonacci hashing
            for (int key : keys) {
                if (debug) {
                    System.out.printf("[DEBUG] Inserting key: %d%n", key);
                }
                table.insert(key);
            }

            // Write results to file — note mod value is not used in Fibonacci hashing
            OutputFormatter.writeOutput(
                    scheme.schemeNumber,
                    scheme.hashingMethod,
                    -1,                     // No mod value for Fibonacci hashing
                    scheme.bucketSize,
                    tableSize,
                    scheme.strategy,
                    table,
                    keys,
                    outputFilePath
            );

            // Display performance metrics
            if (debug) {
                table.printStatistics();
            }

            return;
        }

        // Step 5: Fall-through error case (should never occur unless HashingScheme is corrupted)
        System.err.printf("Unrecognized scheme or hashing method for scheme number %d.%n", schemeNumber);
    }
}
