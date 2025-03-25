package edu.jhu.algos.hashing;

import edu.jhu.algos.utils.PerformanceMetrics;
import edu.jhu.algos.data_structures.LinkedListChain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomHashTable using Fibonacci hashing.
 * Verifies linear and quadratic probing, chaining logic, metrics tracking,
 * collision handling, and Fibonacci constant computation.
 */
public class CustomHashTableTest {

    private static final boolean DEBUG = true;

    /**
     * Verifies clean inserts using linear probing (no collisions expected).
     */
    @Test
    public void testLinearProbingNoCollision() {
        CustomHashTable table = new CustomHashTable(32, 1, "linear", DEBUG);
        table.insert(11);     // Clean insert
        table.insert(200);    // Unlikely to collide

        assertTrue(table.search(11));
        assertTrue(table.search(200));

        assertEquals(2, table.getMetrics().getTotalInsertions());
        assertTrue(table.getMetrics().getTotalComparisons() >= 2); // Searching adds comparisons
        assertEquals(0, table.getMetrics().getTotalCollisions());  // Still expect no collision
    }

    /**
     * Forces a collision and tests probing with Fibonacci-based linear probing.
     */
    @Test
    public void testLinearProbingWithCollision() {
        CustomHashTable table = new CustomHashTable(8, 1, "linear", DEBUG);

        int key1 = 5;
        int key2 = 13;

        // Use known values that collide at index 2 for Fibonacci hash with multiplier
        table.insert(key1);
        table.insert(key2);

        assertTrue(table.search(key1));
        assertTrue(table.search(key2));
        assertEquals(2, table.getMetrics().getTotalInsertions());
        assertTrue(table.getMetrics().getTotalCollisions() >= 1);
    }

    /**
     * Tests quadratic probing strategy and verifies successful inserts.
     */
    @Test
    public void testQuadraticProbing() {
        CustomHashTable table = new CustomHashTable(16, 1, "quadratic", DEBUG);
        table.insert(42);
        table.insert(74); // Try to provoke quadratic steps

        assertTrue(table.search(42));
        assertTrue(table.search(74));
        assertEquals(2, table.getMetrics().getTotalInsertions());
    }

    /**
     * Verifies chaining with multiple insertions to the same index.
     */
    @Test
    public void testChainingInsertions() {
        CustomHashTable table = new CustomHashTable(8, 1, "chaining", DEBUG);
        table.insert(77);
        table.insert(77 + 8); // Often ends up in the same chain

        Object[] chains = table.getRawTable();
        boolean found = false;

        for (Object chain : chains) {
            if (chain instanceof LinkedListChain && ((LinkedListChain) chain).size() == 2) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected a chain of size 2 at some index.");
    }

    /**
     * Checks whether chaining handles exhaustion of node pool without crashing,
     * and that insertions and comparisons are still tracked properly.
     */
    @Test
    public void testChainingNodePoolExhaustion() {
        CustomHashTable table = new CustomHashTable(4, 1, "chaining", DEBUG);

        // Pool size = 2 * 4 = 8 nodes, inserting more than that
        for (int i = 0; i < 20; i++) {
            table.insert(i); // Some inserts will fail due to no free nodes
        }

        // The test is to ensure it doesn't crash or throw
        assertTrue(table.getMetrics().getTotalInsertions() <= 20); // Insertions should not exceed attempts
        assertTrue(table.getMetrics().getTotalInsertions() >= 8);  // Some insertions should have succeeded
    }

    /**
     * Validates behavior when table is full and probing strategy fails to insert.
     */
    @Test
    public void testProbingFailsWhenFull() {
        CustomHashTable table = new CustomHashTable(3, 1, "linear", DEBUG);

        table.insert(1);
        table.insert(2);
        table.insert(3);
        table.insert(4); // Should fail

        assertEquals(3, table.getMetrics().getTotalInsertions());
    }

    /**
     * Confirms load factor accuracy for Fibonacci hashed probing.
     */
    @Test
    public void testLoadFactor() {
        CustomHashTable table = new CustomHashTable(20, 1, "quadratic", DEBUG);
        for (int i = 0; i < 5; i++) table.insert(i);

        double expected = 5.0 / 20.0;
        assertEquals(expected, table.getMetrics().getLoadFactor(), 0.0001);
    }

    /**
     * Ensure illegal strategy throws exception.
     */
    @Test
    public void testInvalidStrategyThrowsError() {
        CustomHashTable table = new CustomHashTable(10, 1, "rocket-science", DEBUG);
        assertThrows(IllegalArgumentException.class, () -> table.insert(5));
    }

    /**
     * Ensure chain access fails safely when not in chaining mode.
     */
    @Test
    public void testAccessChainWithoutChainingFails() {
        CustomHashTable table = new CustomHashTable(10, 1, "linear", DEBUG);
        assertThrows(IllegalStateException.class, () -> table.getChainAt(1));
    }

    /**
     * Verifies the type of getRawTable() output depending on strategy.
     */
    @Test
    public void testRawTableAccess() {
        CustomHashTable linear = new CustomHashTable(5, 1, "linear", DEBUG);
        CustomHashTable chain = new CustomHashTable(5, 1, "chaining", DEBUG);

        assertInstanceOf(Object[].class, linear.getRawTable());
        assertInstanceOf(LinkedListChain[].class, chain.getRawTable());
    }

    /**
     * Verifies search() works correctly for all strategies.
     */
    @Test
    public void testSearchAcrossStrategies() {
        // Linear probing
        CustomHashTable linear = new CustomHashTable(16, 1, "linear", DEBUG);
        linear.insert(5);
        linear.insert(21);
        assertTrue(linear.search(5));
        assertTrue(linear.search(21));
        assertFalse(linear.search(99));

        // Quadratic probing
        CustomHashTable quadratic = new CustomHashTable(16, 1, "quadratic", DEBUG);
        quadratic.insert(7);
        quadratic.insert(23);
        assertTrue(quadratic.search(7));
        assertTrue(quadratic.search(23));
        assertFalse(quadratic.search(42));

        // Chaining
        CustomHashTable chaining = new CustomHashTable(8, 1, "chaining", DEBUG);
        chaining.insert(3);
        chaining.insert(11); // same index
        assertTrue(chaining.search(3));
        assertTrue(chaining.search(11));
        assertFalse(chaining.search(99));
    }

    /**
     * Tests quadratic probing with custom c1 and c2 values.
     * Verifies that custom constants lead to valid probing behavior.
     */
    @Test
    public void testQuadraticProbingWithCustomC1C2() {
        double c1 = 1.0;
        double c2 = 1.0;

        // Use a small table to increase the chance of collision
        CustomHashTable table = new CustomHashTable(7, 1, "quadratic", DEBUG, c1, c2);

        // Force both keys to hash to the same slot
        int firstKey = 3;
        int secondKey = 74; // Should collide with key 3 due to hash(3) ≡ hash(74)

        table.insert(firstKey);
        table.insert(secondKey);

        assertTrue(table.search(firstKey), "First key should be inserted and found.");
        assertTrue(table.search(secondKey), "Second key should be inserted via probing.");

        PerformanceMetrics metrics = table.getMetrics();
        assertEquals(2, metrics.getTotalInsertions(), "Should insert both keys.");
        assertTrue(metrics.getTotalProbes() >= 1, "Expected probing due to collision.");
        assertTrue(metrics.getTotalCollisions() >= 1, "Expected collision due to same home index.");
    }
}
