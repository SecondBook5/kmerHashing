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
            // 3. Print performance metrics from the hash table
            // --------------------------------------------
            PerformanceMetrics metrics;
            try {
                metrics = table.getMetrics();  // Retrieve metrics object
            } catch (Exception e) {
                System.err.println("Error: Unable to retrieve PerformanceMetrics.");
                System.err.println("Details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                return;
            }

            // Build the metrics block with all key statistics
            String statsLine = String.format(
                    "# of collisions: %d, # of comparisons: %d, records inserted: %d, load factor: %.6f\n\n",
                    metrics.getTotalCollisions(),
                    metrics.getTotalComparisons(),
                    metrics.getTotalInsertions(),
                    metrics.getLoadFactor()
            );

            writer.write(statsLine);
            System.out.print(statsLine);

            // --------------------------------------------
            // 4. Print the contents of the hash table
            //     - For "chaining": show one chain per row labeled by index
            //     - For probing: print 5 columns per row
            // --------------------------------------------
            if (strategy.equalsIgnoreCase("chaining")) {
                // Print each index and its chain (linked list format)
                for (int i = 0; i < rawTable.length; i++) {
                    String chainLine;

                    if (rawTable[i] == null) {
                        chainLine = String.format("%2d: None\n", i);
                    } else {
                        try {
                            chainLine = String.format("%2d: %s\n", i, rawTable[i].toString());
                        } catch (Exception e) {
                            chainLine = String.format("%2d: Error printing chain\n", i);
                            System.err.println("Error printing chain at index " + i + ": " +
                                    e.getClass().getSimpleName() + " - " + e.getMessage());
                        }
                    }

                    writer.write(chainLine);
                    System.out.print(chainLine);
                }

            } else {
                // Probing output: print 5 items per line, pad to fixed width
                StringBuilder line = new StringBuilder();

                for (int i = 0; i < rawTable.length; i++) {
                    Object cell = rawTable[i];
                    String cellString = (cell == null || cell.toString().equals("-1")) ? "None" : cell.toString();
                    line.append(String.format("%-8s", cellString)); // pad width for alignment

                    // Output the line every 5 entries or at the end
                    if ((i + 1) % 5 == 0 || i == rawTable.length - 1) {
                        line.append("\n");
                        writer.write(line.toString());
                        System.out.print(line);
                        line.setLength(0); // clear the buffer
                    }
                }
            }

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
