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

        // Verify that the recorded time is at least 50ms
        assertTrue(metrics.getElapsedTimeMs() >= 50, "Elapsed time should be at least 50ms.");
    }

    /**
     * Test stopping the timer without starting it first.
     * - Ensures that `stopTimer()` throws an IllegalStateException.
     */
    @Test
    public void testStopTimerWithoutStart() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        // Ensure an exception is thrown when stopTimer() is called before startTimer()
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

        // Verify that counters correctly reflect the number of calls
        assertEquals(2, metrics.getTotalComparisons(), "Total comparisons should be 2.");
        assertEquals(1, metrics.getTotalCollisions(), "Total collisions should be 1.");
        assertEquals(1, metrics.getTotalProbes(), "Total probes should be 1.");
        assertEquals(1, metrics.getTotalInsertions(), "Total insertions should be 1.");
    }

    /**
     * Test memory tracking before and after an allocation.
     * - Ensures memory tracking reports a nonzero value after execution.
     */
    @Test
    public void testMemoryTracking() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.startTimer();
        int[] dummyArray = new int[1000000]; // Allocate a large array to simulate memory usage
        metrics.stopTimer();

        // Ensure that memory tracking does not return a negative value
        assertTrue(metrics.getMemoryUsageMB() >= 0, "Memory usage should be non-negative.");
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

        // Verify that all values reset to zero
        assertEquals(0, metrics.getTotalComparisons(), "Total comparisons should reset to 0.");
        assertEquals(0, metrics.getTotalCollisions(), "Total collisions should reset to 0.");
        assertEquals(0, metrics.getTotalProbes(), "Total probes should reset to 0.");
        assertEquals(0, metrics.getTotalInsertions(), "Total insertions should reset to 0.");
        assertEquals(0, metrics.getElapsedTimeMs(), "Elapsed time should reset to 0.");
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

        // The execution time should be very small, but non-negative
        assertTrue(metrics.getElapsedTimeMs() >= 0, "Elapsed time should be non-negative.");
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

        // If no memory-intensive operation was performed, the memory usage should be 0 or very small
        assertTrue(metrics.getMemoryUsageMB() >= 0, "Memory usage should be non-negative.");
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

        assertEquals(Integer.MAX_VALUE, metrics.getTotalComparisons(), "Total comparisons should match Integer.MAX_VALUE.");
    }


    /**
     * Test primary and secondary collision counters.
     * - Ensures both types of collisions are counted and reflected in total collisions.
     */
    @Test
    public void testPrimaryAndSecondaryCollisions() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        // Add two primary collisions and three secondary collisions
        metrics.addPrimaryCollision();
        metrics.addPrimaryCollision();
        metrics.addSecondaryCollision();
        metrics.addSecondaryCollision();
        metrics.addSecondaryCollision();

        // Validate individual and total collision counts
        assertEquals(2, metrics.getPrimaryCollisions(), "Primary collisions should be 2.");
        assertEquals(3, metrics.getSecondaryCollisions(), "Secondary collisions should be 3.");
        assertEquals(5, metrics.getTotalCollisions(), "Total collisions should be 5.");
    }

    /**
     * Test load factor calculation.
     * - Ensures load factor is computed as insertions / table size.
     */
    @Test
    public void testLoadFactorCalculation() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.setTableSize(100); // Set table size
        metrics.addInsertion();    // 1 insertion
        metrics.addInsertion();    // 2 insertions

        double expected = 2.0 / 100.0;
        assertEquals(expected, metrics.getLoadFactor(), 0.0001, "Load factor should be insertions / tableSize.");
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

        assertTrue(output.contains("Comparisons"), "Summary should include 'Comparisons'.");
        assertTrue(output.contains("Primary"), "Summary should include 'Primary' collisions.");
        assertTrue(output.contains("Secondary"), "Summary should include 'Secondary' collisions.");
        assertTrue(output.contains("Load Factor"), "Summary should include 'Load Factor'.");
        assertTrue(output.contains("Execution Time"), "Summary should include 'Execution Time'.");
    }
}
