package edu.jhu.algos.data_structures;

import edu.jhu.algos.utils.PerformanceMetrics;

/**
 * Manages a chain of ChainedNode objects for separate chaining in hash tables.
 * - Uses a Stack<ChainedNode> as a free list to simulate memory reuse.
 * - Complies with the assignment requirement to use a stack to track free space.
 */
public class LinkedListChain {
    private ChainedNode head;                      // Head of the linked list chain
    private final Stack<ChainedNode> freeList;     // Stack to manage available (free) nodes
    private final boolean debugMode;               // Flag for debug output (optional diagnostics)

    /**
     * Constructor initializes an empty chain and assigns a shared free list.
     *
     * @param freeList A shared Stack of preallocated ChainedNode objects.
     * @param debugMode Enables debug output when true.
     */
    public LinkedListChain(Stack<ChainedNode> freeList, boolean debugMode) {
        this.head = null;
        this.freeList = freeList;
        this.debugMode = debugMode;
    }

    /**
     * Inserts a key into the chain by reusing a node from the free list.
     * Also updates performance metrics if provided.
     *
     * If no free nodes are available, insertion fails and an error is printed.
     * Nodes are inserted at the front of the chain for O(1) insertion.
     *
     * @param key The integer key to insert into the chain.
     * @param metrics Optional PerformanceMetrics object to track comparisons, insertions, and collisions.
     */
    public void insert(int key, PerformanceMetrics metrics) {
        // Defensive check: ensure memory is available in the free list
        if (freeList.isEmpty()) {
            if (debugMode) {
                System.err.println("Debug: No free nodes available for chaining. Key = " + key);
            }
            return; // Early return — no changes made
        }

        // Traverse the chain to count comparisons (simulating a search)
        ChainedNode current = head;
        while (current != null) {
            if (metrics != null) metrics.addComparison();
            current = current.next;
        }

        // Count as a collision if the chain already has entries
        if (head != null && metrics != null) {
            metrics.addCollision();
        }

        // Reuse a node from the stack
        ChainedNode node = freeList.pop();
        node.key = key;       // Assign the key to this reused node
        node.next = head;     // Link to the current head of the chain
        head = node;          // Update head to the new node

        // Count this as a successful insertion
        if (metrics != null) {
            metrics.addInsertion();
        }

        if (debugMode) {
            System.out.println("[DEBUG] Inserted key " + key + " into chain.");
        }
    }

    /**
     * Searches for a key within the chain, counting comparisons if metrics is provided.
     *
     * @param key The key to search for.
     * @param metrics Optional PerformanceMetrics object for tracking comparisons.
     * @return True if the key is found in the chain, false otherwise.
     */
    public boolean search(int key, PerformanceMetrics metrics) {
        ChainedNode current = head;

        // Traverse the list until key is found or end is reached
        while (current != null) {
            if (metrics != null) metrics.addComparison();

            if (current.key == key) {
                return true; // Key found
            }
            current = current.next;
        }

        return false; // Key not found
    }

    /**
     * Prints the contents of the chain in order from head to tail.
     * Example output: 789 -> 432 -> 120 -> null
     */
    public void printChain() {
        ChainedNode current = head;

        while (current != null) {
            System.out.print(current.key + " -> ");
            current = current.next;
        }

        System.out.println("null");
    }

    /**
     * Returns the number of nodes in the chain.
     *
     * @return The number of keys stored in this chain.
     */
    public int size() {
        int count = 0;
        ChainedNode current = head;

        while (current != null) {
            count++;
            current = current.next;
        }

        return count;
    }

    /**
     * Checks if the chain is currently empty.
     *
     * @return True if the chain contains no keys, false otherwise.
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Clears the chain and pushes all its nodes back into the free list.
     * - Useful for resetting the table or reusing the chain structure.
     */
    public void clear() {
        ChainedNode current = head;

        // Return each node to the free list
        while (current != null) {
            ChainedNode next = current.next;
            freeList.push(current);
            current = next;
        }

        head = null; // Clear the head reference
    }

    /**
     * Returns a string representation of the chain in the format:
     * key1 -> key2 -> ... -> None
     *
     * This is used by OutputFormatter to format chaining-aware output.
     *
     * @return A string representing the full chain from head to tail.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ChainedNode current = head;

        // Traverse the chain and build the string
        while (current != null) {
            sb.append(current.key).append(" -> ");
            current = current.next;
        }

        sb.append("None");
        return sb.toString();
    }
}
