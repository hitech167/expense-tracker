package expensetracker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Keeps all expenses in memory and saves them to plain files in a "data" folder next to the app (the folder you run it from),
 * so no database is needed:
 * <ul>
 *   <li>expenses.csv: one line per expense</li>
 *   <li>settings.properties: the monthly budget</li>
 * </ul>
 */
public class ExpenseStore {

    public static final String[] CATEGORIES =
            {"Food", "Travel", "Shopping", "Bills", "Health", "Entertainment", "Other"};
    /** Currency symbols the user can pick: rupee, dollar, euro, pound. */
    public static final String[] CURRENCIES = {"\u20B9", "$", "\u20AC", "\u00A3"};
    public static final String CSV_HEADER = "date,category,description,amount";

    private final Path dir;
    private final Path dataFile;
    private final Path settingsFile;
    private final List<Expense> expenses = new ArrayList<>();
    private double monthlyBudget = 0;
    private String currency = CURRENCIES[0];
    private boolean darkMode = false;

    public ExpenseStore(Path dir) {
        this.dir = dir;
        this.dataFile = dir.resolve("expenses.csv");
        this.settingsFile = dir.resolve("settings.properties");
    }

    /** Folder used for saved data. Can be changed with -Dexpensetracker.home=/some/folder */
    public static Path defaultDir() {
        String custom = System.getProperty("expensetracker.home");
        if (custom != null && !custom.isEmpty()) {
            return Paths.get(custom);
        }
        Path local = Paths.get(System.getProperty("user.dir"), "data");
        copyOldDataIfNeeded(local);
        return local;
    }

    /** One-time move of data from the old hidden folder (~/.expense-tracker) into the project's data folder. */
    private static void copyOldDataIfNeeded(Path local) {
        Path old = Paths.get(System.getProperty("user.home"), ".expense-tracker");
        try {
            if (!Files.exists(local.resolve("expenses.csv")) && Files.exists(old.resolve("expenses.csv"))) {
                Files.createDirectories(local);
                for (String name : new String[]{"expenses.csv", "settings.properties"}) {
                    if (Files.exists(old.resolve(name))) {
                        Files.copy(old.resolve(name), local.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException ex) {
            // Not critical: the app simply starts with an empty data folder.
        }
    }

    public Path getDir() {
        return dir;
    }

    // ---------- loading and saving ----------

    public void load() throws IOException {
        expenses.clear();
        monthlyBudget = 0;
        currency = CURRENCIES[0];
        darkMode = false;

        if (Files.exists(dataFile)) {
            List<String> lines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    List<String> f = parseCsvLine(line);
                    expenses.add(new Expense(LocalDate.parse(f.get(0)), f.get(1), f.get(2),
                            Double.parseDouble(f.get(3))));
                } catch (RuntimeException ex) {
                    // Skip a damaged line instead of refusing to start.
                }
            }
        }

        if (Files.exists(settingsFile)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(settingsFile)) {
                p.load(in);
            }
            try {
                monthlyBudget = Double.parseDouble(p.getProperty("monthlyBudget", "0"));
            } catch (NumberFormatException ex) {
                monthlyBudget = 0;
            }
            darkMode = "dark".equals(p.getProperty("theme", "light"));
            String savedCurrency = p.getProperty("currency", CURRENCIES[0]);
            for (String c : CURRENCIES) {
                if (c.equals(savedCurrency)) {
                    currency = c;
                }
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(dir);

        // Write to a temporary file first so a crash never leaves a half-written data file.
        Path temp = dir.resolve("expenses.csv.tmp");
        try (BufferedWriter w = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            w.write(CSV_HEADER);
            w.newLine();
            for (Expense e : expenses) {
                w.write(toCsvLine(e, false));
                w.newLine();
            }
        }
        Files.move(temp, dataFile, StandardCopyOption.REPLACE_EXISTING);

        Properties p = new Properties();
        p.setProperty("monthlyBudget", String.format(Locale.ROOT, "%.2f", monthlyBudget));
        p.setProperty("currency", currency);
        p.setProperty("theme", darkMode ? "dark" : "light");
        try (OutputStream out = Files.newOutputStream(settingsFile)) {
            p.store(out, "Expense Tracker settings");
        }
    }

    // ---------- changing data ----------

    public List<Expense> getAll() {
        return Collections.unmodifiableList(expenses);
    }

    public void add(Expense e) {
        expenses.add(e);
    }

    public void replace(Expense oldExpense, Expense newExpense) {
        int index = expenses.indexOf(oldExpense);
        if (index >= 0) {
            expenses.set(index, newExpense);
        } else {
            expenses.add(newExpense);
        }
    }

    public void remove(Expense e) {
        expenses.remove(e);
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double value) {
        monthlyBudget = value;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean dark) {
        darkMode = dark;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String symbol) {
        for (String c : CURRENCIES) {
            if (c.equals(symbol)) {
                currency = c;
            }
        }
    }

    // ---------- calculations ----------

    /** All months that have at least one expense, newest first, for example [2026-11, 2026-10]. */
    public List<String> months() {
        TreeSet<String> set = new TreeSet<>(Comparator.reverseOrder());
        for (Expense e : expenses) {
            set.add(e.getMonth());
        }
        return new ArrayList<>(set);
    }

    public static double total(List<Expense> list) {
        double sum = 0;
        for (Expense e : list) {
            sum += e.getAmount();
        }
        return sum;
    }

    /** Total per category, largest first. */
    public static Map<String, Double> totalsByCategory(List<Expense> list) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (Expense e : list) {
            totals.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        List<Map.Entry<String, Double>> entries = new ArrayList<>(totals.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<String, Double> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : entries) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted;
    }

    /**
     * Total per month for the last {@code count} months ending at {@code end}, oldest first.
     * Months with no expenses are included with 0, so a chart always has the same number of bars.
     */
    public static Map<String, Double> totalsByMonth(List<Expense> list, YearMonth end, int count) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (int i = count - 1; i >= 0; i--) {
            totals.put(end.minusMonths(i).toString(), 0.0);
        }
        for (Expense e : list) {
            if (totals.containsKey(e.getMonth())) {
                totals.merge(e.getMonth(), e.getAmount(), Double::sum);
            }
        }
        return totals;
    }

    // ---------- CSV helpers ----------

    /**
     * @param forSpreadsheet when true, a description that starts with = + - or @ gets a leading
     *                       apostrophe so Excel does not run it as a formula
     */
    public static String toCsvLine(Expense e, boolean forSpreadsheet) {
        String description = e.getDescription();
        if (forSpreadsheet && !description.isEmpty() && "=+-@".indexOf(description.charAt(0)) >= 0) {
            description = "'" + description;
        }
        return e.getDate() + "," + quote(e.getCategory()) + "," + quote(description) + ","
                + String.format(Locale.ROOT, "%.2f", e.getAmount());
    }

    private static String quote(String text) {
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
