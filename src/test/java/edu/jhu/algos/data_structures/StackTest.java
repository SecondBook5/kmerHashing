package edu.jhu.algos.data_structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.EmptyStackException;

/**
 * Unit tests for the custom Stack<T> data structure.
 */
public class StackTest {

    /**
     * Test pushing a single item and peeking it.
     */
    @Test
    public void testPushAndPeek() {
        Stack<Integer> stack = new Stack<>();
        stack.push(10);

        assertEquals(1, stack.size(), "Stack size should be 1 after one push.");
        assertEquals(10, stack.peek(), "Top of the stack should be 10.");
    }

    /**
     * Test pushing multiple items and ensure LIFO behavior.
     */
    @Test
    public void testPushMultipleAndPopInLIFOOrder() {
        Stack<String> stack = new Stack<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");

        assertEquals("C", stack.pop(), "First pop should return last pushed value (C).");
        assertEquals("B", stack.pop(), "Next pop should return B.");
        assertEquals("A", stack.pop(), "Final pop should return A.");
        assertTrue(stack.isEmpty(), "Stack should be empty after all pops.");
    }

    /**
     * Test that pop throws an exception on an empty stack.
     */
    @Test
    public void testPopEmptyThrowsException() {
        Stack<Double> stack = new Stack<>();

        assertThrows(EmptyStackException.class, stack::pop, "Popping an empty stack should throw EmptyStackException.");
    }

    /**
     * Test that peek throws an exception on an empty stack.
     */
    @Test
    public void testPeekEmptyThrowsException() {
        Stack<Character> stack = new Stack<>();

        assertThrows(EmptyStackException.class, stack::peek, "Peeking an empty stack should throw EmptyStackException.");
    }

    /**
     * Test isEmpty on a new and cleared stack.
     */
    @Test
    public void testIsEmpty() {
        Stack<Integer> stack = new Stack<>();
        assertTrue(stack.isEmpty(), "New stack should be empty.");

        stack.push(1);
        assertFalse(stack.isEmpty(), "Stack should not be empty after push.");

        stack.pop();
        assertTrue(stack.isEmpty(), "Stack should be empty after popping all elements.");
    }

    /**
     * Test size increases and decreases properly.
     */
    @Test
    public void testSizeTracking() {
        Stack<String> stack = new Stack<>();
        assertEquals(0, stack.size());

        stack.push("X");
        stack.push("Y");
        assertEquals(2, stack.size());

        stack.pop();
        assertEquals(1, stack.size());

        stack.pop();
        assertEquals(0, stack.size());
    }
}
