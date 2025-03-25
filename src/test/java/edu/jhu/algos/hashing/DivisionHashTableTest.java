package edu.jhu.algos.hashing;

import edu.jhu.algos.utils.PerformanceMetrics;
import org.junit.jupiter.api.Test;
import edu.jhu.algos.data_structures.LinkedListChain;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DivisionHashTable using all supported strategies.
 * Tests correctness, metrics tracking, chaining behavior, and corner cases.
 */
public class DivisionHashTableTest {

    private static final boolean DEBUG = true;

    /**
     * Test linear probing inserts correctly and updates performance metrics.
     */
    @Test
    public void testLinearProbingInsert() {
        DivisionHashTable table = new DivisionHashTable(10, 1, 10, "linear", DEBUG);

        table.insert(2);   // hashes to index 2
        table.insert(12);  // hashes to 2 again → probes to 3

        assertEquals(2, table.table[2]);
        assertEquals(12, table.table[3]);

        PerformanceMetrics metrics = table.metrics;
        assertEquals(2, metrics.getTotalInsertions());
        assertEquals(3, metrics.getTotalComparisons()); // both checked a slot
        assertEquals(1, metrics.getTotalCollisions());  // 12 collided at 2
        assertEquals(1, metrics.getTotalProbes());      // probed to 3
    }

    /**
     * Test quadratic probing inserts correctly and probes into a new slot.
     */
    @Test
    public void testQuadraticProbingInsert() {
        DivisionHashTable table = new DivisionHashTable(10, 1, 10, "quadratic", DEBUG);

        table.insert(5);   // hashes to index 5
        table.insert(15);  // hashes to 5 again → probes

        // Robust assertion: key 15 should exist somewhere in the table
        boolean found15 = false;
        for (Integer slot : table.table) {
            if (slot != null && slot == 15) {
                found15 = true;
                break;
            }
        }

        assertTrue(found15, "Key 15 should be present in the table after insertion.");

        // Probe metrics still matter
        PerformanceMetrics metrics = table.metrics;
        assertEquals(2, metrics.getTotalInsertions());
        assertTrue(metrics.getTotalComparisons() >= 2);
        assertEquals(1, metrics.getTotalCollisions());
        assertEquals(1, metrics.getTotalProbes());
    }


    /**
     * Test chaining inserts multiple values to same linked list chain.
     */
    @Test
    public void testChainingInsert() {
        DivisionHashTable table = new DivisionHashTable(10, 1, 10, "chaining", DEBUG);

        table.insert(7);   // index 7
        table.insert(17);  // also index 7

        assertTrue(table.getChainAt(7).search(7, table.metrics));
        assertTrue(table.getChainAt(7).search(17, table.metrics));
        assertEquals(2, table.getChainAt(7).size());

        PerformanceMetrics metrics = table.metrics;
        assertEquals(2, metrics.getTotalInsertions());
        assertEquals(0, metrics.getTotalProbes());
        assertEquals(1, metrics.getTotalCollisions());
    }

    /**
     * Test probing behavior when table is full and insertion fails.
     */
    @Test
    public void testLinearProbingFailsWhenFull() {
        DivisionHashTable table = new DivisionHashTable(3, 1, 3, "linear", DEBUG);

        table.insert(0);  // → 0
        table.insert(1);  // → 1
        table.insert(2);  // → 2
        table.insert(3);  // Should fail

        PerformanceMetrics metrics = table.metrics;
        assertEquals(3, metrics.getTotalInsertions()); // keys 0, 1, 2 inserted
        assertEquals(6, metrics.getTotalComparisons()); // 3 normal + 3 failed for key 3
        assertEquals(3, metrics.getTotalProbes()); // 3 probes for failed key 3
        assertEquals(3, metrics.getTotalCollisions()); // same: 3 failed probes are 3 collisions
    }

