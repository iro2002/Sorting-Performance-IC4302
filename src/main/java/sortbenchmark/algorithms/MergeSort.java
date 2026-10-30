package sortbenchmark.algorithms;

import sortbenchmark.model.SortItem;

/** Merge Sort: O(n log n) time, O(n) auxiliary array. */
public final class MergeSort implements SortAlgorithm {
    @Override public String getName() { return "Merge Sort"; }

    @Override
    public void sort(SortItem[] values) {
        if (values.length > 1) {
            SortItem[] temporary = new SortItem[values.length];
            mergeSort(values, temporary, 0, values.length - 1);
        }
    }

    private void mergeSort(SortItem[] values, SortItem[] temporary, int left, int right) {
        if (left >= right) { return; }
        int middle = left + (right - left) / 2;
        mergeSort(values, temporary, left, middle);
        mergeSort(values, temporary, middle + 1, right);

        int i = left;
        int j = middle + 1;
        int output = left;
        while (i <= middle && j <= right) {
            if (values[i].compareTo(values[j]) <= 0) {
                temporary[output++] = values[i++];
            } else { temporary[output++] = values[j++]; }
        }
        while (i <= middle) { temporary[output++] = values[i++]; }
        while (j <= right) { temporary[output++] = values[j++]; }
        System.arraycopy(temporary, left, values, left, right - left + 1);
    }
}
