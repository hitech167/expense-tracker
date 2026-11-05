package expensetracker;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;

/** Starts the Expense Tracker with modern system font rendering. */
public class Main {

    public static void main(String[] args) {
        // Enable sub-pixel text anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Falls back to standard platform look and feel
            }

            ExpenseStore store = new ExpenseStore(ExpenseStore.defaultDir());
            try {
                store.load();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "Could not read your saved data from " + store.getDir() + ":\n" + ex.getMessage()
                                + "\n\nThe app will start empty.",
                        "Expense Tracker", JOptionPane.WARNING_MESSAGE);
            }
            new MainFrame(store).setVisible(true);
        });
    }
}
