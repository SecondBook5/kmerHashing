package edu.jhu.algos.hashing;

import edu.jhu.algos.utils.PerformanceMetrics;
import edu.jhu.algos.data_structures.LinkedListChain;
import edu.jhu.algos.data_structures.Stack;
import edu.jhu.algos.data_structures.ChainedNode;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ProbingStrategy class.
 * Tests both open addressing (linear and quadratic probing) and chaining strategies.
 * Verifies correct insert behavior and metric tracking.
 */
public class ProbingStrategyTest {

    private static final boolean DEBUG = true;

    /**
     * Helper method to create an empty node pool for chaining tests.
     */
    private Stack<ChainedNode> makeNodePool(int count) {
        Stack<ChainedNode> pool = new Stack<>();
        for (int i = 0; i < count; i++) {
            pool.push(ChainedNode.createEmptyNode());
        }
        return pool;
    }

    /**
     * Test linear probing inserts the key into the first available slot.
     */
    @Test
    public void testLinearProbingInsertsCorrectly() {
        Integer[] table = new Integer[5];
        PerformanceMetrics metrics = new PerformanceMetrics();

        // Pre-fill index 2 to force a collision
        table[2] = 111;

        // Insert key 222 starting at index 2, should go to index 3
        ProbingStrategy.insertWithProbing(
                table, 222, 2, 5, false, metrics, 0.5, 0.5, DEBUG);

        assertEquals(111, table[2]);
        assertEquals(222, table[3]);
        assertEquals(1, metrics.getTotalCollisions());
        assertEquals(2, metrics.getTotalComparisons());
        assertEquals(1, metrics.getTotalInsertions());
        assertEquals(1, metrics.getTotalProbes());
    }

    /**
     * Test quadratic probing inserts into the correct location after probing.
     */
    @Test
    public void testQuadraticProbingInsertsCorrectly() {
        Integer[] table = new Integer[7];
        PerformanceMetrics metrics = new PerformanceMetrics();

        // Fill index 3 to force a collision
        table[3] = 1000;

        // Attempt to insert key 2000 at index 3 using quadratic probing
        ProbingStrategy.insertWithProbing(
                table, 2000, 3, 7, true, metrics, 0.5, 0.5, DEBUG);

        assertEquals(1000, table[3]);
        assertEquals(2000, table[4]);
        assertEquals(1, metrics.getTotalCollisions());
        assertEquals(2, metrics.getTotalComparisons());
        assertEquals(1, metrics.getTotalInsertions());
        assertEquals(1, metrics.getTotalProbes());
    }

    /**
     * Test that chaining correctly inserts into a LinkedListChain.
     */
    @Test
    public void testChainingInsertWorks() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        Stack<ChainedNode> pool = makeNodePool(2);

        LinkedListChain[] chainTable = new LinkedListChain[5];
        chainTable[1] = new LinkedListChain(pool);

        ProbingStrategy.insertWithChaining(chainTable, 99, 1, metrics, DEBUG);
        ProbingStrategy.insertWithChaining(chainTable, 88, 1, metrics, DEBUG);

