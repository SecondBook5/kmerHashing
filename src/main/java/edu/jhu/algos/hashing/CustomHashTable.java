package edu.jhu.algos.hashing;

import edu.jhu.algos.data_structures.ChainedNode;
import edu.jhu.algos.data_structures.LinkedListChain;
import edu.jhu.algos.data_structures.Stack;
import edu.jhu.algos.utils.PerformanceMetrics;

/**
 * CustomHashTable uses Fibonacci hashing (Knuth's multiplicative method)
 * to implement a high-performance hash table with three supported strategies:
 * - Linear probing
 * - Quadratic probing
 * - Separate chaining using linked lists
 */
public class CustomHashTable extends HashTable {

    private final String strategy;     // Collision handling strategy: "linear", "quadratic", or "chaining"
    private final boolean debug;       // Debug flag for console output

    private LinkedListChain[] chainTable; // Used only if chaining strategy is enabled
    private Stack<ChainedNode> nodePool;  // Shared pool of reusable nodes for chaining

    // Compute golden ratio φ = (1 + √5) / 2 as a double
    private static final double PHI = (1 + Math.sqrt(5)) / 2;

    // Compute the multiplicative constant for Fibonacci hashing: ⌊2^64 / φ⌋
    // We use unsigned multiplication via long, so this constant must fit within a long
    private static final long FIBONACCI_MULTIPLIER = (long) (Math.pow(2, 64) / PHI);

    /**
     * Constructor for the custom hash table.
     *
     * @param tableSize   Size of the hash table (typically 120)
     * @param bucketSize  1 or 3 (used for output formatting and probing)
     * @param strategy    Collision strategy: "linear", "quadratic", or "chaining"
     * @param debug       Enable or disable debug logging
     */
    public CustomHashTable(int tableSize, int bucketSize, String strategy, boolean debug) {
        super(tableSize, bucketSize);           // Call base class constructor to initialize the table and metrics
        this.strategy = strategy.toLowerCase(); // Normalize strategy string to lowercase
        this.debug = debug;                     // Set debug flag
        this.metrics.setTableSize(tableSize);   // Pass table size to metrics to enable load factor calculation

        // Initialize node pool and linked list array if using chaining
        if (this.strategy.equals("chaining")) {
            if (debug) {
                System.out.println("[DEBUG] Initializing chaining support...");
            }
            initChainingSupport();
        }
    }

    /**
     * Initializes chaining support by allocating node pool and linked list slots.
     */
    private void initChainingSupport() {
        this.chainTable = new LinkedListChain[tableSize]; // Allocate linked list chain array
        this.nodePool = new Stack<>();                    // Create a stack to hold reusable node objects

        // Preallocate 2× the number of slots to cover worst-case chaining scenarios
        for (int i = 0; i < tableSize * 2; i++) {
            nodePool.push(ChainedNode.createEmptyNode()); // Fill the pool with empty nodes
        }

        // Create a LinkedListChain at every index
        for (int i = 0; i < tableSize; i++) {
            chainTable[i] = new LinkedListChain(nodePool);
        }

        if (debug) {
            System.out.printf("[DEBUG] Chaining pool created with %d nodes.%n", tableSize * 2);
        }
    }

    /**
     * Implements Fibonacci hashing using Knuth's multiplicative method.
     *
     * @param key The key to hash.
     * @return An index in the range [0, tableSize).
     */
    @Override
    protected int hash(int key) {
        // Convert to positive (if needed)
        long k = Math.abs((long) key);

        // Multiply using Fibonacci constant and shift to get the top bits
        long hashValue = (FIBONACCI_MULTIPLIER * k);

        // Use unsigned right shift to keep only the top bits, then mod table size
        int index = (int) ((hashValue >>> (64 - Integer.numberOfTrailingZeros(tableSize))) % tableSize);

        if (debug) {
            System.out.printf("[DEBUG] Hashed key %d → index %d using Fibonacci hashing%n", key, index);
        }

        return index;
    }

    /**
     * Inserts a key into the hash table using the configured strategy.
     *
     * @param key The integer key to insert.
     */
    @Override
    public void insert(int key) {
        int index = hash(key); // Determine the home index using Fibonacci hashing

        if (debug) {
            System.out.printf("[DEBUG] Inserting key %d using strategy: %s%n", key, strategy);
        }

        // Dispatch to the appropriate probing or chaining strategy
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
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }

    /**
     * Returns either the Integer[] table (probing) or LinkedListChain[] (chaining).
     * This is used by the OutputFormatter for visualization.
     */
    @Override
    public Object[] getRawTable() {
        if (strategy.equals("chaining")) {
            return chainTable;
        } else {
            return table;
        }
    }

    /**
     * Clears the hash table and resets performance metrics.
     * - For chaining, all nodes are returned to the pool.
     */
    @Override
    public void clearTable() {
        super.clearTable(); // Reset base table and metrics

        if (strategy.equals("chaining")) {
            for (LinkedListChain chain : chainTable) {
                chain.clear(); // Return nodes to pool
            }

            if (debug) {
                System.out.println("[DEBUG] Chained lists cleared and nodes returned to pool.");
            }
        }
    }

    /**
     * Prints the table to the console.
     * For chaining, prints each linked list at the corresponding index.
     */
    @Override
    public void printTable() {
        if (strategy.equals("chaining")) {
            for (int i = 0; i < tableSize; i++) {
                System.out.printf("%2d: ", i);
                chainTable[i].printChain();
            }
        } else {
            super.printTable();
        }
    }

    /**
     * Returns a specific chain (for testing).
     *
     * @param index Index in the table.
     * @return The LinkedListChain at the index.
     * @throws IllegalStateException if not using chaining.
     */
    public LinkedListChain getChainAt(int index) {
        if (!strategy.equals("chaining")) {
            throw new IllegalStateException("Chaining is not enabled.");
        }
        return chainTable[index];
    }
}
