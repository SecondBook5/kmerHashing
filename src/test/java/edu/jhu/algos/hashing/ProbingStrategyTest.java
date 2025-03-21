package edu.jhu.algos.hashing;

import edu.jhu.algos.utils.PerformanceMetrics;
import edu.jhu.algos.data_structures.LinkedListChain;
import edu.jhu.algos.data_structures.Stack;
import edu.jhu.algos.data_structures.ChainedNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ProbingStrategy class.
 *
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
