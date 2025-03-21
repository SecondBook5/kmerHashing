package edu.jhu.algos.data_structures;

import java.util.EmptyStackException;

/**
 * A generic stack (Last-In-First-Out) implementation.
 * - Supports push, pop, peek, size, and isEmpty operations.
 * - Internally implemented using a singly linked list.
 * - Used to track free space or manage operations in hash table schemes.
 *
 * @param <T> The type of elements stored in the stack.
 */
public class Stack<T> {

    /**
     * A private internal node class for the stack.
     */
    private class StackNode {
        T value;
        StackNode next;

        StackNode(T value) {
            this.value = value;
            this.next = null;
        }
    }

    private StackNode top;  // The top of the stack
    private int size;       // Number of elements in the stack

    /**
     * Constructs an empty stack.
     */
    public Stack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes a value onto the top of the stack.
     *
     * @param value The value to be pushed.
     */
    public void push(T value) {
        StackNode newNode = new StackNode(value);
        newNode.next = top;
        top = newNode;
        size++;
    }

    /**
     * Pops the top value off the stack and returns it.
     *
     * @return The top value.
     * @throws EmptyStackException If the stack is empty.
     */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }

        T value = top.value;
        top = top.next;
        size--;
        return value;
    }

    /**
     * Returns the top value without removing it.
     *
     * @return The top value.
     * @throws EmptyStackException If the stack is empty.
     */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }

        return top.value;
    }

    /**
     * Checks whether the stack is empty.
     *
     * @return True if the stack has no elements.
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Returns the number of elements in the stack.
     *
     * @return Stack size.
     */
    public int size() {
        return size;
    }
}
