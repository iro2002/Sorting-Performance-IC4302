package sortbenchmark;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sortbenchmark.algorithms.HeapSort;
import sortbenchmark.algorithms.InsertionSort;
import sortbenchmark.algorithms.MergeSort;
import sortbenchmark.algorithms.QuickSort;
import sortbenchmark.algorithms.ShellSort;
import sortbenchmark.algorithms.SortAlgorithm;
import sortbenchmark.model.SortItem;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Automated tests for the 5 sorting algorithms and evaluator. */
public class AlgorithmTests {

    static SortAlgorithm[] algorithms() {
        return new SortAlgorithm[] {
                new InsertionSort(),
                new ShellSort(),
                new MergeSort(),
                new QuickSort(),
                new HeapSort()
        };
    }

    private static SortItem[] wrap(double... values) {
        SortItem[] items = new SortItem[values.length];
        for (int i = 0; i < values.length; i++) {
            items[i] = new SortItem(values[i], i, new String[] { String.valueOf(values[i]) });
        }
        return items;
    }

    private static void assertSorted(SortItem[] items) {
        for (int i = 1; i < items.length; i++) {
            assertTrue(items[i - 1].compareTo(items[i]) <= 0,
                    "Item at index " + (i - 1) + " must be <= item at index " + i);
        }
    }

    @ParameterizedTest(name = "{0} - basic numeric array")
    @MethodSource("algorithms")
    void testBasicSort(SortAlgorithm algorithm) {
        SortItem[] items = wrap(64.5, 34.2, 25.0, 12.0, 22.1, 11.0, 90.3);
        algorithm.sort(items);
        assertSorted(items);
        assertEquals(11.0, items[0].getKey(), 1e-9);
        assertEquals(90.3, items[items.length - 1].getKey(), 1e-9);
    }

    @ParameterizedTest(name = "{0} - empty and single element")
    @MethodSource("algorithms")
    void testEdgeCases(SortAlgorithm algorithm) {
        SortItem[] empty = new SortItem[0];
        algorithm.sort(empty);
        assertEquals(0, empty.length);

        SortItem[] single = wrap(42.0);
        algorithm.sort(single);
        assertEquals(1, single.length);
        assertEquals(42.0, single[0].getKey(), 1e-9);
    }

    @ParameterizedTest(name = "{0} - already sorted and reversed")
    @MethodSource("algorithms")
    void testSortedAndReversed(SortAlgorithm algorithm) {
        SortItem[] sorted = wrap(1.0, 2.0, 3.0, 4.0, 5.0);
        algorithm.sort(sorted);
        assertSorted(sorted);

        SortItem[] reversed = wrap(5.0, 4.0, 3.0, 2.0, 1.0);
        algorithm.sort(reversed);
        assertSorted(reversed);
    }

    @ParameterizedTest(name = "{0} - duplicates and negative values")
    @MethodSource("algorithms")
    void testDuplicatesAndNegatives(SortAlgorithm algorithm) {
        SortItem[] duplicates = wrap(5.0, -2.5, 5.0, 0.0, -10.0, 5.0, -2.5);
        algorithm.sort(duplicates);
        assertSorted(duplicates);
        assertEquals(-10.0, duplicates[0].getKey(), 1e-9);
        assertEquals(5.0, duplicates[duplicates.length - 1].getKey(), 1e-9);
    }

    @ParameterizedTest(name = "{0} - randomized large array")
    @MethodSource("algorithms")
    void testRandomArray(SortAlgorithm algorithm) {
        Random random = new Random(42);
        int size = 500;
        double[] raw = new double[size];
        for (int i = 0; i < size; i++) {
            raw[i] = random.nextDouble() * 1000 - 500;
        }
        SortItem[] items = wrap(raw);
        algorithm.sort(items);
        assertSorted(items);
    }
}
