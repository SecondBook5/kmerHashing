package edu.jhu.algos.data_structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LinkedListChain class.
 *
 * This test suite verifies:
 * - Correct insert and search behavior
 * - Memory reuse using a preallocated stack-based pool
 * - Handling of exhausted memory pools
 * - Accurate size and empty status reporting
 * - Behavior when inserting duplicate keys
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
            pool.push(ChainedNode.createEmptyNode());  // Safe placeholder creation
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
        LinkedListChain chain = new LinkedListChain(pool);

        // Insert three keys into the chain
        chain.insert(10);
        chain.insert(20);
        chain.insert(30);

        // Search for inserted keys (should be found)
        assertTrue(chain.search(10));
        assertTrue(chain.search(20));
        assertTrue(chain.search(30));

        // Search for a key not in the chain (should not be found)
        assertFalse(chain.search(99));
    }

    /**
     * Test that inserting beyond the capacity of the memory pool is handled gracefully.
     * The extra insert should not throw an exception or corrupt the chain.
     */
    @Test
    public void testInsertFailsWhenPoolEmpty() {
        Stack<ChainedNode> pool = makeNodePool(1);
        LinkedListChain chain = new LinkedListChain(pool);

        // Insert the only allowed key
        chain.insert(5);
        assertEquals(1, chain.size());

        // This insertion should fail silently (no more free nodes)
        chain.insert(10);
        assertEquals(1, chain.size());         // No new key inserted
        assertFalse(chain.search(10));         // 10 should not be found
    }

    /**
     * Test that calling clear() on the chain returns nodes to the pool.
     * This simulates freeing memory back into the reusable stack.
     */
    @Test
    public void testClearReturnsNodes() {
        Stack<ChainedNode> pool = makeNodePool(2);
        LinkedListChain chain = new LinkedListChain(pool);

        // Use all available nodes
        chain.insert(42);
        chain.insert(84);

        assertEquals(0, pool.size());          // Pool should be empty
        assertEquals(2, chain.size());         // Chain should hold both keys

        // Clear the chain and verify that nodes are returned
        chain.clear();
        assertEquals(2, pool.size());          // All nodes returned to pool
        assertTrue(chain.isEmpty());           // Chain should now be empty
    }

    /**
     * Test that the size() method returns the correct number of items in the chain.
     */
    @Test
    public void testSizeTracking() {
        Stack<ChainedNode> pool = makeNodePool(4);
        LinkedListChain chain = new LinkedListChain(pool);

        assertEquals(0, chain.size());         // Empty at start

        chain.insert(1);
        chain.insert(2);
        assertEquals(2, chain.size());         // Should have two items
    }

    /**
     * Test that isEmpty() accurately reflects the state of the chain.
     */
    @Test
    public void testIsEmpty() {
        Stack<ChainedNode> pool = makeNodePool(2);
        LinkedListChain chain = new LinkedListChain(pool);

        assertTrue(chain.isEmpty());           // New chain is empty

        chain.insert(7);
        assertFalse(chain.isEmpty());          // Should not be empty after insert

        chain.clear();
        assertTrue(chain.isEmpty());           // Should be empty again after clear
    }

    /**
     * Test insertion of duplicate keys.
     * Confirms that duplicates are allowed and counted.
     */
    @Test
    public void testDuplicateKeyInsertion() {
        Stack<ChainedNode> pool = makeNodePool(3);
        LinkedListChain chain = new LinkedListChain(pool);

        chain.insert(55);
        chain.insert(55);
        chain.insert(55);  // Same key inserted 3 times

        assertEquals(3, chain.size(), "All duplicates should be stored.");
        assertTrue(chain.search(55), "Duplicate key should be searchable.");
    }
}
