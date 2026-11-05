package expensetracker;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern design system & color palette for Expense Tracker.
 * Professional fintech / SaaS look with crisp cards, clean typography,
 * modern rounded controls, and harmonious categorical accents.
 */
public final class UITheme {

    private UITheme() {}

    // --- Base Colors (non-final: they change when the user switches between light and dark mode) ---
    public static Color APP_BG = new Color(245, 247, 251);        // soft slate background
    public static Color CARD_BG = new Color(255, 255, 255);       // crisp white cards
    public static Color CARD_BORDER = new Color(226, 232, 240);   // subtle border
    public static Color CARD_HEADER_BG = new Color(250, 251, 254);

    // --- Text Hierarchy ---
    public static Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static Color TEXT_SECONDARY = new Color(71, 85, 105);
    public static Color TEXT_MUTED = new Color(148, 163, 184);

    // --- Primary Brand Accent ---
    public static Color PRIMARY = new Color(79, 70, 229);         // vibrant indigo
    public static Color PRIMARY_HOVER = new Color(67, 56, 202);
    public static Color PRIMARY_LIGHT = new Color(238, 242, 255); // soft indigo tint

    // --- Status / Feedback Colors ---
    public static Color SUCCESS = new Color(16, 185, 129);
    public static Color SUCCESS_LIGHT = new Color(236, 253, 245);
    public static Color SUCCESS_TEXT = new Color(5, 150, 105);

    public static Color WARNING = new Color(245, 158, 11);
    public static Color WARNING_LIGHT = new Color(254, 243, 199);
    public static Color WARNING_TEXT = new Color(180, 83, 9);

    public static Color DANGER = new Color(239, 68, 68);
    public static Color DANGER_LIGHT = new Color(254, 242, 242);
    public static Color DANGER_TEXT = new Color(220, 38, 38);

    // --- Input & Table Colors ---
    public static Color INPUT_BORDER = new Color(203, 213, 225);
    public static Color INPUT_FOCUS = PRIMARY;
    public static Color TABLE_HEADER_BG = new Color(248, 250, 252);
    public static Color TABLE_ROW_ALT = new Color(250, 251, 254);
    public static Color TABLE_SELECT_BG = new Color(238, 242, 255);
    public static Color TABLE_SELECT_BORDER = new Color(199, 210, 254);

    // --- Extra tokens (tiles, progress track, chart bars, button hovers) ---
    public static Color TILE_BG = new Color(248, 250, 252);
    public static Color TILE_HOVER = new Color(241, 245, 249);
    public static Color TRACK_BG = new Color(226, 232, 240);
    public static Color GRID = new Color(241, 245, 249);
    public static Color BAR_MUTED = new Color(199, 210, 254);
    public static Color BAR_DANGER_MUTED = new Color(252, 165, 165);
    public static Color ACCENT_HOVER = new Color(224, 231, 255);
    public static Color DANGER_HOVER = new Color(254, 226, 226);
    public static Color SUCCESS_HOVER = new Color(209, 250, 229);

    private static boolean dark = false;

    public static boolean isDark() {
        return dark;
    }

    private static Color rgb(int r, int g, int b) {
        return new Color(r, g, b);
    }

    /** Mixes two colours: t = 0 gives base, t = 1 gives over. */
    private static Color blend(Color base, Color over, double t) {
        return new Color(
                (int) Math.round(base.getRed() * (1 - t) + over.getRed() * t),
                (int) Math.round(base.getGreen() * (1 - t) + over.getGreen() * t),
                (int) Math.round(base.getBlue() * (1 - t) + over.getBlue() * t));
    }

