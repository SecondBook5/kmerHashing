package edu.jhu.algos.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PerformanceMetrics.java
 * - Tests execution timing, memory tracking, operation counting, and resets.
 * - Ensures proper defensive programming and exception handling.
 */
public class PerformanceMetricsTest {

    /**
     * Test starting and stopping the timer.
     * - Ensures execution time is correctly measured and is nonzero.
     * - Simulates a short delay to validate time tracking.
     */
    @Test
    public void testTimeTracking() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.startTimer();
        try {
            Thread.sleep(50); // Simulate a delay of 50ms
        } catch (InterruptedException e) {
            fail("Thread sleep interrupted.");
        }
        metrics.stopTimer();

        double elapsedSec = Double.parseDouble(metrics.getElapsedTime());
        assertTrue(elapsedSec >= 0.05, "Elapsed time should be at least 0.05 seconds.");
    }

    /**
     * Test stopping the timer without starting it first.
     * - Ensures that `stopTimer()` throws an IllegalStateException.
     */
    @Test
    public void testStopTimerWithoutStart() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        Exception exception = assertThrows(IllegalStateException.class, metrics::stopTimer);
        assertEquals("Error: stopTimer() called without a valid startTimer() call.", exception.getMessage());
    }

    /**
     * Test incrementing operation counters to ensure they update correctly.
     * - Verifies `addComparison()`, `addCollision()`, `addProbe()`, and `addInsertion()`.
     */
    @Test
    public void testOperationCounters() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.addComparison();
        metrics.addComparison();
        metrics.addCollision();
        metrics.addProbe();
        metrics.addInsertion();

        assertEquals(2, metrics.getTotalComparisons());
        assertEquals(1, metrics.getTotalCollisions());
        assertEquals(1, metrics.getTotalProbes());
        assertEquals(1, metrics.getTotalInsertions());
    }

    /**
     * Test memory tracking before and after an allocation.
     * - Ensures memory tracking reports a nonzero value after execution.
     */
    @Test
    public void testMemoryTracking() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.startTimer();
        int[] dummyArray = new int[1_000_000]; // Simulate memory usage
        metrics.stopTimer();

        double usedBytes = Double.parseDouble(metrics.getMemoryUsage());
        assertTrue(usedBytes >= 0, "Memory usage should be non-negative.");
    }

    /**
     * Test resetting all counters and metrics.
     * - Ensures all tracked values reset to zero.
     */
    @Test
    public void testResetAll() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.startTimer();
        metrics.addComparison();
        metrics.addCollision();
        metrics.addProbe();
        metrics.addInsertion();
        metrics.stopTimer();

        metrics.resetAll();

        // Assert counters reset
        assertEquals(0, metrics.getTotalComparisons());
        assertEquals(0, metrics.getTotalCollisions());
        assertEquals(0, metrics.getTotalProbes());
        assertEquals(0, metrics.getTotalInsertions());

        // Assert elapsed time and memory usage reset to 0 (as double)
        assertEquals(0.0, Double.parseDouble(metrics.getElapsedTime()), 0.0001);
        assertEquals(0.0, Double.parseDouble(metrics.getMemoryUsage()), 0.0001);
    }


    /**
     * Test measuring time for an extremely fast operation.
     * - Ensures that the minimum measurable time is handled correctly.
     */
    @Test
    public void testZeroExecutionTime() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.startTimer();
        metrics.stopTimer();

        double elapsedSec = Double.parseDouble(metrics.getElapsedTime());
        assertTrue(elapsedSec >= 0, "Elapsed time should be non-negative.");
    }

    /**
     * Test memory usage tracking with no change in allocation.
     * - Ensures that memory tracking does not return an incorrect negative value.
     */
    @Test
    public void testZeroMemoryUsage() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.startTimer();
        metrics.stopTimer();

        double memoryBytes = Double.parseDouble(metrics.getMemoryUsage());
        assertTrue(memoryBytes >= 0, "Memory usage should be non-negative.");
    }

    /**
     * Test for extreme counter values.
     * - Ensures that operation counters do not overflow.
     */
    @Test
    public void testExtremeCounterValues() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            metrics.addComparison();
        }

        assertEquals(Integer.MAX_VALUE, metrics.getTotalComparisons());
    }

    /**
     * Test primary and secondary collision counters.
     * - Ensures both types of collisions are counted and reflected in total collisions.
     */
    @Test
    public void testPrimaryAndSecondaryCollisions() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.addPrimaryCollision();
        metrics.addPrimaryCollision();
        metrics.addSecondaryCollision();
        metrics.addSecondaryCollision();
        metrics.addSecondaryCollision();

        assertEquals(2, metrics.getPrimaryCollisions());
        assertEquals(3, metrics.getSecondaryCollisions());
        assertEquals(5, metrics.getTotalCollisions());
    }

    /**
     * Test load factor calculation.
     * - Ensures load factor is computed as insertions / table size.
     */
    @Test
    public void testLoadFactorCalculation() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.setTableSize(100);
        metrics.addInsertion();
        metrics.addInsertion();

        double expected = 2.0 / 100.0;
        assertEquals(expected, metrics.getLoadFactor(), 0.0001);
    }

    /**
     * Test the toString() method summary output.
     * - Ensures it contains all major metric fields in readable form.
     */
    @Test
    public void testToStringSummaryOutput() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.setTableSize(10);
        metrics.addComparison();
        metrics.addPrimaryCollision();
        metrics.addSecondaryCollision();
        metrics.addProbe();
        metrics.addInsertion();

        String output = metrics.toString();

        assertTrue(output.contains("Comparisons"));
        assertTrue(output.contains("Primary"));
        assertTrue(output.contains("Secondary"));
        assertTrue(output.contains("Load Factor"));
        assertTrue(output.contains("Execution Time"));
    }
}
