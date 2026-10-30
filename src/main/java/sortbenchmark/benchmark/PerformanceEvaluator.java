package sortbenchmark.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import sortbenchmark.algorithms.HeapSort;
import sortbenchmark.algorithms.InsertionSort;
import sortbenchmark.algorithms.MergeSort;
import sortbenchmark.algorithms.QuickSort;
import sortbenchmark.algorithms.ShellSort;
import sortbenchmark.algorithms.SortAlgorithm;
import sortbenchmark.data.CsvDataset;
import sortbenchmark.model.SortItem;

/** Measures only each sort (not cloning or parsing) over five runs. */
public final class PerformanceEvaluator {
    private static final int MEASURED_RUNS = 5;

    private final SortAlgorithm[] algorithms = {
            new InsertionSort(), new ShellSort(), new MergeSort(), new QuickSort(), new HeapSort()
    };

    public BenchmarkReport evaluate(CsvDataset dataset, int column) {
        SortItem[] original = dataset.itemsForColumn(column);
        if (original.length == 0) {
            throw new IllegalArgumentException("The file does not contain any data rows.");
        }

        // The JDK sort provides an independent reference for correctness checking.
        SortItem[] expected = original.clone();
        Arrays.sort(expected);
        List<AlgorithmResult> results = new ArrayList<>();

        for (SortAlgorithm algorithm : algorithms) {
            // Warm-up reduces first-use/JIT bias; warm-up is not timed.
            SortItem[] warmup = original.clone();
            algorithm.sort(warmup);
            verifyCorrectness(algorithm, warmup, expected);

            long[] timings = new long[MEASURED_RUNS];
            SortItem[] firstSorted = null;
            for (int run = 0; run < MEASURED_RUNS; run++) {
                SortItem[] current = original.clone(); // identical unsorted input for every run
                long started = System.nanoTime();
                algorithm.sort(current);
                timings[run] = System.nanoTime() - started;

                verifyCorrectness(algorithm, current, expected);
                if (run == 0) { firstSorted = current; }
            }
            Arrays.sort(timings);
            results.add(new AlgorithmResult(algorithm.getName(), timings[MEASURED_RUNS / 2], firstSorted));
        }

        results.sort(Comparator.comparingLong(AlgorithmResult::getMedianNanoseconds));
        return new BenchmarkReport(results);
    }

    private static void verifyCorrectness(SortAlgorithm algorithm, SortItem[] actual, SortItem[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (actual[i] != expected[i]) {
                throw new IllegalStateException(algorithm.getName() + " produced incorrect results at row "
                        + (i + 1) + ".");
            }
        }
    }
}