    /** Switches every colour in the app to the light or the dark palette. Rebuild the window afterwards. */
    public static void apply(boolean useDark) {
        dark = useDark;
        if (!useDark) {
            APP_BG = rgb(245, 247, 251);
            CARD_BG = rgb(255, 255, 255);
            CARD_BORDER = rgb(226, 232, 240);
            CARD_HEADER_BG = rgb(250, 251, 254);
            TEXT_PRIMARY = rgb(15, 23, 42);
            TEXT_SECONDARY = rgb(71, 85, 105);
            TEXT_MUTED = rgb(148, 163, 184);
            PRIMARY = rgb(79, 70, 229);
            PRIMARY_HOVER = rgb(67, 56, 202);
            PRIMARY_LIGHT = rgb(238, 242, 255);
            SUCCESS = rgb(16, 185, 129);
            SUCCESS_LIGHT = rgb(236, 253, 245);
            SUCCESS_TEXT = rgb(5, 150, 105);
            WARNING = rgb(245, 158, 11);
            WARNING_LIGHT = rgb(254, 243, 199);
            WARNING_TEXT = rgb(180, 83, 9);
            DANGER = rgb(239, 68, 68);
            DANGER_LIGHT = rgb(254, 242, 242);
            DANGER_TEXT = rgb(220, 38, 38);
            INPUT_BORDER = rgb(203, 213, 225);
            TABLE_HEADER_BG = rgb(248, 250, 252);
            TABLE_ROW_ALT = rgb(250, 251, 254);
            TABLE_SELECT_BG = rgb(238, 242, 255);
            TABLE_SELECT_BORDER = rgb(199, 210, 254);
            TILE_BG = rgb(248, 250, 252);
            TILE_HOVER = rgb(241, 245, 249);
            TRACK_BG = rgb(226, 232, 240);
            GRID = rgb(241, 245, 249);
            BAR_MUTED = rgb(199, 210, 254);
            BAR_DANGER_MUTED = rgb(252, 165, 165);
            ACCENT_HOVER = rgb(224, 231, 255);
            DANGER_HOVER = rgb(254, 226, 226);
            SUCCESS_HOVER = rgb(209, 250, 229);
        } else {
            APP_BG = rgb(15, 23, 42);
            CARD_BG = rgb(30, 41, 59);
            CARD_BORDER = rgb(51, 65, 85);
            CARD_HEADER_BG = rgb(30, 41, 59);
            TEXT_PRIMARY = rgb(241, 245, 249);
            TEXT_SECONDARY = rgb(203, 213, 225);
            TEXT_MUTED = rgb(148, 163, 184);
            PRIMARY = rgb(99, 102, 241);
            PRIMARY_HOVER = rgb(129, 140, 248);
            PRIMARY_LIGHT = rgb(49, 46, 129);
            SUCCESS = rgb(16, 185, 129);
            SUCCESS_LIGHT = rgb(6, 78, 59);
            SUCCESS_TEXT = rgb(52, 211, 153);
            WARNING = rgb(245, 158, 11);
            WARNING_LIGHT = rgb(69, 47, 12);
            WARNING_TEXT = rgb(251, 191, 36);
            DANGER = rgb(239, 68, 68);
            DANGER_LIGHT = rgb(76, 29, 29);
            DANGER_TEXT = rgb(252, 129, 129);
            INPUT_BORDER = rgb(71, 85, 105);
            TABLE_HEADER_BG = rgb(23, 33, 50);
            TABLE_ROW_ALT = rgb(35, 47, 66);
            TABLE_SELECT_BG = rgb(49, 56, 110);
            TABLE_SELECT_BORDER = rgb(79, 70, 229);
            TILE_BG = rgb(23, 33, 50);
            TILE_HOVER = rgb(51, 65, 85);
            TRACK_BG = rgb(51, 65, 85);
            GRID = rgb(45, 57, 76);
            BAR_MUTED = rgb(67, 76, 150);
            BAR_DANGER_MUTED = rgb(133, 62, 62);
            ACCENT_HOVER = rgb(67, 56, 202);
            DANGER_HOVER = rgb(110, 40, 40);
            SUCCESS_HOVER = rgb(10, 100, 75);
        }
        INPUT_FOCUS = PRIMARY;
        rebuildCategoryStyles();
        updateDialogColors();
    }

    /** Pop-up dialogs (errors, budget alerts) follow the theme too. */
    private static void updateDialogColors() {
        javax.swing.UIManager.put("OptionPane.background", dark ? APP_BG : null);
        javax.swing.UIManager.put("Panel.background", dark ? APP_BG : null);
        javax.swing.UIManager.put("OptionPane.messageForeground", dark ? TEXT_PRIMARY : null);
    }

    // --- Category Color System (Badge Colors & Chart) ---
    public static class CategoryStyle {
        public final Color accent;
        public final Color background;
        public final Color text;

        public CategoryStyle(Color accent, Color background, Color text) {
            this.accent = accent;
            this.background = background;
            this.text = text;
        }
    }

    private static final Map<String, CategoryStyle> CATEGORY_STYLES = new HashMap<>();
    static {
        rebuildCategoryStyles();
    }

