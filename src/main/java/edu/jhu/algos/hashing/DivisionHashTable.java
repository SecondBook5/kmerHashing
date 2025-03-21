package edu.jhu.algos.hashing;

import edu.jhu.algos.utils.PerformanceMetrics;
import edu.jhu.algos.data_structures.LinkedListChain;
import edu.jhu.algos.data_structures.Stack;
import edu.jhu.algos.data_structures.ChainedNode;

/**
 * DivisionHashTable is a concrete subclass of the abstract HashTable.
 * It implements hashing using the division method and supports:
 * - Linear Probing
 * - Quadratic Probing
 * - Chaining (using linked lists inside the table)
 */
public class DivisionHashTable extends HashTable {

    private final int modValue;        // Divisor used in division hashing
    private final String strategy;     // Strategy: "linear", "quadratic", or "chaining"
    private final boolean debug;       // Whether to print debug output

    private LinkedListChain[] chainTable;  // Used only if strategy is "chaining"
    private Stack<ChainedNode> nodePool;   // Shared node pool for memory-efficient chaining

    /**
     * Constructor for DivisionHashTable.
     *
     * @param tableSize   Total number of slots in the table (typically 120).
     * @param bucketSize  Size of each bucket (1 or 3).
     * @param modValue    The divisor for the division hashing function.
     * @param strategy    Collision resolution strategy: "linear", "quadratic", or "chaining".
     * @param debug       Whether to enable debug output.
     */
    public DivisionHashTable(int tableSize, int bucketSize, int modValue, String strategy, boolean debug) {
        super(tableSize, bucketSize);         // Call superclass constructor
        this.modValue = modValue;             // Store modulo value for hashing
        this.strategy = strategy.toLowerCase(); // Normalize strategy for comparison
        this.debug = debug;

        // If chaining strategy is chosen, initialize linked lists and node pool
        if (this.strategy.equals("chaining")) {
            if (debug) {
                System.out.println("[DEBUG] Initializing chaining support...");
            }
            initChainingSupport();
        }
    }

    /**
     * Initializes the chaining-specific data structures:
     * - A LinkedListChain[] for each table slot
     * - A shared stack of preallocated ChainedNodes for memory reuse
     */
    private void initChainingSupport() {
        this.chainTable = new LinkedListChain[tableSize];
        this.nodePool = new Stack<>();

        // Preallocate 2× the number of table slots for chaining nodes
        for (int i = 0; i < tableSize * 2; i++) {
            nodePool.push(ChainedNode.createEmptyNode());
        }

        // Initialize one LinkedListChain per table index
        for (int i = 0; i < tableSize; i++) {
            chainTable[i] = new LinkedListChain(nodePool);
        }

        if (debug) {
            System.out.println("[DEBUG] Chaining support initialized with " + (tableSize * 2) + " preallocated nodes.");
        }
    }

    /**
     * Computes the hash index using the division method.
     * Ensures the result is non-negative.
     *
     * @param key The key to hash.
     * @return The hashed index: abs(key) % modValue
     */
    @Override
    protected int hash(int key) {
        int index = Math.abs(key) % modValue;

        if (debug) {
            System.out.printf("[DEBUG] Hashed key %d → index %d using mod %d%n", key, index, modValue);
        }

        return index;
    }

    /**
     * Inserts a key into the hash table using the configured strategy.
     * Delegates collision handling to the appropriate probing or chaining logic.
     *
     * @param key The integer key to insert.
     */
    @Override
    public void insert(int key) {
        int index = hash(key);  // Compute the home slot

        if (debug) {
            System.out.printf("[DEBUG] Inserting key %d using strategy: %s%n", key, strategy);
        }

        // Route to the appropriate collision handling method
        switch (strategy) {
            case "linear":
                ProbingStrategy.insertWithProbing(
                        table, key, index, tableSize, false, metrics, 0.5, 0.5, debug);
                break;

            case "quadratic":
                ProbingStrategy.insertWithProbing(
                        table, key, index, tableSize, true, metrics, 0.5, 0.5, debug);
                break;

            case "chaining":
                ProbingStrategy.insertWithChaining(chainTable, key, index, metrics, debug);
                break;

            default:
                throw new IllegalArgumentException("Unknown probing strategy: " + strategy);
        }
    }

    /**
     * Prints the contents of the hash table to the console.
     * - For chaining, prints linked list chains at each index.
     * - For probing, defers to the formatting logic in HashTable.
     */
    @Override
    public void printTable() {
        if (debug) {
            System.out.println("[DEBUG] Printing table for strategy: " + strategy);
        }

        if (strategy.equals("chaining")) {
            // Print one line per chained slot
            for (int i = 0; i < tableSize; i++) {
                System.out.printf("%2d ", i);
                chainTable[i].printChain();  // Custom print for chaining
            }
        } else {
            super.printTable();  // Use inherited format for probing tables
        }
    }

    /**
     * Clears the table contents and resets performance metrics.
     * - If chaining, also returns all nodes to the node pool.
     */
    @Override
    public void clearTable() {
        if (debug) {
            System.out.println("[DEBUG] Clearing hash table and resetting metrics...");
        }

        super.clearTable();  // Reset array and metrics

        if (strategy.equals("chaining")) {
            for (int i = 0; i < tableSize; i++) {
                chainTable[i].clear();  // Clear each list and return nodes to pool
            }

            if (debug) {
                System.out.println("[DEBUG] Chained lists cleared and nodes returned to pool.");
            }
        }
    }

    /**
     * Public accessor for retrieving a specific chain for testing.
     *
     * @param index The index of the chain in the table.
     * @return The LinkedListChain at that index.
     * @throws IllegalStateException if chaining is not enabled.
     */
    public LinkedListChain getChainAt(int index) {
        if (!strategy.equals("chaining")) {
            throw new IllegalStateException("Chaining is not enabled for this hash table.");
        }
        return chainTable[index];
    }
}
