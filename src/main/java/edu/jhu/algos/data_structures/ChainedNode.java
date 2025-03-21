package edu.jhu.algos.data_structures;

/**
 * A node structure for separate chaining in hash tables.
 * - Each node stores an integer key and a reference to the next node in the chain.
 * - Used for collision resolution using linked lists (chaining) within the hash table.
 */
public class ChainedNode {
    public int key;               // The integer key stored in this node
    public ChainedNode next;      // Reference to the next node in the linked list chain

    /**
     * Public constructor for creating a new ChainedNode with a valid key.
     * This constructor enforces that only non-negative keys are allowed.
     *
     * @param key The integer key to store in the node.
     * @throws IllegalArgumentException If the key is negative.
     */
    public ChainedNode(int key) {
        // Defensive check: Reject negative keys for assignment compliance
        if (key < 0) {
            throw new IllegalArgumentException("Error: ChainedNode cannot store a negative key.");
        }

        // Store the key
        this.key = key;

        // Initialize the next pointer to null (end of chain)
        this.next = null;
    }

    /**
     * Sets the 'next' reference for this node, linking it to another node in the chain.
     *
     * @param nextNode The node to be linked as the next in the list.
     * @throws IllegalArgumentException If a node is linked to itself (self-loop prevention).
     */
    public void setNext(ChainedNode nextNode) {
        // Defensive check: Prevent circular reference to itself
        if (nextNode == this) {
            throw new IllegalArgumentException("Error: Node cannot point to itself.");
        }

        // Update the next pointer
        this.next = nextNode;
    }

    /**
     * Static factory method used to create a placeholder node for internal memory pools.
     * This bypasses the key validation and assigns a default invalid key (-1) to be overwritten later.
     *
     * @return A ChainedNode with key -1 intended for reuse by memory pools.
     */
    public static ChainedNode createEmptyNode() {
        // Create a new node with a temporary valid key (0) to pass constructor check
        ChainedNode node = new ChainedNode(0);

        // Manually override the key with -1 to mark it as "empty"/placeholder
        node.key = -1;

        // Return the pre-allocated placeholder node
        return node;
    }
}
