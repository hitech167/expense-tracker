package expensetracker;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bar chart that compares total spending of the last few months.
 * The selected month is highlighted, months over budget turn red,
 * and a dashed line shows the monthly budget.
 */
public class MonthlyBarChart extends JPanel {

    private Map<String, Double> data = new LinkedHashMap<>();
    private String highlightMonth = null;
    private double budget = 0;
    private String currency = "\u20B9";

    public MonthlyBarChart() {
        setPreferredSize(new Dimension(300, 340));
        setOpaque(false);
    }

    /**
     * @param months         total per month (yyyy-MM), oldest first
     * @param highlightMonth the month to emphasise, or null for none
     * @param budget         monthly budget, 0 when not set
     */
    public void setData(Map<String, Double> months, String highlightMonth, double budget, String currency) {
        this.data = months != null ? months : new LinkedHashMap<>();
        this.highlightMonth = highlightMonth;
        this.budget = budget;
        this.currency = currency;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        double max = 0;
        for (double v : data.values()) {
            max = Math.max(max, v);
        }
        if (max <= 0) {
            g2.setFont(UITheme.regular(12.5f));
            g2.setColor(UITheme.TEXT_MUTED);
            String msg = "No expenses to chart yet";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
            g2.dispose();
            return;
        }
        double scaleMax = Math.max(max, budget) * 1.15;

        int left = 8;
        int right = 8;
        int top = 30;
        int bottom = 34;
        int chartW = width - left - right;
        int chartH = Math.max(height - top - bottom, 40);
        int baseY = top + chartH;
        int count = data.size();
        double slot = chartW / (double) count;
        int barW = (int) Math.min(slot * 0.6, 44);

        // Baseline
        g2.setColor(UITheme.CARD_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(left, baseY, left + chartW, baseY);

        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue();
            int barH = (int) Math.round(value / scaleMax * chartH);
            int x = (int) Math.round(left + i * slot + (slot - barW) / 2.0);

            boolean selected = entry.getKey().equals(highlightMonth);
            Color color;
            if (budget > 0 && value > budget) {
                color = selected ? UITheme.DANGER : UITheme.BAR_DANGER_MUTED;
            } else {
                color = selected ? UITheme.PRIMARY : UITheme.BAR_MUTED;
            }

            g2.setColor(color);
            if (value > 0) {
                int h = Math.max(barH, 3);
                java.awt.Shape oldClip = g2.getClip();
                g2.clipRect(0, 0, width, baseY);
                g2.fill(new RoundRectangle2D.Double(x, baseY - h, barW, h + 8, 8, 8));
                g2.setClip(oldClip);

                String label = compact(value);
                g2.setFont(UITheme.bold(10.5f));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(selected ? UITheme.TEXT_PRIMARY : UITheme.TEXT_SECONDARY);
                g2.drawString(label, x + (barW - fm.stringWidth(label)) / 2, baseY - h - 5);
            } else {
                g2.fillRect(x, baseY - 2, barW, 2);
            }

            // Month name under the bar
            YearMonth ym = YearMonth.parse(entry.getKey());
            String name = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            String year = "'" + String.format(Locale.ROOT, "%02d", ym.getYear() % 100);
            g2.setFont(selected ? UITheme.bold(11f) : UITheme.regular(11f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(selected ? UITheme.PRIMARY : UITheme.TEXT_SECONDARY);
            g2.drawString(name, x + (barW - fm.stringWidth(name)) / 2, baseY + 16);
            g2.setFont(UITheme.regular(10f));
            fm = g2.getFontMetrics();
            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString(year, x + (barW - fm.stringWidth(year)) / 2, baseY + 29);
            i++;
        }

        // Dashed budget line with a label on the right
        if (budget > 0) {
            int y = baseY - (int) Math.round(budget / scaleMax * chartH);
            g2.setColor(UITheme.WARNING);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                    new float[]{5f, 4f}, 0f));
            g2.drawLine(left, y, left + chartW, y);
            g2.setFont(UITheme.bold(10f));
            FontMetrics fm = g2.getFontMetrics();
            String label = "Budget " + compact(budget);
            g2.setColor(UITheme.WARNING_TEXT);
            g2.drawString(label, left + chartW - fm.stringWidth(label), Math.max(y - 4, 12));
        }

        g2.dispose();
    }

    /** Short amount for a bar label, for example 850, 2.4k, 1.2M. */
    private String compact(double v) {
        if (v >= 1_000_000) {
            return currency + String.format(Locale.ROOT, "%.1fM", v / 1_000_000);
        }
        if (v >= 10_000) {
            return currency + String.format(Locale.ROOT, "%.0fk", v / 1000);
        }
        if (v >= 1000) {
            return currency + String.format(Locale.ROOT, "%.1fk", v / 1000);
        }
        return currency + String.format(Locale.ROOT, "%.0f", v);
    }
}
