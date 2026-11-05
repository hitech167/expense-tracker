package expensetracker;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Modern Donut Chart of monthly spending by category.
 * Features vibrant categorical colors, smooth anti-aliasing,
 * center balance readout, and high-readability legend with percentages.
 */
public class ChartPanel extends JPanel {

    private static final int LEGEND_ROW_HEIGHT = 24;
    private Map<String, Double> data = new LinkedHashMap<>();
    private String currency = "\u20B9";

    public ChartPanel() {
        setPreferredSize(new Dimension(300, 340));
        setOpaque(false);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        repaint();
    }

    public void setData(Map<String, Double> data) {
        this.data = data != null ? data : new LinkedHashMap<>();
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

        double total = 0;
        for (double v : data.values()) {
            total += v;
        }

        // Empty state
        if (total <= 0) {
            drawEmptyState(g2, width, height);
            g2.dispose();
            return;
        }

        int legendCount = data.size();
        int legendHeight = legendCount * LEGEND_ROW_HEIGHT + 10;
        int maxChartHeight = height - legendHeight - 20;
        int outerDiameter = Math.min(width - 40, maxChartHeight);
        outerDiameter = Math.max(outerDiameter, 100);

        int outerRadius = outerDiameter / 2;
        int innerRadius = (int) (outerRadius * 0.62); // 62% donut hole
        int centerX = width / 2;
        int centerY = 12 + outerRadius;

        // Draw donut slices
        double currentAngle = 90.0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double sliceAngle = (entry.getValue() / total) * 360.0;
            if (sliceAngle <= 0.05) continue;

            Color sliceColor = UITheme.getCategoryStyle(entry.getKey()).accent;

            // Outer arc
            Arc2D.Double outerArc = new Arc2D.Double(
                    centerX - outerRadius, centerY - outerRadius,
                    outerDiameter, outerDiameter,
                    currentAngle, -sliceAngle, Arc2D.PIE);

            // Inner hole
            Ellipse2D.Double innerHole = new Ellipse2D.Double(
                    centerX - innerRadius, centerY - innerRadius,
                    innerRadius * 2, innerRadius * 2);

            Area sliceArea = new Area(outerArc);
            sliceArea.subtract(new Area(innerHole));

            g2.setColor(sliceColor);
            g2.fill(sliceArea);

            // Subtle separator line
            g2.setColor(UITheme.CARD_BG);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(sliceArea);

            currentAngle -= sliceAngle;
        }

        // Center cutout text (Total & amount)
        g2.setFont(UITheme.bold(10f));
        g2.setColor(UITheme.TEXT_MUTED);
        String subhead = "TOTAL SPENT";
        FontMetrics fmSub = g2.getFontMetrics();
        int subW = fmSub.stringWidth(subhead);
        g2.drawString(subhead, centerX - (subW / 2), centerY - 6);

        String mainAmount = String.format(Locale.ROOT, "%s%,.0f", currency, total);
        g2.setFont(UITheme.bold(14f));
        g2.setColor(UITheme.TEXT_PRIMARY);
        FontMetrics fmAmt = g2.getFontMetrics();
        int amtW = fmAmt.stringWidth(mainAmount);
        g2.drawString(mainAmount, centerX - (amtW / 2), centerY + 14);

        // Draw legend below donut
        int rowY = centerY + outerRadius + 22;
        g2.setFont(UITheme.regular(12f));
        FontMetrics fmLg = g2.getFontMetrics();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String category = entry.getKey();
            double val = entry.getValue();
            Color color = UITheme.getCategoryStyle(category).accent;

            // Rounded color pill
            g2.setColor(color);
            g2.fillRoundRect(12, rowY - 11, 10, 10, 3, 3);

            // Category name
            g2.setColor(UITheme.TEXT_PRIMARY);
            g2.drawString(category, 30, rowY);

            // Percent tag + formatted amount
            double pct = (val / total) * 100.0;
            String percentStr = String.format(Locale.ROOT, "%.0f%%", pct);
            String amountStr = String.format(Locale.ROOT, "%s%,.2f", currency, val);

            int amtWidth = fmLg.stringWidth(amountStr);
            int pctWidth = fmLg.stringWidth(percentStr);

            // Percentage in muted badge
            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString(percentStr, width - 12 - amtWidth - pctWidth - 12, rowY);

            // Amount in bold text
            g2.setColor(UITheme.TEXT_PRIMARY);
            g2.drawString(amountStr, width - 12 - amtWidth, rowY);

            rowY += LEGEND_ROW_HEIGHT;
        }

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int width, int height) {
        int diameter = Math.min(width - 60, height - 80);
        diameter = Math.max(diameter, 90);
        int x = (width - diameter) / 2;
        int y = (height - diameter) / 2 - 10;

        // Draw soft dashed circle
        g2.setColor(UITheme.CARD_BORDER);
        float[] dash = {6f, 6f};
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
        g2.drawOval(x, y, diameter, diameter);

        // Empty message
        g2.setFont(UITheme.regular(12.5f));
        g2.setColor(UITheme.TEXT_MUTED);
        String msg = "No expenses to chart yet";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, y + (diameter / 2) + 4);
    }
}
