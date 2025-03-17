package edu.jhu.algos.hashing;

/**
 * Implements a division-based hash table.
 * - Uses modulo hashing (key % tableSize).
 * - Implements linear probing to resolve collisions.
 * - Includes defensive programming with error handling.
 */
public class DivisionHashTable extends HashTable {

    private int modValue; // Stores the modulo divisor used in hashing

    /**
     * Constructor for a division-based hash table.
     *
     * @param tableSize  Number of slots in the table (default: 120).
     * @param bucketSize Bucket size (1 for 120 slots, 3 for 40 slots).
     * @param modValue   The divisor for modulo hashing.
     * @throws IllegalArgumentException If modValue is non-positive.
     */
    public DivisionHashTable(int tableSize, int bucketSize, int modValue) {
        super(tableSize, bucketSize);

        // Validate that the modulo divisor is positive
        if (modValue <= 0) {
            throw new IllegalArgumentException("Error: modValue must be a positive integer.");
        }
        this.modValue = modValue;
    }

    /**
     * Computes the hash index using the division method.
     * The hash index is calculated as:
     *    hashIndex = abs(key) % modValue
     *
     * @param key The integer key to be hashed.
     * @return The computed index within the hash table.
     */
    @Override
    protected int hash(int key) {
        return Math.abs(key) % modValue;
    }

    /**
     * Inserts a key into the hash table using Linear Probing.
     * If a collision occurs, it probes the next available slot.
     *
     * Defensive Programming:
     * - Rejects negative keys.
     * - Prevents infinite loops if the table is full.
     * - Uses try-catch blocks for error handling.
     *
     * @param key The integer key to insert.
     */
    @Override
    public void insert(int key) {
        // Defensive check: Reject negative keys to avoid invalid inputs
        if (key < 0) {
            System.err.println("Warning: Negative key " + key + " cannot be inserted. Skipping.");
            return;
        }

        try {
            int index = hash(key); // Compute the initial hash index
            int originalIndex = index; // Store the original index for debugging
            int attempts = 0; // Track probing attempts

            // Linear Probing: Find the next available slot if a collision occurs
            while (table[index] != -1) {
                incrementCollisions(); // Increment collision counter
                index = (index + 1) % tableSize; // Move to the next slot (wraps around if needed)
                attempts++;

                // If probing cycles through all slots, the table is full
                if (attempts >= tableSize) {
                    incrementFailedInsertions();
                    System.err.println("Error: Hash table is full. Could not insert key: " + key);
                    return;
                }
            }

            // Insert the key into the available position
            table[index] = key;
            incrementComparisons(); // Track a successful comparison

            // Debugging message: Indicate if probing was required
            if (index != originalIndex) {
                System.out.println("Linear Probing: Moved key " + key + " from index " + originalIndex + " to " + index);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            // Catch and report if an out-of-bounds access occurs
            System.err.println("Critical Error: Index out of bounds while inserting key " + key);
        } catch (Exception e) {
            // Catch and report any unexpected errors
            System.err.println("Unexpected Error: " + e.getMessage());
        }
    }
}