    /**
     * Test clearTable() resets all fields and metrics correctly.
     */
    @Test
    public void testClearTableResetsEverything() {
        DivisionHashTable table = new DivisionHashTable(10, 1, 10, "linear", DEBUG);

        table.insert(3);
        table.insert(13);
        assertEquals(2, table.metrics.getTotalInsertions());

        table.clearTable();

        for (Integer val : table.table) {
            assertNull(val);
        }

        assertEquals(0, table.metrics.getTotalInsertions());
        assertEquals(0, table.metrics.getTotalComparisons());
    }

    /**
     * Test chaining clearTable returns all nodes and resets chains.
     */
    @Test
    public void testChainingClearTableReleasesNodes() {
        DivisionHashTable table = new DivisionHashTable(5, 1, 5, "chaining", DEBUG);

        table.insert(1);
        table.insert(6);  // same index

        assertEquals(2, table.getChainAt(1).size());

        table.clearTable();

        assertEquals(0, table.getChainAt(1).size());
        assertTrue(table.getChainAt(1).isEmpty());
    }

    /**
     * Edge case: clean insert with no collisions or probes.
     */
    @Test
    public void testNoCollisionNoProbes() {
        DivisionHashTable table = new DivisionHashTable(10, 1, 10, "linear", DEBUG);

        table.insert(3); // clean insert

        PerformanceMetrics metrics = table.metrics;
        assertEquals(1, metrics.getTotalInsertions());
        assertEquals(1, metrics.getTotalComparisons());
        assertEquals(0, metrics.getTotalCollisions());
        assertEquals(0, metrics.getTotalProbes());
    }

    /**
     * Edge case: make sure failed insert doesn't increase insert count.
     */
    @Test
    public void testInsertFailsMetricsStable() {
        DivisionHashTable table = new DivisionHashTable(2, 1, 2, "linear", DEBUG);

        table.insert(0);
        table.insert(1);
        table.insert(2); // should fail

        PerformanceMetrics metrics = table.metrics;
        assertEquals(2, metrics.getTotalInsertions());  // Only 2 succeeded
        assertTrue(metrics.getTotalComparisons() >= 2); // at least 2 comparisons
    }

    /**
     * Chaining should not affect probe/collision counts.
     */
    @Test
    public void testChainingMetricsOnlyTracksInsertions() {
        DivisionHashTable table = new DivisionHashTable(10, 1, 10, "chaining", DEBUG);

        table.insert(1);
        table.insert(11); // same index

        PerformanceMetrics metrics = table.metrics;
        assertEquals(2, metrics.getTotalInsertions());
        assertEquals(1, metrics.getTotalCollisions());
        assertEquals(0, metrics.getTotalProbes());
    }

    /**
     * Test that DivisionHashTable tracks primary and secondary collisions separately.
     */
    @Test
    public void testTracksPrimaryAndSecondaryCollisions() {
        DivisionHashTable table = new DivisionHashTable(5, 1, 5, "linear", DEBUG);

        // Insert 0 and 5 → both hash to index 0
        table.insert(0);  // clean
        table.insert(5);  // primary collision at 0, insert at 1
        table.insert(10); // primary collision at 0, secondary at 1, insert at 2

        PerformanceMetrics metrics = table.metrics;

        assertEquals(3, metrics.getTotalInsertions(), "Should insert 3 keys.");
        assertEquals(3, metrics.getTotalCollisions(), "Should count 2 primary + 1 secondary = 3 total collisions.");
        assertEquals(2, metrics.getPrimaryCollisions(), "Two primary collisions from keys 5 and 10.");
        assertEquals(1, metrics.getSecondaryCollisions(), "One secondary collision from key 10.");

    }

    /**
     * Test that the load factor is computed correctly.
     */
    @Test
    public void testLoadFactor() {
        DivisionHashTable table = new DivisionHashTable(10, 1, 10, "linear", DEBUG);

        table.insert(3);
        table.insert(13);
        table.insert(23);

        double expectedLoad = 3.0 / 10.0;
        double actualLoad = table.metrics.getLoadFactor();

        assertEquals(expectedLoad, actualLoad, 0.0001, "Load factor should equal insertions / table size.");
    }

