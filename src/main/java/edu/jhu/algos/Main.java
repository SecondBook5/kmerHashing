package edu.jhu.algos;

import edu.jhu.algos.io.HashFileHandler;
import java.util.List;

/**
 * Temporary test for HashFileHandler.
 * - Reads and prints integer keys from a file.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java edu.jhu.algos.Main <input_filename>");
            System.exit(1);
        }

        String filename = args[0];

        // Step 1: Read input file
        List<Integer> keys = HashFileHandler.readFile(filename);

        // Step 2: Print extracted keys
        System.out.println("\nExtracted Keys:");
        System.out.println(keys);
    }
}
