package edu.jhu.algos.utils;

/**
 * A utility class to measure performance data for hashing operations,
 * including execution time, memory usage, and key operation counts.
 */
public class PerformanceMetrics {
    // Variables for tracking execution time
    private long startTime;
    private long endTime;

    // Variables for tracking memory usage
    private long memoryBefore;
    private long memoryAfter;

    // Variables for tracking hash table operations
    private long totalComparisons;
    private long totalCollisions;
    private long totalProbes;
    private long totalInsertions;

    /**
     * Default constructor initializes all fields to zero.
     */
    public PerformanceMetrics() {
        resetAll();
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
        this.totalProbes = 0;
        this.totalInsertions = 0;
    }

    /**
     * Increments the comparison counter.
     */
    public void addComparison() {
        this.totalComparisons++;
    }

    /**
     * Increments the collision counter.
     */
    public void addCollision() {
        this.totalCollisions++;
    }

    /**
     * Increments the probe counter.
     */
    public void addProbe() {
        this.totalProbes++;
    }

    /**
     * Increments the insertion counter.
     */
    public void addInsertion() {
        this.totalInsertions++;
    }

    /**
     * Getters for retrieving performance data.
     */
    public long getTotalComparisons() { return totalComparisons; }
    public long getTotalCollisions() { return totalCollisions; }
    public long getTotalProbes() { return totalProbes; }
    public long getTotalInsertions() { return totalInsertions; }
}
