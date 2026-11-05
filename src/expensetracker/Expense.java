package expensetracker;

import java.time.LocalDate;
import java.time.YearMonth;

/** One spending entry. Immutable: to change an expense, replace it with a new one. */
public class Expense {

    private final LocalDate date;
    private final String category;
    private final String description;
    private final double amount;

    public Expense(LocalDate date, String category, String description, double amount) {
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    /** The month of this expense in yyyy-MM form, for example 2026-11. */
    public String getMonth() {
        return YearMonth.from(date).toString();
    }
}
