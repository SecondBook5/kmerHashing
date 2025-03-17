package edu.jhu.algos.utils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * A utility class to measure performance data for hashing operations,
 * including execution time, memory usage, and key operation counts.
 * <p>
 * This class is used to track the efficiency of various hashing techniques,
 * allowing analysis of time complexity, space usage, and operational overhead.
 * </p>
 */
public class PerformanceMetrics {
    // Variables for tracking execution time
    private long startTime;  // Stores the start time (in nanoseconds)
    private long endTime;    // Stores the end time (in nanoseconds)

    // Variables for tracking memory usage
    private long memoryBefore;  // Memory used before execution (in bytes)
    private long memoryAfter;   // Memory used after execution (in bytes)

    // Variables for tracking hash table operations
    private long totalComparisons;  // Tracks the number of key comparisons
    private long totalCollisions;   // Tracks the number of collisions encountered
    private long totalProbes;       // Tracks the number of probing attempts
    private long totalInsertions;   // Tracks the number of successful insertions

    /**
     * Default constructor initializes all fields to zero.
     */
    public PerformanceMetrics() {
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
     * Starts the performance timer and records the current memory usage.
     * <p>
     * This method should be called before the execution of a hashing operation.
     * </p>
     */
    public void startTimer() {
        this.startTime = System.nanoTime();  // Capture the current system time
        this.memoryBefore = getUsedMemory(); // Capture memory usage before execution
    }

    /**
     * Stops the performance timer and records the memory usage after execution.
     * <p>
     * This method should be called immediately after the operation completes.
     * If startTimer() was not called first, this method will throw an exception.
     * </p>
     *
     * @throws IllegalStateException if stopTimer() is called without a valid startTimer() invocation.
     */
    public void stopTimer() {
        if (startTime == 0) {
            throw new IllegalStateException("Error: stopTimer() called without a valid startTimer() call.");
        }
        this.endTime = System.nanoTime();  // Capture the system time after execution
        this.memoryAfter = getUsedMemory(); // Capture memory usage after execution
    }

    /**
     * Records a key comparison operation.
     * <p>
     * This should be called whenever a key comparison occurs in hashing operations.
     * </p>
     */
    public void addComparison() {
        this.totalComparisons++;
    }

    /**
     * Records a collision event.
     * <p>
     * This should be called whenever a key collision occurs during insertion.
     * </p>
     */
    public void addCollision() {
        this.totalCollisions++;
    }

    /**
     * Records a probe event.
     * <p>
     * This should be called whenever probing (linear/quadratic) occurs during insertion or search.
     * </p>
     */
    public void addProbe() {
        this.totalProbes++;
    }

    /**
     * Records a successful insertion event.
     * <p>
     * This should be called whenever a key is successfully inserted into the hash table.
     * </p>
     */
    public void addInsertion() {
        this.totalInsertions++;
    }

    /**
     * Retrieves the total elapsed execution time in milliseconds.
     *
     * @return The elapsed time in milliseconds, or 0 if stopTimer() hasn't been called.
     */
    public long getElapsedTimeMs() {
        if (this.startTime == 0 || this.endTime == 0) {
            return 0; // If timing data is incomplete, return 0
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
     * Exports performance data to a CSV file for analysis.
     * <p>
     * The CSV file will contain columns: Execution Time (ms), Memory Usage (MB),
     * Total Comparisons, Total Collisions, Total Probes, Total Insertions.
     * </p>
     *
     * @param filename The output file name (e.g., "performance_results.csv").
     */
    public void exportToCSV(String filename) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.append(getElapsedTimeMs() + "," + getMemoryUsageMB() + "," +
                    totalComparisons + "," + totalCollisions + "," +
                    totalProbes + "," + totalInsertions + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints all performance metrics to the console.
     */
    public void printMetrics() {
        System.out.println("Execution Time (ms): " + getElapsedTimeMs());
        System.out.println("Memory Usage (MB): " + getMemoryUsageMB());
        System.out.println("Total Comparisons: " + totalComparisons);
        System.out.println("Total Collisions: " + totalCollisions);
        System.out.println("Total Probes: " + totalProbes);
        System.out.println("Total Insertions: " + totalInsertions);
    }

    /**
     * Resets all recorded performance data to zero.
     * <p>
     * This method is useful if the same PerformanceMetrics instance
     * is being reused across multiple hashing operations.
     * </p>
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
}
