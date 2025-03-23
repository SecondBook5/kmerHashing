package edu.jhu.algos;

import edu.jhu.algos.hashing.DivisionHashTable;
import edu.jhu.algos.hashing.CustomHashTable;
import edu.jhu.algos.hashing.HashTable;
import edu.jhu.algos.hashing.HashingScheme;
import edu.jhu.algos.io.OutputFormatter;

import java.util.List;

/**
 * HashingDriver handles execution of hash table schemes 1–14.
 * It delegates to the appropriate hash table strategy and manages output.
 */
public class HashingDriver {

    /**
     * Runs a hashing scheme using a selected strategy, input keys, and output file.
     *
     * @param schemeNumber   The number of the scheme to run (1–14).
     * @param keys           The list of integer keys to insert.
     * @param outputFilePath File path for writing output.
     * @param debug          Enable verbose debugging output if true.
     */
    public static void runScheme(int schemeNumber, List<Integer> keys, String outputFilePath, boolean debug) {

        // Validate input
        if (keys == null || keys.isEmpty()) {
            System.err.println("Error: Input key list is empty or null.");
            return;
        }

        if (schemeNumber < 1 || schemeNumber > 14) {
            System.err.println("Error: Invalid scheme number → " + schemeNumber);
            return;
        }

        final int tableSize = 120;
        HashingScheme scheme = HashingScheme.fromNumber(schemeNumber);

        // Handle schemes 1–11 (division hashing)
        if (scheme != null && scheme.hashingMethod.equals("division")) {
            HashTable table = new DivisionHashTable(
                    tableSize,
                    scheme.bucketSize,
                    scheme.modValue,
                    scheme.strategy,
                    debug
            );

            for (int key : keys) {
                table.insert(key);
            }

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

            if (debug) {
                table.printStatistics();
            }

            return;
        }

        // Handle schemes 12–14 (custom hashing using Fibonacci method)
        if (scheme != null && scheme.hashingMethod.equals("custom")) {
            HashTable table = new CustomHashTable(
                    tableSize,
                    scheme.bucketSize,
                    scheme.strategy,
                    debug
            );

            for (int key : keys) {
                table.insert(key);
            }

            OutputFormatter.writeOutput(
                    scheme.schemeNumber,
                    scheme.hashingMethod,
                    -1, // No mod value for Fibonacci hashing
                    scheme.bucketSize,
                    tableSize,
                    scheme.strategy,
                    table,
                    keys,
                    outputFilePath
            );

            if (debug) {
                table.printStatistics();
            }

            return;
        }

        // Catch-all (should not be reached)
        System.err.println("Unrecognized scheme or hashing method.");
    }
}
