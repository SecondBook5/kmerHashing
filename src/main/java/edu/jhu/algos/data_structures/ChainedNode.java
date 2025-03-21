package edu.jhu.algos.data_structures;

/**
 * A node structure for separate chaining in hash tables.
 * - Each node stores an integer key and a pointer to the next node in the chain.
 * - Used for collision resolution by linked list chaining (inside the table).
 */
public class ChainedNode {
    public int key;             // The integer key stored in this node
    public ChainedNode next;    // Pointer to the next node in the chain (null if none)

    /**
     * Constructs a ChainedNode to store the given key.
     *
     * Defensive programming: Throws an exception for negative keys,
     * assuming only non-negative keys are valid per assignment conventions.
     *
     * @param key The key to be stored in the node.
     * @throws IllegalArgumentException If the key is negative.
     */
    public ChainedNode(int key) {
        if (key < 0) {
            throw new IllegalArgumentException("Error: ChainedNode cannot store a negative key.");
        }
        this.key = key;
        this.next = null; // Initialize the next pointer to null
    }

    /**
     * Sets the next node in the chain.
     *
     * Defensive programming: Prevents a node from pointing to itself.
     *
     * @param nextNode The node to be linked after this node.
     * @throws IllegalArgumentException If attempting to link a node to itself.
     */
    public void setNext(ChainedNode nextNode) {
        if (nextNode == this) {
            throw new IllegalArgumentException("Error: Node cannot point to itself.");
        }
        this.next = nextNode;
    }
}
