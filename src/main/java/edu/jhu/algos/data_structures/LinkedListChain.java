package edu.jhu.algos.data_structures;

/**
 * Manages a chain of ChainedNode objects for separate chaining in hash tables.
 * - Uses a Stack<ChainedNode> as a free list to simulate memory reuse.
 * - Complies with the assignment requirement to use a stack to track free space.
 */
public class LinkedListChain {
    private ChainedNode head;                      // Head of the linked list chain
    private final Stack<ChainedNode> freeList;     // Stack to manage available (free) nodes

    /**
     * Constructor initializes an empty chain and assigns a shared free list.
     *
     * @param freeList A shared Stack of preallocated ChainedNode objects.
     */
    public LinkedListChain(Stack<ChainedNode> freeList) {
        this.head = null;
        this.freeList = freeList;
    }

    /**
     * Inserts a key into the chain by reusing a node from the free list.
     *
     * If no free nodes are available, insertion fails and an error is printed.
     * Nodes are inserted at the front of the chain for O(1) insertion.
     *
     * @param key The integer key to insert into the chain.
     */
    public void insert(int key) {
        // Defensive check: ensure memory is available in the free list
        if (freeList.isEmpty()) {
            System.err.println("Error: No free nodes available for chaining.");
            return;
        }

        // Reuse a node from the stack
        ChainedNode node = freeList.pop();
        node.key = key;       // Assign the key to this reused node
        node.next = head;     // Link to the current head of the chain
        head = node;          // Update head to the new node
    }

    /**
     * Searches for a key within the chain.
     *
     * @param key The key to search for.
     * @return True if the key is found in the chain, false otherwise.
     */
    public boolean search(int key) {
        ChainedNode current = head;

        // Traverse the list until key is found or end is reached
        while (current != null) {
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
}