        assertTrue(chainTable[1].search(99));
        assertTrue(chainTable[1].search(88));
        assertEquals(2, chainTable[1].size());
        assertEquals(2, metrics.getTotalInsertions());
    }

    /**
     * Test chaining fails gracefully when the slot is not initialized.
     */
    @Test
    public void testChainingInsertFailsOnNull() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        // No chain initialized at index 2
        LinkedListChain[] chainTable = new LinkedListChain[3];

        ProbingStrategy.insertWithChaining(chainTable, 55, 2, metrics, DEBUG);

        // Nothing should be inserted
        assertEquals(0, metrics.getTotalInsertions());
    }

    /**
     * Test linear probing fails gracefully when the table is full.
     */
    @Test
    public void testProbingFailsWhenFull() {
        Integer[] table = new Integer[3];
        PerformanceMetrics metrics = new PerformanceMetrics();

        // Fill entire table
        table[0] = 1;
        table[1] = 2;
        table[2] = 3;

        // Attempt to insert, no room
        ProbingStrategy.insertWithProbing(
                table, 999, 0, 3, false, metrics, 0.5, 0.5, DEBUG);

        assertFalse(arrayContains(table, 999));
        assertEquals(3, metrics.getTotalComparisons());
        assertEquals(0, metrics.getTotalInsertions());
        assertEquals(3, metrics.getTotalCollisions());
        assertEquals(3, metrics.getTotalProbes());
    }

    /**
     * Test that primary and secondary collisions are tracked correctly during probing.
     * - Forces a primary collision at i=0 and a secondary collision at i=1.
     */
    @Test
    public void testPrimaryAndSecondaryCollisionCounts() {
        Integer[] table = new Integer[5];
        PerformanceMetrics metrics = new PerformanceMetrics();

        // Force both types of collisions:
        // Fill index 1 (home slot for hash), and index 2 (next probe)
        table[1] = 111;
        table[2] = 222;

        // Try to insert a key that hashes to 1 → should hit index 1 (primary)
        // then hit index 2 (secondary), then go to index 3
        ProbingStrategy.insertWithProbing(
                table, 333, 1, 5, false, metrics, 0.5, 0.5, DEBUG);

        // Validate slot 3 got the key
        assertEquals(333, table[3], "Key should be inserted at index 3 after 2 collisions.");

        // Validate counters
        assertEquals(1, metrics.getPrimaryCollisions(), "Should count 1 primary collision.");
        assertEquals(1, metrics.getSecondaryCollisions(), "Should count 1 secondary collision.");
        assertEquals(2, metrics.getTotalCollisions(), "Total collisions should be 2.");
        assertEquals(3, metrics.getTotalComparisons(), "Three comparisons: index 1, 2, then 3.");
        assertEquals(2, metrics.getTotalProbes(), "Two probes before successful insert.");
        assertEquals(1, metrics.getTotalInsertions(), "One successful insertion.");
    }

    /**
     * Test that quadratic probing properly tracks primary and secondary collisions.
     * - Forces a primary collision at i=0 and a secondary collision at i=1.
     */
    @Test
    public void testQuadraticProbingPrimaryAndSecondaryCollisions() {
        Integer[] table = new Integer[7];
        PerformanceMetrics metrics = new PerformanceMetrics();

        // Use mod 7 hash, key maps to index 2
        table[2] = 1000;  // Home slot (primary collision)
        table[3] = 2000;  // First quadratic probe (i=1): (2 + 0.5*1 + 0.5*1^2) % 7 = (2 + 1) % 7 = 3

        // Should land on index 6 eventually
        ProbingStrategy.insertWithProbing(
                table, 3000, 2, 7, true, metrics, 0.5, 0.5, DEBUG);

        assertEquals(3000, table[5], "Key should be inserted at index 6 after 2 collisions.");
        assertEquals(1, metrics.getPrimaryCollisions(), "Should record 1 primary collision.");
        assertEquals(1, metrics.getSecondaryCollisions(), "Should record 1 secondary collision.");
        assertEquals(2, metrics.getTotalCollisions(), "Total collisions should be 2.");
        assertEquals(3, metrics.getTotalComparisons(), "Three comparisons before insertion.");
        assertEquals(2, metrics.getTotalProbes(), "Two probe steps (i=1 and i=2).");
        assertEquals(1, metrics.getTotalInsertions(), "One successful insertion.");

        System.out.println("Final table state:");
        for (int i = 0; i < table.length; i++) {
            System.out.printf("[%d]: %s\n", i, table[i] == null ? "null" : table[i].toString());
        }
    }

    /**
     * Test that chaining records insertion metrics correctly and preserves chain structure.
     */
    @Test
    public void testChainingInsertionMetrics() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        Stack<ChainedNode> pool = makeNodePool(5); // Allow space for chaining

        LinkedListChain[] chainTable = new LinkedListChain[4];
        chainTable[0] = new LinkedListChain(pool);

        ProbingStrategy.insertWithChaining(chainTable, 11, 0, metrics, DEBUG);
        ProbingStrategy.insertWithChaining(chainTable, 22, 0, metrics, DEBUG);
        ProbingStrategy.insertWithChaining(chainTable, 33, 0, metrics, DEBUG);

        // Validate chain contents and metric count
        assertEquals(3, chainTable[0].size(), "Chain at index 0 should contain 3 items.");
        assertTrue(chainTable[0].search(11), "Chain should contain key 11.");
        assertTrue(chainTable[0].search(22), "Chain should contain key 22.");
        assertTrue(chainTable[0].search(33), "Chain should contain key 33.");
        assertEquals(3, metrics.getTotalInsertions(), "Metrics should show 3 successful insertions.");
    }

    /**
     * Test performance impact of different c1 and c2 values for quadratic probing.
     * This is useful for plotting asymptotic costs.
     */
    @Test
    public void testQuadraticProbingParameterSweep() {
        Integer[] table = new Integer[20];
        double[] c1Values = {0.1, 0.5, 1.0};
        double[] c2Values = {0.1, 0.5, 1.0};

        System.out.printf("%-10s %-10s %-20s%n", "c1", "c2", "Total Comparisons");

        for (double c1 : c1Values) {
            for (double c2 : c2Values) {
                // Clear the table
                Arrays.fill(table, null);

                PerformanceMetrics metrics = new PerformanceMetrics();

                // Insert 10 keys (force collisions by choosing similar keys)
                for (int k = 0; k < 10; k++) {
                    int key = 100 + k * 3; // values spaced close together
                    int index = key % table.length;

                    ProbingStrategy.insertWithProbing(table, key, index, table.length, true, metrics, c1, c2, false);
                }

                System.out.printf("%-10.2f %-10.2f %-20d%n", c1, c2, metrics.getTotalComparisons());
            }
        }
    }


    /**
     * Helper method to confirm if an array contains a value.
     */
    private boolean arrayContains(Integer[] array, int value) {
        for (Integer item : array) {
            if (item != null && item == value) {
                return true;
            }
        }
        return false;
    }
}
