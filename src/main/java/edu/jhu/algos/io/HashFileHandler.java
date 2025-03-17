package edu.jhu.algos.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading input files for kmerHashing.
 * - Reads integer keys from a file.
 * - Prints the file contents to the console.
 * - Stores values in an array for later hashing.
 * - Uses defensive programming with try-catch blocks for robust error handling.
 */
public class HashFileHandler {

    /**
     * Reads an input file, prints its contents, and stores the numbers in a list.
     * Implements defensive programming to handle various edge cases:
     * - Nonexistent file (FileNotFoundException)
     * - Empty file (warns user)
     * - Invalid characters (letters, symbols)
     * - Blank lines (ignored)
     *
     * @param filename The name of the input file.
     * @return A list of valid integers read from the file.
     */
    public static List<Integer> readFile(String filename) {
        List<Integer> keys = new ArrayList<>(); // List to store valid integer keys

        // Attempt to open and read the file
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            System.out.println("Reading file: " + filename);
            String line;

            // Read the file line by line
            while ((line = reader.readLine()) != null) {
                line = line.trim();  // Remove leading and trailing spaces

                // Skip blank lines
                if (line.isEmpty()) continue;

                try {
                    // Convert the line to an integer and store it in the list
                    int key = Integer.parseInt(line);
                    keys.add(key);
                    System.out.println("Read key: " + key);  // Print valid keys to the console
                } catch (NumberFormatException e) {
                    // Handle invalid input (letters, symbols, corrupted data)
                    System.err.println("Warning: Skipping invalid input -> \"" + line + "\" (Not a valid integer)");
                }
            }

            // Warn if the file was read but no valid keys were found
            if (keys.isEmpty()) {
                System.err.println("Warning: No valid keys found in file.");
            }

        } catch (FileNotFoundException e) {
            // Handle case where the file does not exist
            System.err.println("Error: File not found -> " + filename);
        } catch (IOException e) {
            // Handle general I/O errors
            System.err.println("Error reading file -> " + filename);
        }

        // Print summary of how many keys were successfully loaded
        System.out.println("\nFinished reading file. Total keys loaded: " + keys.size());
        return keys;
    }
}
