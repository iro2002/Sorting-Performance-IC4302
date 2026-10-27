package sortbenchmark.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** UTF-8 CSV parser supporting quoted commas, escaped quotes and newlines. */
public final class CsvLoader {
    private CsvLoader() {}

    public static CsvDataset load(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IOException("Please choose an existing CSV file.");
        }
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new IOException("Only .csv files can be uploaded.");
        }

        List<List<String>> records = new ArrayList<>();
        try (PushbackReader reader = new PushbackReader(
                new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8)), 1)) {
            int first = reader.read();
            if (first != -1 && first != '\uFEFF') { reader.unread(first); }

            List<String> record = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            boolean afterQuote = false;
            boolean hasContent = false;
            int next;
            while ((next = reader.read()) != -1) {
                char ch = (char) next;

                if (quoted) {
                    if (ch == '"') {
                        int lookahead = reader.read();
                        if (lookahead == '"') {
                            field.append('"');
                        } else {
                            quoted = false;
                            afterQuote = true;
                            if (lookahead != -1) { reader.unread(lookahead); }
                        }
                    } else { field.append(ch); }
                    continue;
                }

                if (ch == ',') {
                    record.add(field.toString());
                    field.setLength(0);
                    afterQuote = false;
                    hasContent = true;
                } else if (ch == '\n' || ch == '\r') {
                    if (ch == '\r') {
                        int lookahead = reader.read();
                        if (lookahead != '\n' && lookahead != -1) { reader.unread(lookahead); }
                    }
                    if (hasContent) {
                        record.add(field.toString());
                        records.add(record);
                    }
                    record = new ArrayList<>();
                    field.setLength(0);
                    afterQuote = false;
                    hasContent = false;
                } else if (ch == '"') {
                    if (field.length() != 0 || afterQuote) {
                        throw new IOException("Invalid quotation mark near CSV record " + (records.size() + 1) + ".");
                    }
                    quoted = true;
                    hasContent = true;
                } else {
                    if (afterQuote) {
                        throw new IOException("Unexpected text after closing quote near CSV record "
                                + (records.size() + 1) + ".");
                    }
                    field.append(ch);
                    hasContent = true;
                }
            }
            if (quoted) { throw new IOException("The CSV file contains an unclosed quoted value."); }
            if (hasContent) {
                record.add(field.toString());
                records.add(record);
            }
        }

        if (records.isEmpty()) {
            throw new IOException("This CSV file is empty. It needs a header row and data.");
        }
        List<String> headers = new ArrayList<>();
        List<String> firstRecord = records.get(0);
        for (int i = 0; i < firstRecord.size(); i++) {
            String name = firstRecord.get(i).trim();
            headers.add(name.isEmpty() ? "Column " + (i + 1) : name);
        }
        List<String[]> rows = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            List<String> record = records.get(i);
            if (record.size() != headers.size()) {
                throw new IOException("CSV record " + (i + 1) + " has " + record.size()
                        + " columns; expected " + headers.size() + ".");
            }
            rows.add(record.toArray(new String[0]));
        }
        return new CsvDataset(headers, rows);
    }
}
