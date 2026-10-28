package sortbenchmark.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.File;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import sortbenchmark.benchmark.AlgorithmResult;
import sortbenchmark.benchmark.BenchmarkReport;
import sortbenchmark.benchmark.PerformanceEvaluator;
import sortbenchmark.data.CsvDataset;
import sortbenchmark.data.CsvExporter;
import sortbenchmark.data.CsvLoader;
import sortbenchmark.model.SortItem;

/**
 * Desktop GUI: CSV selection, numeric columns, sorted data and rankings.
 * University of Colombo | Faculty of Technology | Department of ICT | IC4302 - Programming III.
 */
public final class SortingAppFrame extends JFrame {
    private static final Color NAVY_PRIMARY = new Color(15, 35, 65);
    private static final Color ACCENT_BLUE = new Color(29, 78, 216);
    private static final Color SUCCESS_GREEN = new Color(16, 124, 65);
    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    // High-visibility modern buttons with custom rendering for all Look and Feels
    private final JButton uploadButton = new ModernButton("📂  Upload CSV", NAVY_PRIMARY, Color.WHITE);
    private final JButton sortButton = new ModernButton("⚡  Compare 5 Algorithms", ACCENT_BLUE, Color.WHITE);
    private final JButton exportButton = new ModernButton("💾  Export Sorted CSV", SUCCESS_GREEN, Color.WHITE);

    private final JComboBox<ColumnOption> columnCombo = new JComboBox<>();
    private final JTextField selectedFile = new JTextField("No CSV file selected");
    private final JLabel fileInfo = new JLabel("ℹ️  Please upload a UTF-8 CSV file to begin benchmarking.");
    private final JPanel winnerCard = new JPanel(new BorderLayout(8, 0));
    private final JLabel winnerLabel = new JLabel("ℹ️  Upload a CSV file, select a numeric column, and run comparison to find the fastest algorithm.");
    private final JLabel dataTitle = new JLabel("Dataset — Original Rows");
    private final JLabel statusLabel = new JLabel("● Ready");
    private final JProgressBar progressBar = new JProgressBar();
    private final CsvTableModel csvTableModel = new CsvTableModel();
    private final TimingTableModel timingTableModel = new TimingTableModel();
    private final JTable dataTable = new JTable(csvTableModel);
    private final JTable timingTable = new JTable(timingTableModel);

    private CsvDataset dataset;
    private BenchmarkReport report;
    private boolean busy;

    public SortingAppFrame() {
        super("Sorting Algorithm Performance Evaluator — Department of ICT, Faculty of Technology, University of Colombo (IC4302)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 700));
        setSize(1140, 820);
        setLocationRelativeTo(null);
        createLayout();
        bindEvents();
        updateButtons();
    }

    private void createLayout() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        content.setBackground(BG_LIGHT);
        setContentPane(content);

        // ==========================================
        // 1. TOP SECTION: University Branding & Header
        // ==========================================
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);