    private static void rebuildCategoryStyles() {
        String[] names = {"Food", "Travel", "Shopping", "Bills", "Health", "Entertainment", "Other"};
        int[][] accent = {{249, 115, 22}, {2, 132, 199}, {139, 92, 246}, {217, 119, 6},
                {16, 185, 129}, {219, 39, 119}, {100, 116, 139}};
        int[][] lightBg = {{255, 247, 237}, {240, 249, 255}, {245, 243, 255}, {254, 243, 199},
                {236, 253, 245}, {253, 242, 248}, {241, 245, 249}};
        int[][] lightText = {{194, 65, 12}, {3, 105, 161}, {109, 40, 217}, {180, 83, 9},
                {5, 150, 105}, {190, 24, 93}, {51, 65, 85}};
        CATEGORY_STYLES.clear();
        for (int i = 0; i < names.length; i++) {
            Color ac = rgb(accent[i][0], accent[i][1], accent[i][2]);
            Color bg = rgb(lightBg[i][0], lightBg[i][1], lightBg[i][2]);
            Color tx = rgb(lightText[i][0], lightText[i][1], lightText[i][2]);
            if (dark) {
                bg = blend(CARD_BG, ac, 0.22);
                tx = blend(ac, Color.WHITE, 0.55);
            }
            CATEGORY_STYLES.put(names[i], new CategoryStyle(ac, bg, tx));
        }
    }

    public static CategoryStyle getCategoryStyle(String category) {
        CategoryStyle style = CATEGORY_STYLES.get(category);
        if (style == null) {
            style = CATEGORY_STYLES.get("Other");
        }
        return style;
    }

    // --- Typography Helpers ---
    private static final String FONT_FAMILY;
    static {
        String[] preferredFonts = {"Segoe UI", "Inter", "San Francisco", "Helvetica Neue", "Ubuntu", "Arial"};
        String chosen = Font.SANS_SERIF;
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.util.Set<String> available = new java.util.HashSet<>(java.util.Arrays.asList(ge.getAvailableFontFamilyNames()));
        for (String f : preferredFonts) {
            if (available.contains(f)) {
                chosen = f;
                break;
            }
        }
        FONT_FAMILY = chosen;
    }

