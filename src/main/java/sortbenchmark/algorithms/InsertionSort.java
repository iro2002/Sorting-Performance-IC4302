package sortbenchmark.algorithms;

import sortbenchmark.model.SortItem;

/** Insertion Sort: O(n^2) average/worst case; O(1) extra space. */
public final class InsertionSort implements SortAlgorithm {
    @Override public String getName() { return "Insertion Sort"; }

    @Override
    public void sort(SortItem[] values) {
        for (int i = 1; i < values.length; i++) {
            SortItem current = values[i];
            int j = i - 1;
            while (j >= 0 && values[j].compareTo(current) > 0) {
                values[j + 1] = values[j];
                j--;
            }
            values[j + 1] = current;
        }
    }
}
