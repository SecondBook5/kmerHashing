package edu.jhu.algos.hashing;

import edu.jhu.algos.data_structures.ChainedNode;
import edu.jhu.algos.data_structures.LinkedListChain;
import edu.jhu.algos.data_structures.Stack;
import edu.jhu.algos.utils.PerformanceMetrics;
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
     * Verifies clean inserts using linear probing (no collisions).
     */
    @Test
    public void testLinearProbingNoCollision() {
        CustomHashTable table = new CustomHashTable(10, 1, "linear", DEBUG);
        table.insert(123);

        assertTrue(table.search(123));
        assertEquals(1, table.getMetrics().getTotalInsertions());
        assertEquals(1, table.getMetrics().getTotalComparisons());
    }

    /**
     * Forces a collision and tests probing with Fibonacci-based linear probing.
     */
    @Test
    public void testLinearProbingWithCollision() {
        CustomHashTable table = new CustomHashTable(5, 1, "linear", DEBUG);

        table.insert(5);   // hashes to some index i
        table.insert(5 + 5); // likely to hash to same index i

        assertEquals(2, table.getMetrics().getTotalInsertions());
        assertTrue(table.getMetrics().getTotalCollisions() >= 1);
    }

    /**
     * Tests quadratic probing strategy.
     */
    @Test
    public void testQuadraticProbing() {
        CustomHashTable table = new CustomHashTable(10, 1, "quadratic", DEBUG);
        table.insert(10);
        table.insert(10 + 10); // likely to cause a quadratic step

        assertEquals(2, table.getMetrics().getTotalInsertions());
        assertTrue(table.search(10));
    }

    /**
     * Verifies chaining with multiple items in same slot.
     */
    @Test
    public void testChainingInsertions() {
        CustomHashTable table = new CustomHashTable(8, 1, "chaining", DEBUG);

        int index = table.hash(777); // get hashed index
        table.insert(777);
        table.insert(777 + 8); // likely to go to same index

        LinkedListChain chain = table.getChainAt(index);
        assertEquals(2, chain.size());
        assertTrue(chain.search(777));
        assertTrue(chain.search(777 + 8));
    }

    /**
     * Checks whether chaining handles exhaustion of node pool.
     */
    @Test
    public void testChainingNodePoolExhaustion() {
        CustomHashTable table = new CustomHashTable(3, 1, "chaining", DEBUG);

        for (int i = 0; i < 10_000; i++) {
            table.insert(i);
        }

        assertTrue(table.getMetrics().getTotalInsertions() <= 6000); // conservatively avoids OOM
    }

    /**
     * Validates that failed insert (when table is full) doesn’t increase insert count.
     */
    @Test
    public void testProbingFailsWhenFull() {
        CustomHashTable table = new CustomHashTable(3, 1, "linear", DEBUG);

        table.insert(1);
        table.insert(2);
        table.insert(3);
        table.insert(4); // should fail

        assertEquals(3, table.getMetrics().getTotalInsertions());
    }

    /**
     * Confirms load factor accuracy under probing mode.
     */
    @Test
    public void testLoadFactor() {
        CustomHashTable table = new CustomHashTable(20, 1, "quadratic", DEBUG);
        for (int i = 0; i < 5; i++) table.insert(i);

        double expected = 5.0 / 20.0;
        assertEquals(expected, table.getMetrics().getLoadFactor(), 0.0001);
    }

    /**
     * Ensure that illegal strategy fails fast.
     */
    @Test
    public void testInvalidStrategyThrowsError() {
        assertThrows(IllegalArgumentException.class, () -> new CustomHashTable(10, 1, "rocket-science", DEBUG));
    }

    /**
     * Ensure chained table access fails safely when chaining is disabled.
     */
    @Test
    public void testAccessChainWithoutChainingFails() {
        CustomHashTable table = new CustomHashTable(10, 1, "linear", DEBUG);
        assertThrows(IllegalStateException.class, () -> table.getChainAt(1));
    }

    /**
     * Ensure getRawTable() returns correct structure.
     */
    @Test
    public void testRawTableAccess() {
        CustomHashTable linear = new CustomHashTable(5, 1, "linear", DEBUG);
        CustomHashTable chain = new CustomHashTable(5, 1, "chaining", DEBUG);

        assertInstanceOf(Integer[].class, linear.getRawTable());
        assertInstanceOf(LinkedListChain[].class, chain.getRawTable());
    }
}