    public static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, Math.round(size));
    }

    public static Font regular(float size) {
        return font(Font.PLAIN, size);
    }

    public static Font bold(float size) {
        return font(Font.BOLD, size);
    }

    // --- Modern Card Panel ---
    public static JPanel createCard(int cornerRadius) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Card background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

                // Subtle border
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    public static JPanel createCard() {
        return createCard(14);
    }

    // --- Modern Rounded Styled Buttons ---
    public static JButton createButton(String text, Color bg, Color hoverBg, Color textColor, boolean isPrimary) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                Color currentBg = hover ? hoverBg : bg;
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, w, h, 10, 10);

                if (!isPrimary) {
                    g2.setColor(hover ? (dark ? hoverBg.brighter() : hoverBg.darker()) : CARD_BORDER);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setForeground(textColor);
        btn.setFont(bold(13f));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY, PRIMARY_HOVER, Color.WHITE, true);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, TILE_BG, TILE_HOVER, TEXT_PRIMARY, false);
    }

    public static JButton createAccentButton(String text) {
        return createButton(text, PRIMARY_LIGHT, ACCENT_HOVER, dark ? TEXT_PRIMARY : PRIMARY, false);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER_LIGHT, DANGER_HOVER, DANGER_TEXT, false);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS_LIGHT, SUCCESS_HOVER, SUCCESS_TEXT, false);
    }

    // --- Modern Styled Input Field ---
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns) {
            private boolean focused = false;
            {
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        repaint();
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        focused = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (focused) {
                    g2.setColor(INPUT_FOCUS);
                    g2.setStroke(new BasicStroke(1.75f));
                } else {
                    g2.setColor(INPUT_BORDER);
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setFont(regular(13f));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(PRIMARY);
        field.setBorder(new EmptyBorder(7, 10, 7, 10));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
        return field;
    }

    // --- Modern Styled ComboBox ---
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        styleComboBox(box);
        return box;
    }

    public static <T> JComboBox<T> createComboBox() {
        JComboBox<T> box = new JComboBox<>();
        styleComboBox(box);
        return box;
    }

    private static <T> void styleComboBox(JComboBox<T> box) {
        box.setUI(new ThemedComboUI());
        box.setFont(regular(13f));
        box.setBackground(CARD_BG);
        box.setForeground(TEXT_PRIMARY);
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(0, 0, 0, 0));
        box.setRenderer(new ThemedComboRenderer());
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 34));
        box.setFocusable(false);
    }

    /** Drop-down that looks the same on every system and follows the light/dark theme. */
    private static class ThemedComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                public void paint(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(TEXT_SECONDARY);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.fillPolygon(new int[]{cx - 4, cx + 4, cx}, new int[]{cy - 2, cy - 2, cy + 3}, 3);
                    g2.dispose();
                }
            };
            b.setBorder(new EmptyBorder(0, 0, 0, 0));
            b.setContentAreaFilled(false);
            b.setFocusable(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(26, 24));
            return b;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
            // the rounded background is painted in paint()
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 8, 8);
            g2.setColor(INPUT_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 8, 8);
            g2.dispose();
            super.paint(g, c);
        }

        @Override
        protected javax.swing.plaf.basic.ComboPopup createPopup() {
            javax.swing.plaf.basic.BasicComboPopup popup = new javax.swing.plaf.basic.BasicComboPopup(comboBox) {
                @Override
                protected javax.swing.JScrollPane createScroller() {
                    javax.swing.JScrollPane sp = super.createScroller();
                    styleScrollPane(sp);
                    return sp;
                }
            };
            popup.setBorder(BorderFactory.createLineBorder(INPUT_BORDER));
            return popup;
        }
    }

    private static class ThemedComboRenderer extends javax.swing.DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, false);
            setFont(regular(13f));
            setForeground(TEXT_PRIMARY);
            if (index < 0) {
                setOpaque(false);
                setBorder(new EmptyBorder(0, 8, 0, 4));
            } else {
                setOpaque(true);
                setBackground(isSelected ? TABLE_SELECT_BG : CARD_BG);
                setBorder(new EmptyBorder(5, 8, 5, 8));
            }
            return this;
        }
    }

    // --- Slim scroll bars that follow the theme ---
    public static void styleScrollPane(javax.swing.JScrollPane sp) {
        for (javax.swing.JScrollBar bar : new javax.swing.JScrollBar[]{sp.getVerticalScrollBar(), sp.getHorizontalScrollBar()}) {
            if (bar != null) {
                bar.setUI(new SlimScrollBarUI());
                bar.setOpaque(true);
            }
        }
        JPanel corner = new JPanel();
        corner.setBackground(TILE_BG);
        sp.setCorner(javax.swing.JScrollPane.LOWER_RIGHT_CORNER, corner);
    }

    private static class SlimScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(12, 12);
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) {
            g.setColor(TILE_BG);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() || isDragging ? TEXT_MUTED : INPUT_BORDER);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // --- Table header that follows the theme on every system ---
    private static class ThemedHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();
            if (table.getRowSorter() != null) {
                for (javax.swing.RowSorter.SortKey key : table.getRowSorter().getSortKeys()) {
                    if (key.getColumn() == table.convertColumnIndexToModel(column)
                            && key.getSortOrder() != javax.swing.SortOrder.UNSORTED) {
                        text += key.getSortOrder() == javax.swing.SortOrder.ASCENDING ? "  \u25B2" : "  \u25BC";
                    }
                }
            }
            super.getTableCellRendererComponent(table, text, false, false, row, column);
            setFont(bold(12f));
            setForeground(TEXT_SECONDARY);
            setBackground(TABLE_HEADER_BG);
            setOpaque(true);
            setHorizontalAlignment(JLabel.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                    new EmptyBorder(0, 4, 0, 4)));
            return this;
        }
    }

    // --- Card Header Label ---
    public static JLabel createCardTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(bold(15f));
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(4, 2, 8, 2));
        return label;
    }

    // --- Pill Badge for Table Categories ---
    public static class CategoryPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String cat = value != null ? value.toString() : "";
            CategoryStyle style = getCategoryStyle(cat);

            JPanel pill = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Row background
                    if (isSelected) {
                        g2.setColor(TABLE_SELECT_BG);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    } else {
                        g2.setColor(row % 2 == 1 ? TABLE_ROW_ALT : CARD_BG);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }

                    // Pill badge
                    int pillH = 22;
                    int pillY = (getHeight() - pillH) / 2;
                    int dotSize = 8;
                    int dotX = 14;
                    int dotY = (getHeight() - dotSize) / 2;

                    g2.setFont(bold(11.5f));
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int textW = fm.stringWidth(cat);
                    int pillW = textW + 30;

                    // Pill background
                    g2.setColor(style.background);
                    g2.fillRoundRect(8, pillY, pillW, pillH, pillH, pillH);

                    // Pill border
                    g2.setColor(new Color(style.accent.getRed(), style.accent.getGreen(), style.accent.getBlue(), 60));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(8, pillY, pillW, pillH, pillH, pillH);

                    // Colored dot indicator
                    g2.setColor(style.accent);
                    g2.fillOval(dotX, dotY, dotSize, dotSize);

                    // Pill text
                    g2.setColor(style.text);
                    int textY = pillY + ((pillH - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(cat, dotX + dotSize + 6, textY);

                    g2.dispose();
                }
            };
            pill.setOpaque(true);
            return pill;
        }
    }

    // --- Style JTable with Modern SaaS Look ---
    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(GRID);
        table.setBackground(CARD_BG);
        table.setSelectionBackground(TABLE_SELECT_BG);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFont(regular(13f));
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(bold(12f));
        header.setForeground(TEXT_SECONDARY);
        header.setBackground(TABLE_HEADER_BG);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        header.setDefaultRenderer(new ThemedHeaderRenderer());
    }
}
