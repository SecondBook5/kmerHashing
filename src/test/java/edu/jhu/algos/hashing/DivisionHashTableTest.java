package edu.jhu.algos.hashing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DivisionHashTable.
 * - Tests insertion, collisions, and error handling.
 */
class DivisionHashTableTest {

    private DivisionHashTable hashTable;

    /**
     * Setup: Initializes a new hash table before each test.
     */
    @BeforeEach
    void setUp() {
        hashTable = new DivisionHashTable(10, 1, 10); // Small table for testing
    }

    /**
     * Test inserting valid keys.
     */
    @Test
    void testValidInsertions() {
        hashTable.insert(5);
        hashTable.insert(15); // Should trigger Linear Probing (collision at index 5)

        assertEquals(5, hashTable.table[5]);
        assertEquals(15, hashTable.table[6]); // Should be placed after probing
    }

    /**
     * Test inserting duplicate keys (causing collisions).
     */
    @Test
    void testCollisionHandling() {
        hashTable.insert(3);
        hashTable.insert(13); // Collision at index 3, should move to 4
        hashTable.insert(23); // Another collision at index 3, should move to 5

        assertEquals(3, hashTable.table[3]);
        assertEquals(13, hashTable.table[4]);
        assertEquals(23, hashTable.table[5]);
    }

    /**
     * Test inserting negative keys (should be rejected).
     */
    @Test
    void testNegativeKeyRejection() {
        hashTable.insert(-5);
        for (int i = 0; i < hashTable.tableSize; i++) {
            assertNotEquals(-5, hashTable.table[i]); // Negative key should not be in the table
        }
    }

    /**
     * Test inserting more keys than the table can hold.
     */
    @Test
    void testTableFullScenario() {
        for (int i = 0; i < 10; i++) {
            hashTable.insert(i * 10); // Fill the table
        }

        // Next insertion should fail
        hashTable.insert(1000);

        boolean tableFull = true;
        for (int i = 0; i < hashTable.tableSize; i++) {
            if (hashTable.table[i] == -1) {
                tableFull = false;
                break;
            }
        }
        assertTrue(tableFull); // Table should be completely full
    }

    /**
     * Test inserting into a full table does not cause ArrayIndexOutOfBoundsException.
     */
    @Test
    void testNoOutOfBoundsErrors() {
        for (int i = 0; i < 10; i++) {
            hashTable.insert(i * 10); // Fill the table
        }

        assertDoesNotThrow(() -> hashTable.insert(200)); // Should gracefully handle full table
    }
}
