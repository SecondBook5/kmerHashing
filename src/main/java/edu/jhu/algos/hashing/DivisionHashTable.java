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

    private final int modValue;         // Divisor used in division hashing
    private final String strategy;      // Strategy: "linear", "quadratic", or "chaining"
    private final double c1;            // Coefficient for linear term in quadratic probing
    private final double c2;            // Coefficient for quadratic term in quadratic probing

    private LinkedListChain[] chainTable;  // Used only if strategy is "chaining"

    /**
     * Constructor for DivisionHashTable with default quadratic constants.
     *
     * @param tableSize   Total number of slots in the table (typically 120).
     * @param bucketSize  Size of each bucket (1 or 3).
     * @param modValue    The divisor for the division hashing function.
     * @param strategy    Collision resolution strategy: "linear", "quadratic", or "chaining".
     * @param debug       Whether to enable debug output.
     */
    public DivisionHashTable(int tableSize, int bucketSize, int modValue, String strategy, boolean debug) {
        this(tableSize, bucketSize, modValue, strategy, debug, 0.5, 0.5);  // Default to 0.5
    }

    /**
     * Overloaded constructor for DivisionHashTable with customizable c1 and c2.
     *
     * @param tableSize   Total number of slots in the table.
     * @param bucketSize  Size of each bucket (1 or 3).
     * @param modValue    Modulo divisor.
     * @param strategy    Probing strategy: linear, quadratic, or chaining.
     * @param debug       Enable verbose output.
     * @param c1          c1 coefficient for quadratic probing.
     * @param c2          c2 coefficient for quadratic probing.
     */
    public DivisionHashTable(int tableSize, int bucketSize, int modValue, String strategy, boolean debug, double c1, double c2) {
        super(tableSize, bucketSize, debug);    // Call superclass constructor with debug mode
        this.modValue = modValue;               // Store modulo value for hashing
        this.strategy = strategy.toLowerCase(); // Normalize strategy for comparison
        this.c1 = c1;
        this.c2 = c2;

        this.metrics.setTableSize(tableSize);   // Enable load factor tracking

        // If chaining strategy is chosen, initialize linked lists and node pool
        if (this.strategy.equals("chaining")) {
            if (debugMode) {
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
        // Shared node pool for memory-efficient chaining
        Stack<ChainedNode> nodePool = new Stack<>();

        // Preallocate 2× the number of table slots for chaining nodes
        for (int i = 0; i < tableSize * 2; i++) {
            nodePool.push(ChainedNode.createEmptyNode());
        }

        // Initialize one LinkedListChain per table index
        for (int i = 0; i < tableSize; i++) {
            chainTable[i] = new LinkedListChain(nodePool, debugMode);
        }

        if (debugMode) {
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
        int hashed = Math.abs(key) % modValue;
        int index = hashed % tableSize;

        if (debugMode) {
            System.out.printf("[DEBUG] Hashed key %d → %d (mod %d) → final index %d (mod tableSize %d)%n",
                    key, hashed, modValue, index, tableSize);
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

        if (debugMode) {
            System.out.printf("[DEBUG] Inserting key %d using strategy: %s%n", key, strategy);

            if (strategy.equals("quadratic")) {
                System.out.printf("[DEBUG] Quadratic probing parameters: c1 = %.2f, c2 = %.2f%n", c1, c2);
            }
        }

        // Route to the appropriate collision handling method
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
                throw new IllegalArgumentException("Unknown probing strategy: " + strategy);
        }
    }

    /**
     * Searches for a key in the hash table using the configured strategy.
     *
     * @param key The integer key to locate.
     * @return True if the key is found; false otherwise.
     */
    @Override
    public boolean search(int key) {
        int index = hash(key);  // Compute the initial index using division hashing

        return switch (strategy) {
            case "linear" -> {
                for (int i = 0; i < tableSize; i++) {
                    int probeIndex = (index + i) % tableSize;
                    metrics.addComparison();
                    if (table[probeIndex] == null) yield false;
                    if (table[probeIndex].equals(key)) yield true;
                }
                yield false;
            }
            case "quadratic" -> {
                for (int i = 0; i < tableSize; i++) {
                    int probeIndex = (int) ((index + c1 * i + c2 * i * i) % tableSize);
                    if (probeIndex < 0) probeIndex = Math.floorMod(probeIndex, tableSize);
                    metrics.addComparison();
                    if (table[probeIndex] == null) yield false;
                    if (table[probeIndex].equals(key)) yield true;
                }
                yield false;
            }
            case "chaining" -> {
                metrics.addComparison();
                yield chainTable[index].search(key);
            }
            default -> throw new IllegalStateException("Unknown strategy for search: " + strategy);
        };
    }

    @Override
    public void printTable() {
        if (debugMode) {
            System.out.println("[DEBUG] Printing table for strategy: " + strategy);
        }

        if (strategy.equals("chaining")) {
            for (int i = 0; i < tableSize; i++) {
                System.out.printf("%2d ", i);
                chainTable[i].printChain();
            }
        } else {
            super.printTable();
        }
    }

    @Override
    public void clearTable() {
        if (debugMode) {
            System.out.println("[DEBUG] Clearing hash table and resetting metrics...");
        }

        super.clearTable();

        if (strategy.equals("chaining")) {
            for (int i = 0; i < tableSize; i++) {
                chainTable[i].clear();
            }

            if (debugMode) {
                System.out.println("[DEBUG] Chained lists cleared and nodes returned to pool.");
            }
        }
    }

    public LinkedListChain getChainAt(int index) {
        if (!strategy.equals("chaining")) {
            throw new IllegalStateException("Chaining is not enabled for this hash table.");
        }
        return chainTable[index];
    }

    @Override
    public Object[] getRawTable() {
        return strategy.equals("chaining") ? chainTable : table;
    }
}
