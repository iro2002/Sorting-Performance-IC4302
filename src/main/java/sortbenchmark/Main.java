package sortbenchmark;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import sortbenchmark.ui.SortingAppFrame;

/** Application entry point. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // The default cross-platform look and feel is also supported.
            }
            new SortingAppFrame().setVisible(true);
        });
    }
}
