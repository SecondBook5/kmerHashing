package edu.jhu.algos.io;

import edu.jhu.algos.hashing.HashTable;
import edu.jhu.algos.utils.PerformanceMetrics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * OutputFormatter handles formatting and writing output to file.
 * It echoes the input, prints hash strategy parameters, performance metrics,
 * and the final hash table in a grid format.
 *
 * If debug mode is enabled, the content is also mirrored to the console.
 */
public class OutputFormatter {

    /**
     * Writes the full output for a hashing scheme to a specified file.
     * Includes defensive programming for null input and file handling errors.
     * Mirrors the output to the console if debug is enabled.
     *
     * @param schemeNumber     The ID number of the scheme (1–14).
     * @param hashingMethod    Hashing method ("division" or "custom").
     * @param modValue         Modulo value used (ignored if "custom").
     * @param bucketSize       Number of slots per bucket (1 or 3).
     * @param tableSize        Size of the hash table (typically 120).
     * @param strategy         Collision strategy used ("linear", "quadratic", "chaining").
     * @param table            The populated hash table.
     * @param keys             Original input list of keys to insert.
     * @param outputFile       Path to the output file to write.
     */
    public static void writeOutput(
            int schemeNumber,
            String hashingMethod,
            int modValue,
            int bucketSize,
            int tableSize,
            String strategy,
            HashTable table,
            List<Integer> keys,
            String outputFile
    ) {
        // Defensive check: ensure input parameters are not null
        if (table == null || keys == null || outputFile == null) {
            System.err.println("Error: Null input passed to OutputFormatter.");
            System.err.printf("  - table: %s\n  - keys: %s\n  - outputFile: %s\n",
                    (table == null ? "null" : "ok"),
                    (keys == null ? "null" : "ok"),
                    (outputFile == null ? "null" : "ok"));
            return;
        }

        // Attempt to retrieve the internal table array from the hash table
        Object[] rawTable;
        try {
            rawTable = table.getRawTable(); // Used to print the final table structure
        } catch (Exception e) {
            System.err.println("Error: Unable to retrieve raw table from HashTable.");
            System.err.println("Details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return;
        }

        // Attempt to open the output file for writing
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            // --------------------------------------------
            // 1. Echo the original input keys to output file
            // --------------------------------------------
            StringBuilder inputBlock = new StringBuilder("original input:\n");

            // Loop through all keys and append them with formatting
            for (int i = 0; i < keys.size(); i++) {
                inputBlock.append(keys.get(i));
                if (i < keys.size() - 1) inputBlock.append(", ");
                if ((i + 1) % 5 == 0) inputBlock.append("\n"); // wrap every 5 items
            }
            if (keys.size() % 5 != 0) inputBlock.append("\n");

            writer.write(inputBlock.toString()); // write to file
            System.out.print(inputBlock);        // mirror to console

            // --------------------------------------------
            // 2. Echo the scheme's configuration settings
            // --------------------------------------------
            String configLine = String.format(
                    "\nscheme %d (%s) - modulo: %s, bucket size: %d, %s\n",
                    schemeNumber,
                    hashingMethod,
                    hashingMethod.equals("custom") ? "N/A" : modValue,  // Omit mod if custom
                    bucketSize,
                    strategy
            );

            writer.write(configLine);
            System.out.print(configLine);

            // --------------------------------------------
            // 3. Print collision/comparison stats before the table
            // --------------------------------------------
            PerformanceMetrics metrics;
            try {
                metrics = table.getMetrics();  // Retrieve metrics object
            } catch (Exception e) {
                System.err.println("Error: Unable to retrieve PerformanceMetrics.");
                System.err.println("Details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                return;
            }

            String statsBlock = String.format(
                    "# of primary collisions: %d, secondary collisions: %d, total collisions: %d\n" +
                            "# of comparisons: %d, records inserted: %d, load factor: %.6f\n\n",
                    metrics.getPrimaryCollisions(),
                    metrics.getSecondaryCollisions(),
                    metrics.getTotalCollisions(),
                    metrics.getTotalComparisons(),
                    metrics.getTotalInsertions(),
                    metrics.getLoadFactor()
            );

            writer.write(statsBlock);
            System.out.print(statsBlock);

            // --------------------------------------------
            // 4. Print the contents of the hash table
            //     - Chaining: print in table format with arrows for debug-like style
            //     - Probing: classic 5-wide (bucket 1) or 3-wide rows (bucket 3)
            // --------------------------------------------
            if (strategy.equalsIgnoreCase("chaining")) {
                if (bucketSize == 1) {
                    // Print chaining table: 5 items per row
                    for (int i = 0; i < rawTable.length; i++) {
                        String cell = (rawTable[i] == null) ? "None" : rawTable[i].toString();
                        writer.write(String.format("%-20s", cell));
                        System.out.printf("%-20s", cell);

                        if ((i + 1) % 5 == 0 || i == rawTable.length - 1) {
                            writer.write("\n");
                            System.out.println();
                        }
                    }
                } else if (bucketSize == 3) {
                    // Print chaining table: one row per bucket with 3 columns
                    for (int i = 0; i < rawTable.length; i += 3) {
                        StringBuilder row = new StringBuilder();
                        for (int j = 0; j < 3; j++) {
                            int index = i + j;
                            if (index < rawTable.length) {
                                Object cell = rawTable[index];
                                String cellStr = (cell == null) ? "None" : cell.toString();
                                row.append(String.format("%-20s", cellStr));
                            }
                        }
                        writer.write(row.toString() + "\n");
                        System.out.println(row);
                    }
                }
            } else {
                // Probing strategies
                if (bucketSize == 1) {
                    for (int i = 0; i < rawTable.length; i++) {
                        String cell = (rawTable[i] == null) ? "None" : rawTable[i].toString();
                        writer.write(String.format("%-8s", cell));
                        System.out.printf("%-8s", cell);

                        if ((i + 1) % 5 == 0 || i == rawTable.length - 1) {
                            writer.write("\n");
                            System.out.println();
                        }
                    }
                } else if (bucketSize == 3) {
                    for (int i = 0; i < rawTable.length; i += 3) {
                        StringBuilder row = new StringBuilder();
                        for (int j = 0; j < 3; j++) {
                            int index = i + j;
                            if (index < rawTable.length) {
                                Object cell = rawTable[index];
                                String cellStr = (cell == null) ? "None" : cell.toString();
                                row.append(String.format("%-8s", cellStr));
                            }
                        }
                        writer.write(row.toString() + "\n");
                        System.out.println(row);
                    }
                }
            }

            // --------------------------------------------
            // 5. Append final runtime/memory metrics
            // --------------------------------------------
            String trailingStats = String.format(
                    "\nExecution Time: %d ms\nMemory Usage: %d MB\n",
                    metrics.getElapsedTimeMs(),
                    metrics.getMemoryUsageMB()
            );
            writer.write(trailingStats);
            System.out.print(trailingStats);

        } catch (IOException ioEx) {
            // Handle file I/O errors with detailed context
            System.err.println("Error: Unable to write output file → " + outputFile);
            System.err.println("Details: " + ioEx.getClass().getSimpleName() + " - " + ioEx.getMessage());

        } catch (Exception generalEx) {
            // Catch any other unexpected error
            System.err.println("Unexpected error occurred during output formatting.");
            System.err.println("Details: " + generalEx.getClass().getSimpleName() + " - " + generalEx.getMessage());
        }
    }
}
