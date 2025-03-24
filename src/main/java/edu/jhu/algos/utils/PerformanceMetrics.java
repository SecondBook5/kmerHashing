package edu.jhu.algos.utils;

/**
 * A utility class to measure performance data for hashing operations,
 * including execution time, memory usage, and key operation counts.
 * <p>
 * ▶ PRIMARY vs. SECONDARY COLLISIONS (important distinction for probing strategies):
 * - A **primary collision** occurs when the first index computed by the hash function
 *   (i.e., the "home slot") is already occupied.
 * - A **secondary collision** occurs during subsequent probes (i > 0) when a new slot is checked
 *   and is also found to be occupied.
 * This distinction helps us analyze whether a hash function (primary collision) or a probing
 * strategy (secondary collisions) is contributing more to table inefficiency.
 */
public class PerformanceMetrics {
    // Variables for tracking execution time
    private long startTime;
    private long endTime;

    // Variables for tracking memory usage
    private long memoryBefore;
    private long memoryAfter;

    // Variables for tracking hash table operations
    private long totalComparisons;       // Total number of key comparisons
    private long totalCollisions;        // Total number of collisions (primary + secondary)
    private long primaryCollisions;      // Primary collisions: first attempt fails (home slot occupied)
    private long secondaryCollisions;    // Secondary collisions: subsequent probe fails
    private long totalProbes;            // Total number of probing steps (includes successful insert probes)
    private long totalInsertions;        // Total number of keys successfully inserted

    private int tableSize = -1;          // Optional: used to compute load factor

    /**
     * Default constructor initializes all fields to zero.
     */
    public PerformanceMetrics() {
        resetAll();
    }

    /**
     * Sets the table size for computing load factor.
     * This should be set externally by the hash table object.
     *
     * @param tableSize The number of addressable slots in the hash table.
     */
    public void setTableSize(int tableSize) {
        this.tableSize = tableSize;
    }

    /**
     * Starts the performance timer and records the current memory usage.
     * Must be called before an operation to track its execution time.
     */
    public void startTimer() {
        this.startTime = System.nanoTime();
        this.memoryBefore = getUsedMemory();
    }

    /**
     * Stops the performance timer and records the memory usage after execution.
     * Throws an exception if `startTimer()` wasn't called first.
     */
    public void stopTimer() {
        if (startTime == 0) {
            throw new IllegalStateException("Error: stopTimer() called without a valid startTimer() call.");
        }
        this.endTime = System.nanoTime();
        this.memoryAfter = getUsedMemory();
    }

    /**
     * Retrieves the total elapsed execution time in milliseconds.
     *
     * @return The elapsed time in milliseconds, or 0 if stopTimer() hasn't been called.
     */
    public long getElapsedTimeMs() {
        if (startTime == 0 || endTime == 0) {
            System.err.println("Warning: getElapsedTimeMs() called before stopTimer(). Returning 0.");
            return 0;
        }
        return (endTime - startTime) / 1_000_000; // Convert nanoseconds to milliseconds
    }

    /**
     * Retrieves the memory usage difference before and after execution, in megabytes.
     *
     * @return The memory usage difference (MB).
     */
    public long getMemoryUsageMB() {
        return (memoryAfter - memoryBefore) / (1024 * 1024); // Convert bytes to MB
    }

    /**
     * Retrieves the current heap memory usage in bytes.
     *
     * @return The used memory in bytes.
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory(); // Calculate used heap memory
    }

    /**
     * Resets all recorded performance data to zero.
     */
    public void resetAll() {
        this.startTime = 0;
        this.endTime = 0;
        this.memoryBefore = 0;
        this.memoryAfter = 0;
        this.totalComparisons = 0;
        this.totalCollisions = 0;
        this.primaryCollisions = 0;
        this.secondaryCollisions = 0;
        this.totalProbes = 0;
        this.totalInsertions = 0;
    }

    /**
     * Increments the comparison counter for key comparisons.
     */
    public void addComparison() {
        this.totalComparisons++;
    }

    /**
     * Increments the general collision counter (for cases where distinction is not made).
     */
    public void addCollision() {
        this.totalCollisions++;
    }

    /**
     * Increments the primary collision counter.
     * A primary collision occurs when the home slot is already occupied (i == 0).
     */
    public void addPrimaryCollision() {
        this.primaryCollisions++;
        this.totalCollisions++;
    }

    /**
     * Increments the secondary collision counter.
     * A secondary collision occurs during additional probes (i > 0).
     */
    public void addSecondaryCollision() {
        this.secondaryCollisions++;
        this.totalCollisions++;
    }

    /**
     * Increments the probe counter for probing steps.
     */
    public void addProbe() {
        this.totalProbes++;
    }

    /**
     * Increments the insertion counter for successfully stored keys.
     */
    public void addInsertion() {
        this.totalInsertions++;
    }

    /**
     * Computes the load factor = (number of insertions / table size).
     * Returns -1 if table size is not set.
     *
     * @return Load factor as a decimal, or -1 if undefined.
     */
    public double getLoadFactor() {
        if (tableSize <= 0) return -1;
        return (double) totalInsertions / tableSize;
    }

    /**
     * Getters for retrieving performance data.
     */
    public long getTotalComparisons() { return totalComparisons; }
    public long getTotalCollisions() { return totalCollisions; }
    public long getPrimaryCollisions() { return primaryCollisions; }
    public long getSecondaryCollisions() { return secondaryCollisions; }
    public long getTotalProbes() { return totalProbes; }
    public long getTotalInsertions() { return totalInsertions; }

    /**
     * Returns a formatted string summarizing key performance metrics.
     *
     * @return Summary of performance statistics.
     */
    @Override
    public String toString() {
        return String.format(
                "Comparisons: %d\n" +
                        "Collisions: %d (Primary: %d, Secondary: %d)\n" +
                        "Probes: %d\n" +
                        "Insertions: %d\n" +
                        "Load Factor: %.2f\n" +
                        "Execution Time: %d ms\n" +
                        "Memory Usage: %d MB",
                totalComparisons,
                totalCollisions,
                primaryCollisions,
                secondaryCollisions,
                totalProbes,
                totalInsertions,
                getLoadFactor(),
                getElapsedTimeMs(),
                getMemoryUsageMB()
        );
    }
}
