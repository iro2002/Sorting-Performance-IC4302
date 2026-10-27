package sortbenchmark.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sortbenchmark.model.SortItem;

/** Holds CSV headers and original (unsorted) rows. */
public final class CsvDataset {
    private final List<String> headers;
    private final List<String[]> rows;

    public CsvDataset(List<String> headers, List<String[]> rows) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("The CSV file must contain a header row.");
        }
        this.headers = Collections.unmodifiableList(new ArrayList<>(headers));
        List<String[]> copiedRows = new ArrayList<>();
        for (String[] row : rows) {
            if (row.length != headers.size()) {
                throw new IllegalArgumentException("A CSV row has an unexpected number of columns.");
            }
            copiedRows.add(row.clone());
        }
        this.rows = Collections.unmodifiableList(copiedRows);
    }

    public List<String> getHeaders() { return headers; }
    public List<String[]> getRows() { return rows; }
    public int getRowCount() { return rows.size(); }

    /** A column is numeric only when every row has a finite numeric value. */
    public List<Integer> getNumericColumns() {
        List<Integer> numeric = new ArrayList<>();
        for (int column = 0; column < headers.size(); column++) {
            boolean valid = !rows.isEmpty();
            for (String[] row : rows) {
                try {
                    String value = row[column].trim();
                    if (value.isEmpty() || !Double.isFinite(Double.parseDouble(value))) {
                        valid = false;
                        break;
                    }
                } catch (NumberFormatException exception) {
                    valid = false;
                    break;
                }
            }
            if (valid) { numeric.add(column); }
        }
        return numeric;
    }

    /** Each item carries its complete source row, not just the numeric key. */
    public SortItem[] itemsForColumn(int column) {
        if (column < 0 || column >= headers.size()) {
            throw new IllegalArgumentException("Invalid column index.");
        }
        SortItem[] items = new SortItem[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            double key = Double.parseDouble(rows.get(i)[column].trim());
            if (!Double.isFinite(key)) {
                throw new IllegalArgumentException("Non-finite number found in the selected column.");
            }
            items[i] = new SortItem(key, i, rows.get(i));
        }
        return items;
    }
}
