package expensetracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Small self-check for saving, loading and totals. No test library needed.
 * Run with: java -ea -cp out expensetracker.ExpenseStoreTest
 */
public class ExpenseStoreTest {

    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("expense-tracker-test");

        ExpenseStore store = new ExpenseStore(dir);
        store.add(new Expense(LocalDate.of(2026, 11, 2), "Food", "Lunch, with \"friends\"", 250.50));
        store.add(new Expense(LocalDate.of(2026, 11, 3), "Travel", "Bus pass", 800));
        store.add(new Expense(LocalDate.of(2026, 10, 28), "Food", "", 120));
        store.setMonthlyBudget(15000);
        store.save();

        ExpenseStore loaded = new ExpenseStore(dir);
        loaded.load();
        check(loaded.getAll().size() == 3, "all expenses are loaded");
        check("Lunch, with \"friends\"".equals(loaded.getAll().get(0).getDescription()),
                "commas and quotes survive saving");
        check(loaded.getAll().get(0).getAmount() == 250.50, "amount survives saving");
        check(loaded.getMonthlyBudget() == 15000, "budget survives saving");

        List<String> months = loaded.months();
        check(months.size() == 2 && months.get(0).equals("2026-11"), "months are listed newest first");

        Map<String, Double> totals = ExpenseStore.totalsByCategory(loaded.getAll());
        check(totals.keySet().iterator().next().equals("Travel"), "largest category comes first");
        check(Math.abs(totals.get("Food") - 370.50) < 0.001, "category totals add up");
        check(Math.abs(ExpenseStore.total(loaded.getAll()) - 1170.50) < 0.001, "overall total adds up");

        Expense formula = new Expense(LocalDate.of(2026, 11, 4), "Other", "=SUM(A1)", 1);
        check(ExpenseStore.toCsvLine(formula, true).contains("\"'=SUM(A1)\""),
                "exported formulas are neutralised");
        check(ExpenseStore.toCsvLine(formula, false).contains("\"=SUM(A1)\""),
                "saved data keeps the real text");

        Expense first = loaded.getAll().get(0);
        loaded.replace(first, new Expense(first.getDate(), "Bills", "Edited", 99));
        check(loaded.getAll().size() == 3 && loaded.getAll().get(0).getCategory().equals("Bills"),
                "replace edits one expense in place");
        loaded.remove(loaded.getAll().get(0));
        check(loaded.getAll().size() == 2, "remove deletes one expense");

        // Monthly totals: always the requested number of months, oldest first, empty months are 0
        Map<String, Double> byMonth = ExpenseStore.totalsByMonth(loaded.getAll(), YearMonth.of(2026, 11), 6);
        check(byMonth.size() == 6 && byMonth.keySet().iterator().next().equals("2026-06"),
                "monthly totals cover six months, oldest first");
        check(byMonth.get("2026-07") == 0.0, "months without expenses count as zero");
        check(Math.abs(byMonth.get("2026-10") - 120) < 0.001, "monthly total adds up");

        // Currency is saved and a bad value is ignored
        check("\u20B9".equals(loaded.getCurrency()), "default currency is the rupee");
        loaded.setCurrency("$");
        loaded.setCurrency("???");
        check("$".equals(loaded.getCurrency()), "unknown currency is ignored");
        loaded.save();
        ExpenseStore again = new ExpenseStore(dir);
        again.load();
        check("$".equals(again.getCurrency()), "currency survives saving");

        // Light / dark mode is saved
        check(!again.isDarkMode(), "light mode is the default");
        again.setDarkMode(true);
        again.save();
        ExpenseStore third = new ExpenseStore(dir);
        third.load();
        check(third.isDarkMode(), "dark mode survives saving");

        System.out.println("All checks passed.");
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError("FAILED: " + what);
        }
        System.out.println("ok  " + what);
    }
}
