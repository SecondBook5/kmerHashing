package edu.jhu.algos;

import edu.jhu.algos.io.HashFileHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HashingDriver class.
 * Verifies correct execution and output file generation for schemes 1–11.
 * Tests error handling for edge cases such as null input and invalid schemes.
 */
public class HashingDriverTest {

    private static final String OUTPUT_PREFIX = "test_scheme_output_";

    /**
     * Loads a dummy input file containing basic integers.
     * This mirrors what a real LabHashingInput.txt would contain.
     *
     * @return A list of integers to hash.
     */
    private List<Integer> loadSampleKeys() {
        return Arrays.asList(5, 18, 23, 42, 57, 67, 99);
    }

    /**
     * Reads a file and returns its contents as a string for assertion checks.
     */
    private String readOutputFile(String path) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Verifies that schemes 1–11 complete without errors and generate output files.
     */
    @Test
    public void testValidSchemes_1to11() throws IOException {
        List<Integer> keys = loadSampleKeys();

        for (int i = 1; i <= 11; i++) {
            String filePath = OUTPUT_PREFIX + i + ".txt";
            HashingDriver.runScheme(i, keys, filePath, false);

            File file = new File(filePath);
            assertTrue(file.exists(), "Expected file not created for scheme " + i);

            String contents = readOutputFile(filePath);
            assertTrue(contents.contains("scheme " + i + " (division)"), "Missing scheme header in output file.");
            assertTrue(contents.contains("original input:"), "Input block not printed.");
            assertTrue(contents.contains("load factor"), "Statistics block missing.");
        }
    }

    /**
     * Test that an empty key list does not produce output or crash.
     */
    @Test
    public void testEmptyInputList() {
        List<Integer> emptyKeys = new ArrayList<>();
        assertDoesNotThrow(() -> HashingDriver.runScheme(1, emptyKeys, OUTPUT_PREFIX + "empty.txt", false));
    }

    /**
     * Test null key list is handled safely without crashing.
     */
    @Test
    public void testNullKeyList() {
        assertDoesNotThrow(() -> HashingDriver.runScheme(1, null, OUTPUT_PREFIX + "null.txt", false));
    }

    /**
     * Test invalid scheme numbers (below 1 and above 14) are handled safely.
     */
    @Test
    public void testInvalidSchemeNumberTooLow() {
        List<Integer> keys = loadSampleKeys();
        assertDoesNotThrow(() -> HashingDriver.runScheme(0, keys, OUTPUT_PREFIX + "invalid_low.txt", false));
    }

    @Test
    public void testInvalidSchemeNumberTooHigh() {
        List<Integer> keys = loadSampleKeys();
        assertDoesNotThrow(() -> HashingDriver.runScheme(15, keys, OUTPUT_PREFIX + "invalid_high.txt", false));
    }

    /**
     * Dynamically deletes all test files after each run.
     */
    @AfterEach
    public void cleanUp() {
        List<String> tempFiles = new ArrayList<>();

        // Dynamically generate scheme files (1–11)
        for (int i = 1; i <= 11; i++) {
            tempFiles.add(OUTPUT_PREFIX + i + ".txt");
        }

        // Add all edge-case outputs
        tempFiles.addAll(Arrays.asList(
                OUTPUT_PREFIX + "empty.txt",
                OUTPUT_PREFIX + "null.txt",
                OUTPUT_PREFIX + "invalid_low.txt",
                OUTPUT_PREFIX + "invalid_high.txt"
        ));

        // Attempt to delete all files
        for (String path : tempFiles) {
            File file = new File(path);
            if (file.exists() && !file.delete()) {
                System.err.println("Warning: Failed to delete temp file → " + path);
            }
        }
    }
}
