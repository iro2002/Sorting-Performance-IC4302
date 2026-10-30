package sortbenchmark.algorithms;

import sortbenchmark.model.SortItem;

/** Quick Sort: midpoint pivot and smaller-side-first recursion. */
public final class QuickSort implements SortAlgorithm {
    @Override public String getName() { return "Quick Sort"; }

    @Override
    public void sort(SortItem[] values) {
        if (values.length > 1) { quickSort(values, 0, values.length - 1); }
    }

    private void quickSort(SortItem[] values, int low, int high) {
        while (low < high) {
            int i = low;
            int j = high;
            SortItem pivot = values[low + (high - low) / 2];
            while (i <= j) {
                while (values[i].compareTo(pivot) < 0) { i++; }
                while (values[j].compareTo(pivot) > 0) { j--; }
                if (i <= j) {
                    SortItem temp = values[i];
                    values[i] = values[j];
                    values[j] = temp;
                    i++;
                    j--;
                }
            }
            // Process the smaller partition recursively to limit stack growth.
            if (j - low < high - i) {
                if (low < j) { quickSort(values, low, j); }
                low = i;
            } else {
                if (i < high) { quickSort(values, i, high); }
                high = j;
            }
        }
    }
}
