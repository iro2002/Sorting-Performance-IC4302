package sortbenchmark.model;

/** Numeric sort key plus its complete original CSV row. */
public final class SortItem implements Comparable<SortItem> {
    private final double key;
    private final int originalIndex;
    private final String[] cells;

    public SortItem(double key, int originalIndex, String[] cells) {
        this.key = key;
        this.originalIndex = originalIndex;
        this.cells = cells;
    }

    public double getKey() { return key; }
    public int getOriginalIndex() { return originalIndex; }
    public String[] getCells() { return cells; }

    @Override
    public int compareTo(SortItem other) {
        int comparison = Double.compare(key, other.key);
        // Keeps identical numeric values in their original file order for every algorithm.
        return comparison != 0 ? comparison : Integer.compare(originalIndex, other.originalIndex);
    }
}
