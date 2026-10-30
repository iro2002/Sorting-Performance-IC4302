package sortbenchmark.algorithms;

import sortbenchmark.model.SortItem;

/** Shell Sort: gap-based insertion sort, with gaps halved each pass. */
public final class ShellSort implements SortAlgorithm {
    @Override public String getName() { return "Shell Sort"; }

    @Override
    public void sort(SortItem[] values) {
        int size = values.length;
        for (int gap = size / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < size; i++) {
                SortItem current = values[i];
                int j = i;
                while (j >= gap && values[j - gap].compareTo(current) > 0) {
                    values[j] = values[j - gap];
                    j -= gap;
                }
                values[j] = current;
            }
        }
    }
}
