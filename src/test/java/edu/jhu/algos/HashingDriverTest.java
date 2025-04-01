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
 * Verifies correct execution and output file generation for schemes 1–14.
 * Tests manual configuration and error handling for invalid input.
 */
public class HashingDriverTest {

    private static final String OUTPUT_PREFIX = "test_scheme_output_";

    /**
     * Loads a dummy input file containing basic integers.
     */
    private List<Integer> loadSampleKeys() {
        return Arrays.asList(5, 18, 23, 42, 57, 67, 99);
    }

    /**
     * Reads a file and returns its contents as a string.
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
     * Test all valid predefined schemes (1–11) using division hashing.
     */
    @Test
    public void testValidSchemes_1to11() throws IOException {
        List<Integer> keys = loadSampleKeys();

        for (int i = 1; i <= 11; i++) {
            String filePath = OUTPUT_PREFIX + i + ".txt";
            HashingDriver.runScheme(i, keys, filePath, false, false);

            File file = new File(filePath);
            assertTrue(file.exists(), "Expected file not created for scheme " + i);

            String contents = readOutputFile(filePath);
            assertTrue(contents.contains("scheme " + i + " (division)"));
            assertTrue(contents.contains("original input:"));
            assertTrue(contents.contains("load factor"));
        }
    }

    /**
     * Test all custom schemes (12–14) using Fibonacci hashing.
     */
    @Test
    public void testCustomSchemes_12to14() throws IOException {
        List<Integer> keys = loadSampleKeys();

        for (int i = 12; i <= 14; i++) {
            String filePath = OUTPUT_PREFIX + i + ".txt";
            HashingDriver.runScheme(i, keys, filePath, false, false);

            File file = new File(filePath);
            assertTrue(file.exists(), "Expected file not created for scheme " + i);

            String contents = readOutputFile(filePath);
            assertTrue(contents.contains("scheme " + i + " (custom)"));
            assertTrue(contents.contains("original input:"));
            assertTrue(contents.contains("load factor"));
        }
    }

    /**
     * Test edge case: empty input list should not throw.
     */
    @Test
    public void testEmptyInputList() {
        List<Integer> emptyKeys = new ArrayList<>();
        assertDoesNotThrow(() -> HashingDriver.runScheme(1, emptyKeys, OUTPUT_PREFIX + "empty.txt", false, false));
    }

    /**
     * Test edge case: null key list should not throw.
     */
    @Test
    public void testNullKeyList() {
        assertDoesNotThrow(() -> HashingDriver.runScheme(1, null, OUTPUT_PREFIX + "null.txt", false, false));
    }

    /**
     * Test invalid scheme numbers.
     */
    @Test
    public void testInvalidSchemeNumberTooLow() {
        List<Integer> keys = loadSampleKeys();
        assertDoesNotThrow(() -> HashingDriver.runScheme(0, keys, OUTPUT_PREFIX + "invalid_low.txt", false, false));
    }

    @Test
    public void testInvalidSchemeNumberTooHigh() {
        List<Integer> keys = loadSampleKeys();
        assertDoesNotThrow(() -> HashingDriver.runScheme(15, keys, OUTPUT_PREFIX + "invalid_high.txt", false, false));
    }

    /**
     * Test runManual using division hashing with linear probing.
     */
    @Test
    public void testManualDivisionLinearProbing() throws IOException {
        List<Integer> keys = loadSampleKeys();
        String filePath = OUTPUT_PREFIX + "manual_division_linear.txt";

        HashingDriver.runManual(
                "division",
                113,           // mod
                1,             // bucket size
                "linear",      // strategy
                0.5,
                0.5,
                keys,
                filePath,
                false
        );

        File file = new File(filePath);
        assertTrue(file.exists());

        String contents = readOutputFile(filePath);
        assertTrue(contents.contains("original input:"));
        assertTrue(contents.contains("load factor"));
    }

    /**
     * Test runManual using custom hashing with quadratic probing.
     */
    @Test
    public void testManualCustomQuadraticProbing() throws IOException {
        List<Integer> keys = loadSampleKeys();
        String filePath = OUTPUT_PREFIX + "manual_custom_quadratic.txt";

        HashingDriver.runManual(
                "custom",
                -1,
                1,
                "quadratic",
                0.5,
                0.5,
                keys,
                filePath,
                false
        );

        File file = new File(filePath);
        assertTrue(file.exists());

        String contents = readOutputFile(filePath);
        assertTrue(contents.contains("original input:"));
        assertTrue(contents.contains("load factor"));
    }

    /**
     * Ensure that invalid hashing method is caught gracefully.
     */
    @Test
    public void testManualInvalidHashingMethod() {
        List<Integer> keys = loadSampleKeys();
        assertDoesNotThrow(() -> HashingDriver.runManual(
                "invalid-method", 0, 1, "linear", 0.5, 0.5,
                keys, OUTPUT_PREFIX + "invalid_method.txt", false
        ));
    }

    /**
     * Ensure that chaining works when specified manually.
     */
    @Test
    public void testManualChainingCustom() throws IOException {
        List<Integer> keys = loadSampleKeys();
        String filePath = OUTPUT_PREFIX + "manual_custom_chaining.txt";

        HashingDriver.runManual(
                "custom",
                -1,
                1,
                "chaining",
                0.0,
                0.0,
                keys,
                filePath,
                false
        );

        File file = new File(filePath);
        assertTrue(file.exists());
        String contents = readOutputFile(filePath);
        assertTrue(contents.contains("original input:"));
    }

    /**
     * Cleanup generated test files after each test run.
     */
    @AfterEach
    public void cleanUp() {
        List<String> tempFiles = new ArrayList<>();

        // Scheme-based output files
        for (int i = 1; i <= 14; i++) {
            tempFiles.add(OUTPUT_PREFIX + i + ".txt");
        }

        // Additional test-specific outputs
        tempFiles.addAll(Arrays.asList(
                OUTPUT_PREFIX + "empty.txt",
                OUTPUT_PREFIX + "null.txt",
                OUTPUT_PREFIX + "invalid_low.txt",
                OUTPUT_PREFIX + "invalid_high.txt",
                OUTPUT_PREFIX + "manual_division_linear.txt",
                OUTPUT_PREFIX + "manual_custom_quadratic.txt",
                OUTPUT_PREFIX + "manual_custom_chaining.txt",
                OUTPUT_PREFIX + "invalid_method.txt"
        ));

        // Delete all files if they exist
        for (String path : tempFiles) {
            File file = new File(path);
            if (file.exists() && !file.delete()) {
                System.err.println("Warning: Failed to delete file → " + path);
            }
        }
    }
}