        // Institutional branding bar
        JPanel institutionBar = new JPanel(new BorderLayout(10, 0));
        institutionBar.setBackground(NAVY_PRIMARY);
        institutionBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 58, 95), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        JPanel instLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        instLeft.setOpaque(false);

        JLabel uniLabel = new JLabel("UNIVERSITY OF COLOMBO   •   FACULTY OF TECHNOLOGY");
        uniLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        uniLabel.setForeground(new Color(191, 219, 254));

        JLabel deptLabel = new JLabel("Department of ICT");
        deptLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        deptLabel.setForeground(Color.WHITE);

        instLeft.add(uniLabel);
        instLeft.add(deptLabel);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgePanel.setOpaque(false);
        JLabel courseBadge = new JLabel(" IC4302 – Programming III ");
        courseBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        courseBadge.setForeground(Color.WHITE);
        courseBadge.setOpaque(true);
        courseBadge.setBackground(new Color(37, 99, 235));
        courseBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(96, 165, 250), 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        badgePanel.add(courseBadge);

        institutionBar.add(instLeft, BorderLayout.WEST);
        institutionBar.add(badgePanel, BorderLayout.EAST);
        topContainer.add(institutionBar, BorderLayout.NORTH);

        // App title & controls card
        JPanel appCard = new JPanel(new BorderLayout(0, 10));
        appCard.setBackground(Color.WHITE);
        appCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Sorting Algorithm Performance Evaluator");
        title.setForeground(NAVY_PRIMARY);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        JLabel subtitle = new JLabel("Empirical benchmarking of 5 sorting algorithms (Insertion, Shell, Merge, Quick, Heap Sort) with automated verification.");
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        titlePanel.add(title);
        titlePanel.add(subtitle);
        appCard.add(titlePanel, BorderLayout.NORTH);

        // Controls Area organized into 3 distinct, spacious rows
        JPanel controlsArea = new JPanel(new GridLayout(3, 1, 0, 8));
        controlsArea.setOpaque(false);

        // Row 1: File selection line
        JPanel uploadLine = new JPanel(new BorderLayout(10, 0));
        uploadLine.setOpaque(false);
        selectedFile.setEditable(false);
        selectedFile.setBackground(new Color(248, 250, 252));
        selectedFile.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        selectedFile.setPreferredSize(new Dimension(300, 34));
        selectedFile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        uploadLine.add(selectedFile, BorderLayout.CENTER);
        uploadLine.add(uploadButton, BorderLayout.EAST);
        controlsArea.add(uploadLine);

        // Row 2: Dataset information banner
        JPanel infoStrip = new JPanel(new BorderLayout());
        infoStrip.setBackground(new Color(241, 245, 249));
        infoStrip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        fileInfo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        fileInfo.setForeground(new Color(51, 65, 85));
        infoStrip.add(fileInfo, BorderLayout.CENTER);
        controlsArea.add(infoStrip);

        // Row 3: Column selector and high-visibility action buttons
        JPanel actionLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionLine.setOpaque(false);

        JLabel colLabel = new JLabel("Sort by Column:");
        colLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        colLabel.setForeground(NAVY_PRIMARY);
        actionLine.add(colLabel);

        columnCombo.setPreferredSize(new Dimension(240, 34));
        columnCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        actionLine.add(columnCombo);

        actionLine.add(sortButton);
        actionLine.add(exportButton);

        controlsArea.add(actionLine);
        appCard.add(controlsArea, BorderLayout.SOUTH);
        topContainer.add(appCard, BorderLayout.SOUTH);

        content.add(topContainer, BorderLayout.NORTH);

        // ==========================================
        // 2. CENTER SECTION: Data Preview & Results Split
        // ==========================================
        // Dataset Preview Panel
        JPanel dataPanel = new JPanel(new BorderLayout(0, 6));
        dataPanel.setBackground(Color.WHITE);
        dataPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        dataTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        dataTitle.setForeground(NAVY_PRIMARY);
        dataPanel.add(dataTitle, BorderLayout.NORTH);

        dataTable.setRowHeight(24);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setGridColor(BORDER_COLOR);
        dataTable.setShowGrid(true);
        dataTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        dataTable.getTableHeader().setDefaultRenderer(new ModernHeaderRenderer());
        dataTable.getTableHeader().setReorderingAllowed(false);
        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 28));

        JScrollPane dataScroll = new JScrollPane(dataTable);
        dataScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        dataPanel.add(dataScroll, BorderLayout.CENTER);

        // Results Panel
        JPanel resultsPanel = new JPanel(new BorderLayout(0, 6));
        resultsPanel.setBackground(Color.WHITE);
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel timingTitle = new JLabel("Performance Results — Median of 5 Measured Runs (ms)");
        timingTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        timingTitle.setForeground(NAVY_PRIMARY);
        resultsPanel.add(timingTitle, BorderLayout.NORTH);

        timingTable.setRowHeight(26);
        timingTable.setFillsViewportHeight(true);
        timingTable.setGridColor(BORDER_COLOR);
        timingTable.setShowGrid(true);
        timingTable.getTableHeader().setDefaultRenderer(new ModernHeaderRenderer());
        timingTable.getTableHeader().setReorderingAllowed(false);
        timingTable.getTableHeader().setPreferredSize(new Dimension(0, 28));
        timingTable.setDefaultRenderer(Object.class, new TimingTableRenderer());
        timingTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        timingTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        timingTable.getColumnModel().getColumn(2).setPreferredWidth(160);

        JScrollPane timingScroll = new JScrollPane(timingTable);
        timingScroll.setPreferredSize(new Dimension(900, 155));
        timingScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        resultsPanel.add(timingScroll, BorderLayout.CENTER);

        // Winner Banner Card
        winnerCard.setBackground(new Color(241, 245, 249));
        winnerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        winnerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        winnerLabel.setForeground(new Color(71, 85, 105));
        winnerCard.add(winnerLabel, BorderLayout.CENTER);
        resultsPanel.add(winnerCard, BorderLayout.SOUTH);

        // Split Pane between dataset and timing results
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dataPanel, resultsPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);
        content.add(splitPane, BorderLayout.CENTER);

        // ==========================================
        // 3. SOUTH SECTION: Status Bar & Institution Credit
        // ==========================================
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JPanel statusLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusLeft.setOpaque(false);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(new Color(71, 85, 105));
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(120, 14));
        statusLeft.add(statusLabel);
        statusLeft.add(progressBar);

        JLabel footerCredit = new JLabel("Department of ICT • Faculty of Technology • University of Colombo");
        footerCredit.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        footerCredit.setForeground(new Color(148, 163, 184));

        footer.add(statusLeft, BorderLayout.WEST);
        footer.add(footerCredit, BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);
    }

    private void bindEvents() {
        uploadButton.addActionListener(event -> chooseCsv());
        sortButton.addActionListener(event -> runComparison());
        exportButton.addActionListener(event -> exportSortedData());
        columnCombo.addActionListener(event -> {
            if (!busy && dataset != null) {
                report = null;
                csvTableModel.showOriginalRows();
                timingTableModel.setReport(null);
                setWinnerDisplay(null);
                dataTitle.setText("Dataset — Original Rows (" + dataset.getRowCount() + " rows)");
                exportButton.setEnabled(false);
            }
        });
    }

    private void chooseCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a UTF-8 CSV file");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        File samples = new File("sample-data");
        if (samples.isDirectory()) { chooser.setCurrentDirectory(samples); }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) { return; }
        File file = chooser.getSelectedFile();
        setBusy(true, "Loading CSV file...");
        new SwingWorker<CsvDataset, Void>() {
            @Override protected CsvDataset doInBackground() throws Exception {
                return CsvLoader.load(file.toPath());
            }

            @Override protected void done() {
                try {
                    CsvDataset loaded = get();
                    dataset = loaded;
                    report = null;
                    selectedFile.setText(file.getAbsolutePath());
                    csvTableModel.setDataset(loaded);
                    timingTableModel.setReport(null);
                    setWinnerDisplay(null);
                    dataTitle.setText("Dataset — Original Rows (" + loaded.getRowCount() + " rows)");
                    List<Integer> numeric = loaded.getNumericColumns();
                    columnCombo.removeAllItems();
                    for (int index : numeric) {
                        columnCombo.addItem(new ColumnOption(index, loaded.getHeaders().get(index)));
                    }
                    fileInfo.setText(String.format(Locale.US,
                            "📊 Dataset Summary:  Rows: %s  |  Total Columns: %d  |  Numeric Columns for Sorting: %d",
                            NumberFormat.getIntegerInstance(Locale.US).format(loaded.getRowCount()),
                            loaded.getHeaders().size(),
                            numeric.size()));
                    statusLabel.setText("● Loaded " + file.getName() + " successfully.");
                    if (numeric.isEmpty()) {
                        JOptionPane.showMessageDialog(SortingAppFrame.this,
                                "The file loaded, but no column contains valid numbers in every row.\n"
                                + "Remove missing/text values from a numeric column and try again.",
                                "No numeric columns", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("File loading was interrupted.");
                } catch (ExecutionException exception) {
                    showError(messageFrom(exception.getCause()));
                } finally {
                    setBusy(false, statusLabel.getText());
                }
            }
        }.execute();
    }

    private void runComparison() {
        ColumnOption selected = (ColumnOption) columnCombo.getSelectedItem();
        if (dataset == null || selected == null) {
            showError("Upload a CSV file and select a numeric column first.");
            return;
        }
        if (dataset.getRowCount() > 10000) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "This file contains more than 10,000 rows.\n"
                    + "Insertion Sort can be much slower on large datasets. Continue?",
                    "Large dataset warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) { return; }
        }
        final int column = selected.index;
        setBusy(true, "Running and checking five sorting algorithms...");
        new SwingWorker<BenchmarkReport, Void>() {
            @Override protected BenchmarkReport doInBackground() {
                return new PerformanceEvaluator().evaluate(dataset, column);
            }

            @Override protected void done() {
                try {
                    report = get();
                    timingTableModel.setReport(report);
                    AlgorithmResult fastest = report.getFastest();
                    csvTableModel.showSortedRows(fastest.getSortedItems());
                    dataTitle.setText("Dataset — Sorted by " + fastest.getAlgorithmName() + " (" + dataset.getRowCount() + " rows)");
                    setWinnerDisplay(fastest);
                    statusLabel.setText("✔ Benchmark completed: 5 algorithms evaluated, 5 runs each, outputs verified.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Performance evaluation was interrupted.");
                } catch (ExecutionException exception) {
                    report = null;
                    showError(messageFrom(exception.getCause()));
                } finally {
                    setBusy(false, statusLabel.getText());
                }
            }
        }.execute();
    }

    private void setWinnerDisplay(AlgorithmResult fastest) {
        if (fastest != null) {
            winnerCard.setBackground(new Color(236, 253, 245));
            winnerCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(110, 231, 183), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            winnerLabel.setForeground(new Color(6, 95, 70));
            winnerLabel.setText(String.format(Locale.US,
                    "🏆  FASTEST ALGORITHM: %s   |   Median Execution Time: %.6f ms   (Verified 100%% against reference sort)",
                    fastest.getAlgorithmName(), fastest.getMilliseconds()));
        } else {
            winnerCard.setBackground(new Color(241, 245, 249));
            winnerCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            winnerLabel.setForeground(new Color(71, 85, 105));
            winnerLabel.setText("ℹ️  Upload a CSV file, select a numeric column, and run comparison to find the fastest algorithm.");
        }
    }

    private void exportSortedData() {
        if (dataset == null || report == null) { return; }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save sorted CSV");
        chooser.setSelectedFile(new File("sorted-results.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) { return; }
        Path target = chooser.getSelectedFile().toPath();
        if (!target.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            target = target.resolveSibling(target.getFileName().toString() + ".csv");
        }
        if (target.toFile().exists()) {
            int decision = JOptionPane.showConfirmDialog(this, "Overwrite the existing file?",
                    "Confirm export", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (decision != JOptionPane.YES_OPTION) { return; }
        }
        try {
            CsvExporter.export(target, dataset.getHeaders(), report.getFastest().getSortedItems());
            statusLabel.setText("✔ Saved sorted data to " + target.toAbsolutePath());
            JOptionPane.showMessageDialog(this, "CSV exported successfully.", "Export complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            showError("Could not export CSV: " + messageFrom(exception));
        }
    }

    private void setBusy(boolean isBusy, String message) {
        busy = isBusy;
        statusLabel.setText(message);
        progressBar.setVisible(isBusy);
        progressBar.setIndeterminate(isBusy);
        updateButtons();
    }

    private void updateButtons() {
        uploadButton.setEnabled(!busy);
        boolean canSort = !busy && dataset != null && columnCombo.getItemCount() > 0;
        sortButton.setEnabled(canSort);
        sortButton.setToolTipText(canSort
                ? "Click to benchmark all 5 algorithms on the chosen column"
                : "Upload a CSV file and select a numeric column to enable");

        boolean canExport = !busy && report != null;
        exportButton.setEnabled(canExport);
        exportButton.setToolTipText(canExport
                ? "Click to export the fastest algorithm's sorted dataset"
                : "Run algorithm comparison first to enable export");

        columnCombo.setEnabled(canSort);
    }

    private void showError(String message) {
        statusLabel.setText("⚠️ Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Application error", JOptionPane.ERROR_MESSAGE);
    }

    private static String messageFrom(Throwable throwable) {
        return throwable == null || throwable.getMessage() == null
                ? "An unexpected error occurred." : throwable.getMessage();
    }

    private static final class ColumnOption {
        private final int index;
        private final String name;

        private ColumnOption(int index, String name) {
            this.index = index;
            this.name = name;
        }

        @Override public String toString() { return name + " (col " + (index + 1) + ")"; }
    }

    private static final class CsvTableModel extends AbstractTableModel {
        private CsvDataset dataset;
        private SortItem[] sorted;

        private void setDataset(CsvDataset dataset) {
            this.dataset = dataset;
            sorted = null;
            fireTableStructureChanged();
        }

        private void showSortedRows(SortItem[] sorted) {
            this.sorted = sorted;
            fireTableDataChanged();
        }

        private void showOriginalRows() {
            sorted = null;
            fireTableDataChanged();
        }

        @Override public int getColumnCount() { return dataset == null ? 0 : dataset.getHeaders().size(); }
        @Override public int getRowCount() { return dataset == null ? 0 : dataset.getRowCount(); }
        @Override public String getColumnName(int index) { return dataset.getHeaders().get(index); }
        @Override public Object getValueAt(int row, int column) {
            String[] cells = sorted == null ? dataset.getRows().get(row) : sorted[row].getCells();
            return cells[column];
        }
    }

    private static final class TimingTableModel extends AbstractTableModel {
        private final List<AlgorithmResult> results = new ArrayList<>();
        private static final String[] HEADERS = { "Algorithm", "Median Time (ms)", "Benchmark Result" };

        private void setReport(BenchmarkReport report) {
            results.clear();
            if (report != null) { results.addAll(report.getResults()); }
            fireTableDataChanged();
        }

        @Override public int getColumnCount() { return HEADERS.length; }
        @Override public int getRowCount() { return results.size(); }
        @Override public String getColumnName(int column) { return HEADERS[column]; }
        @Override public Object getValueAt(int row, int column) {
            AlgorithmResult result = results.get(row);
            switch (column) {
                case 0: return result.getAlgorithmName();
                case 1: return String.format(Locale.US, "%.6f", result.getMilliseconds());
                case 2: return row == 0 ? "★ FASTEST (Rank 1)" : ("Rank " + (row + 1) + " (Verified)");
                default: return "";
            }
        }
    }

    private static final class AlternatingRowRenderer extends DefaultTableCellRenderer {
        private static final Color EVEN_ROW = Color.WHITE;
        private static final Color ODD_ROW = new Color(248, 250, 252);
        private static final Color SELECTION_BG = new Color(219, 234, 254);
        private static final Color SELECTION_FG = new Color(30, 58, 138);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (isSelected) {
                setBackground(SELECTION_BG);
                setForeground(SELECTION_FG);
            } else {
                setBackground(row % 2 == 0 ? EVEN_ROW : ODD_ROW);
                setForeground(new Color(30, 41, 59));
            }
            return this;
        }
    }

    private static final class TimingTableRenderer extends DefaultTableCellRenderer {
        private static final Color WINNER_BG = new Color(220, 252, 231);
        private static final Color WINNER_FG = new Color(22, 101, 52);
        private static final Color EVEN_ROW = Color.WHITE;
        private static final Color ODD_ROW = new Color(248, 250, 252);
        private static final Color SELECTION_BG = new Color(219, 234, 254);
        private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        private static final Font BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        private static final Font REGULAR_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            boolean isWinnerRow = (row == 0 && table.getRowCount() > 0);

            if (column == 1) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(MONO_FONT);
            } else if (column == 2) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(BOLD_FONT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(isWinnerRow ? BOLD_FONT : REGULAR_FONT);
            }

            if (isSelected) {
                setBackground(SELECTION_BG);
                setForeground(new Color(30, 58, 138));
            } else if (isWinnerRow) {
                setBackground(WINNER_BG);
                setForeground(WINNER_FG);
            } else {
                setBackground(row % 2 == 0 ? EVEN_ROW : ODD_ROW);
                setForeground(new Color(30, 41, 59));
            }

            return this;
        }
    }

    private static final class ModernHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(NAVY_PRIMARY);
            setForeground(Color.WHITE);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            if (column == 1 && table.getColumnCount() == 3) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (column == 2 && table.getColumnCount() == 3) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(51, 65, 85)),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            return this;
        }
    }

    /**
     * Custom rendered, modern anti-aliased button that provides high-contrast,
     * crisp visibility across all Java Look-and-Feels (including Windows L&F).
     */
    private static final class ModernButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final Color normalFg;

        private ModernButton(String text, Color bg, Color fg) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = brighten(bg, 0.15f);
            this.pressedBg = bg.darker();
            this.normalFg = fg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(fg);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        }

        private static Color brighten(Color c, float factor) {
            int r = Math.min(255, (int) (c.getRed() + (255 - c.getRed()) * factor));
            int g = Math.min(255, (int) (c.getGreen() + (255 - c.getGreen()) * factor));
            int b = Math.min(255, (int) (c.getBlue() + (255 - c.getBlue()) * factor));
            return new Color(r, g, b);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = fm.stringWidth(getText()) + 32;
            return new Dimension(w, 34);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean isEnabled = isEnabled();
            ButtonModel model = getModel();

            Color bg;
            Color border;
            Color fg;

            if (!isEnabled) {
                bg = new Color(241, 245, 249);       // Slate 100
                border = new Color(203, 213, 225);   // Slate 300
                fg = new Color(100, 116, 139);       // Slate 500 (high contrast, clearly legible)
            } else if (model.isPressed()) {
                bg = pressedBg;
                border = pressedBg.darker();
                fg = normalFg;
            } else if (model.isRollover()) {
                bg = hoverBg;
                border = normalBg.darker();
                fg = normalFg;
            } else {
                bg = normalBg;
                border = normalBg.darker();
                fg = normalFg;
            }

            // Fill rounded background
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            // Draw border
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            // Draw text
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }
}
