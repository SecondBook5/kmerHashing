package edu.jhu.algos.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for HashFileHandler.java
 * - Tests handling of valid input, invalid input, empty files, missing files, blank lines,
 *   large files, leading/trailing spaces, and special characters.
 */
public class HashFileHandlerTest {

    /**
     * Creates a temporary test file with specified content.
     * Ensures test files are managed correctly and cleaned up automatically.
     *
     * @param tempDir  The temporary directory to create the file in.
     * @param filename The name of the test file.
     * @param content  The content to write to the file.
     * @return The absolute path of the created file, or null if an error occurs.
     */
    private Path createTestFile(Path tempDir, String filename, String content) {
        try {
            Path testFile = tempDir.resolve(filename); // Create file in temporary directory
            Files.writeString(testFile, content); // Write content
            return testFile;
        } catch (IOException e) {
            fail("Error creating test file: " + filename);
            return null; // Return null to signal file creation failure
        }
    }

    /**
     * Test reading a valid file with integers.
     */
    @Test
    public void testValidFile(@TempDir Path tempDir) {
        Path testFile = createTestFile(tempDir, "test_valid.txt", "12501\n84763\n22599\n55555\n99999\n");
        assertNotNull(testFile, "Test file creation failed.");

        List<Integer> keys = HashFileHandler.readFile(testFile.toString());

        assertEquals(5, keys.size(), "Should read 5 valid integers");
        assertEquals(12501, keys.get(0), "First number should match");
        assertEquals(99999, keys.get(4), "Last number should match");
    }

    /**
     * Test reading a file with a mix of valid and invalid inputs.
     */
    @Test
    public void testFileWithInvalidInputs(@TempDir Path tempDir) {
        Path testFile = createTestFile(tempDir, "test_invalid.txt", "12501\nabc\n84763\n55x5\n99999\n");
        assertNotNull(testFile, "Test file creation failed.");

        List<Integer> keys = HashFileHandler.readFile(testFile.toString());

        assertEquals(3, keys.size(), "Should only read valid integers (3 valid, 2 invalid)");
        assertEquals(12501, keys.get(0), "First valid number should match");
        assertEquals(99999, keys.get(2), "Last valid number should match");
    }

    /**
     * Test reading an empty file.
     */
    @Test
    public void testEmptyFile(@TempDir Path tempDir) {
        Path testFile = createTestFile(tempDir, "test_empty.txt", "");
        assertNotNull(testFile, "Test file creation failed.");

        List<Integer> keys = HashFileHandler.readFile(testFile.toString());

        assertTrue(keys.isEmpty(), "Should return an empty list for an empty file");
    }

    /**
     * Test reading a file with blank lines.
     */
    @Test
    public void testFileWithBlankLines(@TempDir Path tempDir) {
        Path testFile = createTestFile(tempDir, "test_blank_lines.txt", "\n12501\n\n84763\n\n99999\n");
        assertNotNull(testFile, "Test file creation failed.");

        List<Integer> keys = HashFileHandler.readFile(testFile.toString());

        assertEquals(3, keys.size(), "Should ignore blank lines and read 3 valid integers");
        assertEquals(12501, keys.get(0), "First valid number should match");
        assertEquals(99999, keys.get(2), "Last valid number should match");
    }

    /**
     * Test reading a file with leading and trailing spaces.
     */
    @Test
    public void testFileWithLeadingTrailingSpaces(@TempDir Path tempDir) {
        Path testFile = createTestFile(tempDir, "test_spaces.txt", "   12501   \n  84763  \n22599\n  55555\n99999  ");
        assertNotNull(testFile, "Test file creation failed.");

        List<Integer> keys = HashFileHandler.readFile(testFile.toString());

        assertEquals(5, keys.size(), "Should correctly parse numbers with spaces");
        assertEquals(12501, keys.get(0), "First valid number should match");
        assertEquals(99999, keys.get(4), "Last valid number should match");
    }

    /**
     * Test reading a file with special characters.
     */
    @Test
    public void testFileWithSpecialCharacters(@TempDir Path tempDir) {
        Path testFile = createTestFile(tempDir, "test_special_chars.txt", "12501\n# Comment line\n84763\n%$$\n99999\n");
        assertNotNull(testFile, "Test file creation failed.");

        List<Integer> keys = HashFileHandler.readFile(testFile.toString());

        assertEquals(3, keys.size(), "Should only read valid numbers and ignore special characters");
        assertEquals(12501, keys.get(0), "First valid number should match");
        assertEquals(99999, keys.get(2), "Last valid number should match");
    }

    /**
     * Test reading a very large file to check performance.
     */
    @Test
    public void testLargeFile(@TempDir Path tempDir) {
        StringBuilder largeFileContent = new StringBuilder();
        for (int i = 1; i <= 10000; i++) {
            largeFileContent.append(i).append("\n");
        }

        Path testFile = createTestFile(tempDir, "test_large.txt", largeFileContent.toString());
        assertNotNull(testFile, "Test file creation failed.");

        List<Integer> keys = HashFileHandler.readFile(testFile.toString());

        assertEquals(10000, keys.size(), "Should correctly read a large number of integers");
        assertEquals(1, keys.get(0), "First number should be 1");
        assertEquals(10000, keys.get(9999), "Last number should be 10000");
    }
    @Test
    public void testMinMaxIntegerValues(@TempDir Path tempDir) {
        Path testFile = createTestFile(tempDir, "test_int_bounds.txt", "2147483647\n-2147483648\nabc\n");
        assertNotNull(testFile, "Test file creation failed.");

        List<Integer> keys = HashFileHandler.readFile(testFile.toString());

        assertEquals(2, keys.size(), "Should read 2 valid integer bounds and skip the invalid line");
        assertEquals(Integer.MAX_VALUE, keys.get(0), "Should read Integer.MAX_VALUE");
        assertEquals(Integer.MIN_VALUE, keys.get(1), "Should read Integer.MIN_VALUE");
    }

}
