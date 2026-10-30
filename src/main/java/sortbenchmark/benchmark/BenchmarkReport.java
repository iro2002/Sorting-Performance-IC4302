package sortbenchmark.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Ordered timing table (fastest first). */
public final class BenchmarkReport {
    private final List<AlgorithmResult> results;

    public BenchmarkReport(List<AlgorithmResult> results) {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("A benchmark report requires results.");
        }
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
    }

    public List<AlgorithmResult> getResults() { return results; }
    public AlgorithmResult getFastest() { return results.get(0); }
}
