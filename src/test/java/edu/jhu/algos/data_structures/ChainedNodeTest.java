package edu.jhu.algos.data_structures;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ChainedNode class.
 * - Verifies correct behavior for key storage and linking.
 * - Verifies defensive programming and error handling.
 */
public class ChainedNodeTest {

    /**
     * Test that a node correctly stores a non-negative key.
     */
    @Test
    public void testKeyStorage() {
        ChainedNode node = new ChainedNode(42);               // Create a node with key 42
        assertEquals(42, node.key, "Node should store the correct key.");
    }

    /**
     * Test that the 'next' pointer is null by default after construction.
     */
    @Test
    public void testDefaultNextIsNull() {
        ChainedNode node = new ChainedNode(7);                // Create a node
        assertNull(node.next, "New node's next pointer should be null.");
    }

    /**
     * Test that one node can be linked to another using setNext.
     */
    @Test
    public void testSetNext() {
        ChainedNode node1 = new ChainedNode(1);               // Create first node
        ChainedNode node2 = new ChainedNode(2);               // Create second node
        node1.setNext(node2);                                 // Link node1 → node2

        assertNotNull(node1.next, "Next pointer should not be null after linking.");
        assertEquals(2, node1.next.key, "Next node should store the correct key.");
    }

    /**
     * Test that setNext throws an exception if a node tries to point to itself.
     */
    @Test
    public void testSetNextToSelfThrows() {
        ChainedNode node = new ChainedNode(99);               // Create node
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            node.setNext(node);                               // Attempt to link node to itself
        });

        assertTrue(exception.getMessage().contains("cannot point to itself"),
                "Self-linking should throw an exception.");
    }

    /**
     * Test that constructing a node with a negative key throws an exception.
     */
    @Test
    public void testNegativeKeyThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ChainedNode(-10);                             // Attempt to create a node with invalid key
        });

        assertTrue(exception.getMessage().contains("negative key"),
                "Negative key should trigger an exception.");
    }

    /**
     * Test that the createEmptyNode factory method works without error.
     * - Should return a node with key -1, used internally for memory pools.
     */
    @Test
    public void testCreateEmptyNode() {
        ChainedNode empty = ChainedNode.createEmptyNode();     // Use the static factory method
        assertEquals(-1, empty.key, "createEmptyNode() should assign key = -1.");
        assertNull(empty.next, "createEmptyNode() should initialize next to null.");
    }
}
