package edu.jhu.algos.data_structures;

import edu.jhu.algos.utils.PerformanceMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LinkedListChain class with PerformanceMetrics tracking.
 */
public class LinkedListChainTest {

    /**
     * Helper method to create a reusable memory pool for chaining.
     * This pool simulates preallocated memory using a Stack of placeholder nodes.
     *
     * @param count The number of reusable nodes to create.
     * @return A stack of ChainedNodes with placeholder keys.
     */
    private Stack<ChainedNode> makeNodePool(int count) {
        Stack<ChainedNode> pool = new Stack<>();

        // Fill the pool with safe placeholder nodes using the factory method
        for (int i = 0; i < count; i++) {
            pool.push(ChainedNode.createEmptyNode());
        }
        return pool;
    }

    /**
     * Test insertion and search functionality.
     * Verifies that keys can be inserted into the chain and then located.
     */
    @Test
    public void testInsertAndSearch() {
        Stack<ChainedNode> pool = makeNodePool(3);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        chain.insert(10, metrics);
        chain.insert(20, metrics);
        chain.insert(30, metrics);

        assertTrue(chain.search(10, metrics));
        assertTrue(chain.search(20, metrics));
        assertTrue(chain.search(30, metrics));
        assertFalse(chain.search(99, metrics));

        assertEquals(3, metrics.getTotalInsertions());
        assertEquals(12, metrics.getTotalComparisons());
    }

    /**
     * Test that inserting beyond the capacity of the memory pool is handled gracefully.
     * The extra insert should not throw an exception or corrupt the chain.
     */
    @Test
    public void testInsertFailsWhenPoolEmpty() {
        Stack<ChainedNode> pool = makeNodePool(1);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        chain.insert(5, metrics);
        assertEquals(1, chain.size());

        chain.insert(10, metrics);
        assertEquals(1, chain.size());
        assertFalse(chain.search(10, metrics));

        assertEquals(1, metrics.getTotalInsertions());
    }

    /**
     * Test that calling clear() on the chain returns nodes to the pool.
     * This simulates freeing memory back into the reusable stack.
     */
    @Test
    public void testClearReturnsNodes() {
        Stack<ChainedNode> pool = makeNodePool(2);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        chain.insert(42, metrics);
        chain.insert(84, metrics);

        assertEquals(0, pool.size());
        assertEquals(2, chain.size());

        // Clear the chain and verify that nodes are returned
        chain.clear();
        assertEquals(2, pool.size());
        assertTrue(chain.isEmpty());
    }

    /**
     * Test that the size() method returns the correct number of items in the chain.
     */
    @Test
    public void testSizeTracking() {
        Stack<ChainedNode> pool = makeNodePool(4);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        assertEquals(0, chain.size());

        chain.insert(1, metrics);
        chain.insert(2, metrics);
        assertEquals(2, chain.size());
    }

    /**
     * Test that isEmpty() accurately reflects the state of the chain.
     */
    @Test
    public void testIsEmpty() {
        Stack<ChainedNode> pool = makeNodePool(2);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        assertTrue(chain.isEmpty());

        chain.insert(7, metrics);
        assertFalse(chain.isEmpty());

        chain.clear();
        assertTrue(chain.isEmpty());
    }

    /**
     * Test insertion of duplicate keys.
     * Confirms that duplicates are allowed and counted.
     */
    @Test
    public void testDuplicateKeyInsertion() {
        Stack<ChainedNode> pool = makeNodePool(3);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        chain.insert(55, metrics);
        chain.insert(55, metrics);
        chain.insert(55, metrics);

        assertEquals(3, chain.size());
        assertTrue(chain.search(55, metrics));
        assertEquals(3, metrics.getTotalInsertions());
    }

    /**
     * Test the toString() method returns correct formatted chain representation.
     */
    @Test
    public void testToStringOutput() {
        Stack<ChainedNode> pool = makeNodePool(3);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        chain.insert(100, metrics);
        chain.insert(200, metrics);
        chain.insert(300, metrics);

        String result = chain.toString();
        assertTrue(result.startsWith("300 -> 200 -> 100 -> "));
        assertTrue(result.endsWith("None"));
        assertEquals("300 -> 200 -> 100 -> None", result);
    }

    @Test
    public void testToStringAfterClear() {
        Stack<ChainedNode> pool = makeNodePool(2);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        chain.insert(1, metrics);
        chain.insert(2, metrics);
        chain.clear();

        assertEquals("None", chain.toString());
    }

    /**
     * Verifies that each node in the chain is compared during search,
     * and that PerformanceMetrics correctly counts comparisons.
     */
    @Test
    public void testSearchComparisonCounting() {
        Stack<ChainedNode> pool = makeNodePool(5);
        LinkedListChain chain = new LinkedListChain(pool, false);
        PerformanceMetrics metrics = new PerformanceMetrics();

        // Insert values into chain: head will be 50 → 40 → 30
        chain.insert(30, metrics);
        chain.insert(40, metrics);
        chain.insert(50, metrics); // head

        // Reset comparisons so we only count during search
        metrics.resetAll();

        // Search for tail node (30) → 3 comparisons
        assertTrue(chain.search(30, metrics));
        assertEquals(3, metrics.getTotalComparisons(), "Expected 3 comparisons for key at tail");

        // Reset and search for middle node (40) → 2 comparisons
        metrics.resetAll();
        assertTrue(chain.search(40, metrics));
        assertEquals(2, metrics.getTotalComparisons(), "Expected 2 comparisons for middle key");

        // Reset and search for nonexistent key → 3 comparisons
        metrics.resetAll();
        assertFalse(chain.search(999, metrics));
        assertEquals(3, metrics.getTotalComparisons(), "Expected 3 comparisons for missing key");
    }
    /**
     * Test that the contains() method correctly identifies keys in the chain.
     * Unlike search(), contains() does NOT track comparisons.
     */
    @Test
    public void testContainsWithoutMetrics() {
        Stack<ChainedNode> pool = makeNodePool(3);
        PerformanceMetrics metrics = new PerformanceMetrics();
        LinkedListChain chain = new LinkedListChain(pool, false);

        chain.insert(11, metrics);
        chain.insert(22, metrics);
        chain.insert(33, metrics);

        assertTrue(chain.contains(11));
        assertTrue(chain.contains(22));
        assertTrue(chain.contains(33));
        assertFalse(chain.contains(999));
    }
}
