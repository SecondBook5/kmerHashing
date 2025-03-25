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
    private final double c1;           // Quadratic probing constant (linear term)
    private final double c2;           // Quadratic probing constant (quadratic term)

    private LinkedListChain[] chainTable; // Used only if chaining strategy is enabled

    // Compute golden ratio φ = (1 + √5) / 2 as a double
    private static final double PHI = (1 + Math.sqrt(5)) / 2;

    // Compute the multiplicative constant for Fibonacci hashing: ⌊2^64 / φ⌋
    // This value is precomputed and must be less than Long.MAX_VALUE
    // Fibonacci multiplier constant = 2^64 / φ, interpreted as unsigned (0x9E3779B97F4A7C15)
    private static final long FIBONACCI_MULTIPLIER = 0x9E3779B97F4A7C15L;

    /**
     * Constructor for the custom hash table.
     *
     * @param tableSize   Size of the hash table (typically 120)
     * @param bucketSize  1 or 3 (used for output formatting and probing)
     * @param strategy    Collision strategy: "linear", "quadratic", or "chaining"
     * @param debug       Enable or disable debug logging
     */
    public CustomHashTable(int tableSize, int bucketSize, String strategy, boolean debug) {
        this(tableSize, bucketSize, strategy, debug, 0.5, 0.5);  // default c1, c2
    }

    /**
     * Extended constructor to allow custom c1 and c2 for quadratic probing.
     *
     * @param tableSize   Size of the hash table
     * @param bucketSize  1 or 3 (formatting only)
     * @param strategy    "linear", "quadratic", or "chaining"
     * @param debug       Enable debug mode
     * @param c1          Linear coefficient for quadratic probing
     * @param c2          Quadratic coefficient for quadratic probing
     */
    public CustomHashTable(int tableSize, int bucketSize, String strategy, boolean debug, double c1, double c2) {
        super(tableSize, bucketSize, debug);       // Call base class constructor with debugMode
        this.strategy = strategy.toLowerCase();    // Normalize strategy string to lowercase
        this.c1 = c1;
        this.c2 = c2;
        this.metrics.setTableSize(tableSize);   // Pass table size to metrics to enable load factor calculation

        // Initialize node pool and linked list array if using chaining
        if (this.strategy.equals("chaining")) {
            if (debugMode) {
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
        // Shared pool of reusable nodes for chaining
        Stack<ChainedNode> nodePool = new Stack<>();                    // Create a stack to hold reusable node objects

        // Preallocate 2× the number of slots to cover worst-case chaining scenarios
        for (int i = 0; i < tableSize * 2; i++) {
            nodePool.push(ChainedNode.createEmptyNode()); // Fill the pool with empty nodes
        }

        // Create a LinkedListChain at every index
        for (int i = 0; i < tableSize; i++) {
            chainTable[i] = new LinkedListChain(nodePool, debugMode); // Initialize each chain with the shared node pool
        }

        if (debugMode) {
            System.out.printf("[DEBUG] Chaining pool created with %d nodes.%n", tableSize * 2);
        }
    }

    /**
     * Implements Fibonacci hashing using Knuth's multiplicative method.
     * Maps a key into the hash table using multiplication and modulo.
     *
     * @param key The key to hash.
     * @return An index in the range [0, tableSize).
     */
    @Override
    protected int hash(int key) {
        // Ensure non-negative key by converting to absolute long
        long k = Math.abs((long) key);

        // Perform Fibonacci multiplication
        long hashValue = k * FIBONACCI_MULTIPLIER;

        // Use modulo with table size (not a power of 2)
        int index = (int) (hashValue % tableSize);

        // Defensive clamp to ensure index is always within bounds
        if (index < 0 || index >= tableSize) {
            index = Math.floorMod(index, tableSize);
        }

        if (debugMode) {
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

        if (debugMode) {
            System.out.printf("[DEBUG] Inserting key %d using strategy: %s%n", key, strategy);
        }

        // Dispatch to the appropriate probing or chaining strategy
        switch (strategy) {
            case "linear":
                ProbingStrategy.insertWithProbing(
                        table, key, index, tableSize, false, metrics, 0.5, 0.5, debugMode);
                break;

            case "quadratic":
                ProbingStrategy.insertWithProbing(
                        table, key, index, tableSize, true, metrics, c1, c2, debugMode);
                break;

            case "chaining":
                ProbingStrategy.insertWithChaining(chainTable, key, index, metrics, debugMode);
                break;

            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }

    /**
     * Searches for a key in the hash table.
     *
     * @param key The key to locate in the hash table.
     * @return True if the key is found; false otherwise.
     */
    @Override
    public boolean search(int key) {
        int index = hash(key);  // Compute the initial index using Fibonacci hashing

        return switch (strategy) {
            case "linear" -> {
                // Perform linear probing until key is found or slot is null
                for (int i = 0; i < tableSize; i++) {
                    int probeIndex = (index + i) % tableSize;

                    metrics.addComparison(); // Track each access
                    if (table[probeIndex] == null) yield false;
                    if (table[probeIndex].equals(key)) yield true;
                }
                yield false;
            }
            case "quadratic" -> {
                for (int i = 0; i < tableSize; i++) {
                    int probeIndex = (int) ((index + c1 * i + c2 * i * i) % tableSize);

                    metrics.addComparison(); // Track lookup
                    if (probeIndex < 0) probeIndex = Math.floorMod(probeIndex, tableSize);

                    if (table[probeIndex] == null) yield false;
                    if (table[probeIndex].equals(key)) yield true;
                }
                yield false;
            }
            case "chaining" -> {
                // Use linked list search at computed index
                LinkedListChain chain = chainTable[index];
                yield chain.search(key, metrics);
            }
            default -> throw new IllegalStateException("Unsupported strategy: " + strategy);
        };
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

            if (debugMode) {
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