    /**
     * Test that getRawTable() returns correct internal structure based on strategy.
     */
    @Test
    public void testGetRawTableReturnsCorrectStructure() {
        // Case 1: Linear Probing (should return Integer[])
        DivisionHashTable linearTable = new DivisionHashTable(10, 1, 10, "linear", DEBUG);
        Object[] rawLinear = linearTable.getRawTable();
        assertNotNull(rawLinear, "Raw table for linear probing should not be null.");
        assertInstanceOf(Integer[].class, rawLinear, "Linear strategy should return Integer[] from getRawTable().");

        // Case 2: Quadratic Probing (also returns Integer[])
        DivisionHashTable quadTable = new DivisionHashTable(10, 1, 10, "quadratic", DEBUG);
        Object[] rawQuadratic = quadTable.getRawTable();
        assertNotNull(rawQuadratic, "Raw table for quadratic probing should not be null.");
        assertInstanceOf(Integer[].class, rawQuadratic, "Quadratic strategy should return Integer[] from getRawTable().");

        // Case 3: Chaining (should return LinkedListChain[])
        DivisionHashTable chainTable = new DivisionHashTable(10, 1, 10, "chaining", DEBUG);
        Object[] rawChaining = chainTable.getRawTable();
        assertNotNull(rawChaining, "Raw table for chaining should not be null.");
        assertInstanceOf(LinkedListChain[].class, rawChaining, "Chaining strategy should return LinkedListChain[] from getRawTable().");
    }

    /**
     * Test that search() correctly finds inserted keys using all strategies.
     */
    @Test
    public void testSearchFunctionalityAcrossStrategies() {
        // Linear Probing
        DivisionHashTable linear = new DivisionHashTable(10, 1, 10, "linear", DEBUG);
        linear.insert(3);
        linear.insert(13);
        assertTrue(linear.search(3), "Should find key 3 in linear probing.");
        assertTrue(linear.search(13), "Should find key 13 in linear probing.");
        assertFalse(linear.search(99), "Should not find key 99 in linear probing.");

        // Quadratic Probing
        DivisionHashTable quadratic = new DivisionHashTable(10, 1, 10, "quadratic", DEBUG);
        quadratic.insert(4);
        quadratic.insert(14);
        assertTrue(quadratic.search(4), "Should find key 4 in quadratic probing.");
        assertTrue(quadratic.search(14), "Should find key 14 in quadratic probing.");
        assertFalse(quadratic.search(100), "Should not find key 100 in quadratic probing.");

        // Chaining
        DivisionHashTable chaining = new DivisionHashTable(10, 1, 10, "chaining", DEBUG);
        chaining.insert(5);
        chaining.insert(15); // same bucket
        assertTrue(chaining.search(5), "Should find key 5 in chaining.");
        assertTrue(chaining.search(15), "Should find key 15 in chaining.");
        assertFalse(chaining.search(200), "Should not find key 200 in chaining.");
    }

    /**
     * Test quadratic probing with custom c1 and c2 values.
     */
    @Test
    public void testQuadraticProbingWithCustomC1C2() {
        // Use distinct constants for variation
        double customC1 = 1.0;
        double customC2 = 1.0;

        // Create a table with the overloaded constructor
        DivisionHashTable table = new DivisionHashTable(10, 1, 10, "quadratic", DEBUG, customC1, customC2);

        table.insert(2);   // Expect to hash to index 2
        table.insert(12);  // Same index → should probe based on custom formula

        // Validate that both keys are in table
        assertTrue(table.search(2), "Should find key 2.");
        assertTrue(table.search(12), "Should find key 12.");

        PerformanceMetrics metrics = table.metrics;
        assertEquals(2, metrics.getTotalInsertions());
        assertTrue(metrics.getTotalComparisons() >= 2);
        assertTrue(metrics.getTotalProbes() >= 1);
    }
}
