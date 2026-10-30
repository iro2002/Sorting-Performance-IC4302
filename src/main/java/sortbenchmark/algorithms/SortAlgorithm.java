package sortbenchmark.algorithms;

import sortbenchmark.model.SortItem;

/** Common interface, allowing the evaluator to run all algorithms fairly. */
public interface SortAlgorithm {
    String getName();

    /** Sorts the supplied array in ascending numeric order, in place. */
    void sort(SortItem[] values);
}
