package sortbenchmark.benchmark;

import sortbenchmark.model.SortItem;

/** One algorithm's median timing and fully sorted rows. */
public final class AlgorithmResult {
    private final String algorithmName;
    private final long medianNanoseconds;
    private final SortItem[] sortedItems;

    public AlgorithmResult(String algorithmName, long medianNanoseconds, SortItem[] sortedItems) {
        this.algorithmName = algorithmName;
        this.medianNanoseconds = medianNanoseconds;
        this.sortedItems = sortedItems;
    }

    public String getAlgorithmName() { return algorithmName; }
    public long getMedianNanoseconds() { return medianNanoseconds; }
    public double getMilliseconds() { return medianNanoseconds / 1_000_000.0; }
    public SortItem[] getSortedItems() { return sortedItems; }
}
