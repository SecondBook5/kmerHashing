package edu.jhu.algos.data_structures;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ChainedNode class.
 * Verifies correct behavior and defensive error handling.
 */
public class ChainedNodeTest {

    /**
     * Test that a node correctly stores the key.
     */
    @Test
    public void testKeyStorage() {
        ChainedNode node = new ChainedNode(42);
        assertEquals(42, node.key, "The node should store the correct key.");
    }

    /**
     * Test that the next pointer is null by default.
     */
    @Test
    public void testDefaultNextIsNull() {
        ChainedNode node = new ChainedNode(7);
        assertNull(node.next, "The next pointer should be null by default.");
    }

    /**
     * Test that setNext properly links nodes.
     */
    @Test
    public void testSetNext() {
        ChainedNode node1 = new ChainedNode(1);
        ChainedNode node2 = new ChainedNode(2);

        node1.setNext(node2);

        assertNotNull(node1.next, "The next pointer should not be null after linking.");
        assertEquals(2, node1.next.key, "The next node should store the correct key.");
    }

    /**
     * Test that setting a node to point to itself throws an exception.
     */
    @Test
    public void testSetNextToSelfThrows() {
        ChainedNode node = new ChainedNode(99);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> node.setNext(node));
        assertTrue(exception.getMessage().contains("cannot point to itself"));
    }

    /**
     * Test that negative keys are rejected by the constructor.
     */
    @Test
    public void testNegativeKeyThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ChainedNode(-10));
        assertTrue(exception.getMessage().contains("negative key"));
    }
}
