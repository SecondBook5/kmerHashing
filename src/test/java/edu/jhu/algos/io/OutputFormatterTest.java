package edu.jhu.algos.io;

import edu.jhu.algos.data_structures.ChainedNode;
import edu.jhu.algos.data_structures.LinkedListChain;
import edu.jhu.algos.data_structures.Stack;
import edu.jhu.algos.hashing.DivisionHashTable;
import edu.jhu.algos.hashing.HashTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OutputFormatter.java.
 * Verifies correct formatting for probing and chaining strategies,
 * defensive error handling, and output file creation.
 */
public class OutputFormatterTest {

    private static final String OUTPUT_FILE_PROBING = "test_output_probing.txt";
    private static final String OUTPUT_FILE_CHAINING = "test_output_chaining.txt";
    private static final String OUTPUT_FILE_NULL_INPUT = "test_output_null.txt";

    /**
     * Reads the full contents of a file into a single string for assertion checks.
     *
     * @param filename Path to the output file.
     * @return File content as string.
     * @throws IOException If reading the file fails.
     */
    private String readOutputFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * Tests that linear probing output is written and formatted correctly.
     */
    @Test
    public void testProbingOutput() throws IOException {
        List<Integer> keys = Arrays.asList(5, 25, 45);
        HashTable table = new DivisionHashTable(10, 1, 10, "linear", false);

        for (int key : keys) {
            table.insert(key);
        }

        OutputFormatter.writeOutput(
                1,
                "division",
                10,
                1,
                10,
                "linear",
                table,
                keys,
                OUTPUT_FILE_PROBING,
                true // Enable debug mode
        );

        File file = new File(OUTPUT_FILE_PROBING);
        assertTrue(file.exists(), "Expected output file was not created: " + OUTPUT_FILE_PROBING);

        String output = readOutputFile(OUTPUT_FILE_PROBING);
        System.out.println("Debug Output (Probing):\n" + output);

        assertTrue(output.contains("scheme 1 (division)"));
        assertTrue(output.contains("linear"));
        assertTrue(output.contains("load factor"));
        assertTrue(output.contains("original input"));
        assertTrue(output.contains("5")); // Confirm one inserted key is present
    }

    /**
     * Tests that chaining output includes linked format with "->" and terminator "None".
     */
    @Test
    public void testChainingOutput() throws IOException {
        List<Integer> keys = Arrays.asList(3, 13, 23); // All hash to index 3 with mod 10
        HashTable table = new DivisionHashTable(10, 1, 10, "chaining", false);

        for (int key : keys) {
            table.insert(key);
        }

        OutputFormatter.writeOutput(
                3,
                "division",
                10,
                1,
                10,
                "chaining",
                table,
                keys,
                OUTPUT_FILE_CHAINING,
                true // Enable debug mode
        );

        File file = new File(OUTPUT_FILE_CHAINING);
        assertTrue(file.exists(), "Expected output file was not created: " + OUTPUT_FILE_CHAINING);

        String output = readOutputFile(OUTPUT_FILE_CHAINING);
        System.out.println("Debug Output (Chaining):\n" + output);

        assertTrue(output.contains("scheme 3 (division)"));
        assertTrue(output.contains("chaining"));
        assertTrue(output.contains("->"));    // Chained format
        assertTrue(output.contains("None"));  // Chain terminator
    }

    /**
     * Tests that passing null input to OutputFormatter does not crash.
     */
    @Test
    public void testNullInputHandling() {
        assertDoesNotThrow(() ->
                OutputFormatter.writeOutput(
                        99,
                        "division",
                        10,
                        1,
                        10,
                        "linear",
                        null,     // null hash table
                        null,     // null keys
                        OUTPUT_FILE_NULL_INPUT,
                        true      // debug on for visibility
                )
        );
    }

    /**
     * Helper method to delete a file after test completion.
     *
     * @param filename Path to the file.
     */
    private void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("Warning: Failed to delete file → " + filename);
            }
        }
    }

    /**
     * Cleans up test output files after each test.
     */
    @AfterEach
    public void cleanUp() {
        deleteFile(OUTPUT_FILE_PROBING);
        deleteFile(OUTPUT_FILE_CHAINING);
        deleteFile(OUTPUT_FILE_NULL_INPUT);
    }
}
