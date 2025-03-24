package edu.jhu.algos;

import edu.jhu.algos.hashing.DivisionHashTable;
import edu.jhu.algos.hashing.CustomHashTable;
import edu.jhu.algos.hashing.HashTable;
import edu.jhu.algos.hashing.HashingScheme;
import edu.jhu.algos.io.OutputFormatter;

import java.util.List;

/**
 * HashingDriver handles execution of all hashing schemes (1–14)
 * as well as user-defined manual hashing configuration via CLI parameters.
 * Responsibilities:
 * - Look up scheme definitions from the HashingScheme enum.
 * - Construct and execute appropriate HashTable (Division or Custom).
 * - Track and print performance metrics.
 * - Write formatted results to output files.
 */
public class HashingDriver {

    /**
     * Executes a predefined hashing scheme using HashingScheme (schemes 1–14).
     * Decision Tree:
     * Step 1: Validate inputs (scheme number and key list)
     * Step 2: Fetch HashingScheme from scheme number
     * Step 3: If method == "division", use DivisionHashTable
     * Step 4: If method == "custom", use CustomHashTable (Fibonacci hashing)
     * Step 5: If no match found, report an error
     */
    public static void runScheme(int schemeNumber, List<Integer> keys, String outputFilePath, boolean debug) {

        // Step 1: Validate input
        if (keys == null || keys.isEmpty()) {
            System.err.println("Error: Input key list is empty or null.");
            return;
        }

        if (schemeNumber < 1 || schemeNumber > 14) {
            System.err.println("Error: Invalid scheme number → " + schemeNumber);
            return;
        }

        final int tableSize = 120; // Fixed table size for all schemes
        HashingScheme scheme = HashingScheme.fromNumber(schemeNumber); // Step 2: Lookup configuration

        // Optional debug output for scheme info
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

        // Step 3: Division hashing (schemes 1–11)
        if (scheme != null && scheme.hashingMethod.equals("division")) {
            HashTable table = new DivisionHashTable(
                    tableSize,
                    scheme.bucketSize,
                    scheme.modValue,
                    scheme.strategy,
                    debug
            );

            // Start performance timer
            table.getMetrics().startTimer();

            // Insert all keys
            for (int key : keys) {
                if (debug) System.out.printf("[DEBUG] Inserting key: %d%n", key);
                table.insert(key);
            }

            // Stop performance timer
            table.getMetrics().stopTimer();

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

            if (debug) table.printStatistics();
            return;
        }

        // Step 4: Custom (Fibonacci) hashing (schemes 12–14)
        if (scheme != null && scheme.hashingMethod.equals("custom")) {
            HashTable table = new CustomHashTable(
                    tableSize,
                    scheme.bucketSize,
                    scheme.strategy,
                    debug
            );

            // Start performance timer
            table.getMetrics().startTimer();

            // Insert each key using Fibonacci hashing
            for (int key : keys) {
                if (debug) System.out.printf("[DEBUG] Inserting key: %d%n", key);
                table.insert(key);
            }

            // Stop performance timer
            table.getMetrics().stopTimer();

            // Write results to file — note mod value is not used in Fibonacci hashing
            OutputFormatter.writeOutput(
                    scheme.schemeNumber,
                    scheme.hashingMethod,
                    -1, // No mod value for Fibonacci
                    scheme.bucketSize,
                    tableSize,
                    scheme.strategy,
                    table,
                    keys,
                    outputFilePath
            );

            if (debug) table.printStatistics();
            return;
        }

        // Step 5: Fallback (should never be reached unless HashingScheme is corrupted)
        System.err.printf("Unrecognized scheme or hashing method for scheme number %d.%n", schemeNumber);
    }

    /**
     * Executes a manual hashing configuration from CLI flags (outside the 1–14 schemes).
     * Decision Tree:
     * Step 1: Validate key list
     * Step 2: Determine hashing method ("division" or "custom")
     * Step 3: Construct correct HashTable subclass with user parameters
     * Step 4: Insert keys
     * Step 5: Format and print results
     */
    public static void runManual(
            String hashingMethod,
            int modValue,
            int bucketSize,
            String strategy,
            double c1,
            double c2,
            List<Integer> keys,
            String outputFilePath,
            boolean debug
    ) {
        final int tableSize = 120;

        // Step 1: Validate keys
        if (keys == null || keys.isEmpty()) {
            System.err.println("Error: Input key list is empty or null.");
            return;
        }

        // Print configuration summary
        if (debug) {
            System.out.printf(
                    "[DEBUG] Running manual mode: method=%s, strategy=%s, mod=%d, bucketSize=%d, c1=%.2f, c2=%.2f%n",
                    hashingMethod, strategy, modValue, bucketSize, c1, c2
            );
        }

        HashTable table;

        // Step 2 + 3: Determine hash type and construct table
        if ("division".equalsIgnoreCase(hashingMethod)) {
            table = new DivisionHashTable(
                    tableSize,
                    bucketSize,
                    modValue,
                    strategy.toLowerCase(),
                    debug,
                    c1,
                    c2
            );
        } else if ("custom".equalsIgnoreCase(hashingMethod)) {
            table = new CustomHashTable(
                    tableSize,
                    bucketSize,
                    strategy.toLowerCase(),
                    debug,
                    c1,
                    c2
            );
        } else {
            System.err.println("Error: Unsupported hashing method: " + hashingMethod);
            return;
        }

        // Step 4: Insert keys
        table.getMetrics().startTimer();

        for (int key : keys) {
            if (debug) System.out.printf("[DEBUG] Inserting key: %d%n", key);
            table.insert(key);
        }

        table.getMetrics().stopTimer();

        // Step 5: Format output
        OutputFormatter.writeOutput(
                -1, // Not a numbered scheme
                hashingMethod,
                ("division".equalsIgnoreCase(hashingMethod) ? modValue : -1),
                bucketSize,
                tableSize,
                strategy,
                table,
                keys,
                outputFilePath
        );

        if (debug) table.printStatistics();
    }
}
