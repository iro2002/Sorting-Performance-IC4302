package sortbenchmark.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import sortbenchmark.model.SortItem;

/** Writes all CSV fields with correct quoting. */
public final class CsvExporter {
    private CsvExporter() {}

    public static void export(Path path, List<String> headers, SortItem[] sorted) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writeRecord(writer, headers.toArray(new String[0]));
            for (SortItem item : sorted) { writeRecord(writer, item.getCells()); }
        }
    }

    private static void writeRecord(BufferedWriter writer, String[] fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) { writer.write(','); }
            String value = fields[i] == null ? "" : fields[i];
            boolean needsQuotes = value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                    || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
            if (needsQuotes) {
                writer.write('"');
                writer.write(value.replace("\"", "\"\""));
                writer.write('"');
            } else { writer.write(value); }
        }
        writer.newLine();
    }
}
