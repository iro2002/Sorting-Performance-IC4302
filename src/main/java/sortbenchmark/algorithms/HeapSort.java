package sortbenchmark.algorithms;

import sortbenchmark.model.SortItem;

/** Heap Sort: in-place max heap, O(n log n) worst-case time. */
public final class HeapSort implements SortAlgorithm {
    @Override public String getName() { return "Heap Sort"; }

    @Override
    public void sort(SortItem[] values) {
        int size = values.length;
        for (int parent = size / 2 - 1; parent >= 0; parent--) {
            siftDown(values, parent, size);
        }
        for (int end = size - 1; end > 0; end--) {
            SortItem temp = values[0];
            values[0] = values[end];
            values[end] = temp;
            siftDown(values, 0, end);
        }
    }

    private void siftDown(SortItem[] values, int root, int heapSize) {
        while (2L * root + 1 < heapSize) {
            int leftChild = root * 2 + 1;
            int largest = leftChild;
            int rightChild = leftChild + 1;
            if (rightChild < heapSize && values[rightChild].compareTo(values[leftChild]) > 0) {
                largest = rightChild;
            }
            if (values[root].compareTo(values[largest]) >= 0) { return; }
            SortItem temp = values[root];
            values[root] = values[largest];
            values[largest] = temp;
            root = largest;
        }
    }
}
