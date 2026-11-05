package expensetracker;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Modern, attractive main window for Expense Tracker.
 * Professional fintech theme with rounded cards, clean typography,
 * category pill badges, custom donut chart, and vibrant status indicators.
 */
public class MainFrame extends JFrame {

    private static final String ALL_MONTHS = "All months";
    private static final String ALL_CATEGORIES = "All categories";

    private final ExpenseStore store;
    private final List<Expense> visible = new ArrayList<>();
    private Expense editing = null;
    private Expense lastDeleted = null;
    private boolean updatingFilters = false;

    // Filter bar components
    private JTextField budgetBox;
    private JTextField searchBox;
    private JComboBox<String> monthFilter;
    private JComboBox<String> categoryFilter;

    // Form components
    private JPanel formCard;
    private JLabel formTitle;
    private JLabel formSubtitle;
    private JTextField amountBox;
    private JTextField dateBox;
    private JTextField descBox;
    private JComboBox<String> categoryBox;
    private JButton saveButton;
    private JButton cancelEditButton;

    // Table components
    private JTable table;
    private DefaultTableModel model;
    private JLabel tableCountBadge;

    // Summary components
    private JLabel budgetValLabel;
    private JLabel spentValLabel;
    private JLabel remainingValLabel;
    private JLabel remainingTitleLabel;
    private JLabel usageLabel;
    private ModernProgressBar usageBar;
    private JLabel topCategoryLabel;
    private ChartPanel chart;

    // Currency-dependent labels and charts
    private JLabel budgetLbl;
    private JLabel amountLabel;
    private JLabel lastFormLabel;
    private JLabel logoLabel;
    private JComboBox<String> currencyBox;
    private MonthlyBarChart barChart;
    private JComboBox<String> chartSwitch;
    private ToggleSwitch themeSwitch;
    private boolean updatingCurrency = false;

    // Status bar
    private JLabel statusLabel;

    public MainFrame(ExpenseStore store) {
        this.store = store;

        setTitle("Expense Tracker");
        java.awt.Rectangle screen = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1100, screen.width - 60), Math.min(680, screen.height - 80));
        setMinimumSize(new Dimension(520, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UITheme.apply(store.isDarkMode());
        buildUi();
    }

    /** Builds the whole window content. It is called again when the theme changes. */
    private void buildUi() {
        getContentPane().setBackground(UITheme.APP_BG);

        ScrollRoot root = new ScrollRoot(new BorderLayout(10, 10));
        root.setBackground(UITheme.APP_BG);
        root.setBorder(new EmptyBorder(10, 14, 8, 14));

        root.add(buildTopHeaderAndFilters(), BorderLayout.NORTH);

        JPanel centerArea = new JPanel(new BorderLayout(14, 0));
        centerArea.setBackground(UITheme.APP_BG);
        centerArea.add(buildFormPanel(), BorderLayout.WEST);
        centerArea.add(buildTablePanel(), BorderLayout.CENTER);
        centerArea.add(buildSummaryPanel(), BorderLayout.EAST);
        root.add(centerArea, BorderLayout.CENTER);

        root.add(buildStatusBar(), BorderLayout.SOUTH);
        javax.swing.JScrollPane pageScroll = new javax.swing.JScrollPane(root);
        pageScroll.setBorder(null);
        pageScroll.getViewport().setBackground(UITheme.APP_BG);
        UITheme.styleScrollPane(pageScroll);
        pageScroll.getVerticalScrollBar().setUnitIncrement(18);
        pageScroll.getHorizontalScrollBar().setUnitIncrement(18);
        setContentPane(pageScroll);

        installShortcuts();
        applyCurrencyLabels();
        onDataChanged();
        javax.swing.SwingUtilities.invokeLater(() -> amountBox.requestFocusInWindow());
    }

    // ---------- Top Header & Filter Bar ----------

    private JPanel buildTopHeaderAndFilters() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // Header branding banner
        JPanel brandBar = new JPanel(new BorderLayout());
        brandBar.setOpaque(false);
        brandBar.setBorder(new EmptyBorder(0, 2, 8, 2));

        JPanel brandLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandLeft.setOpaque(false);

        JLabel logoBadge = new JLabel(" ₹ ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoLabel = logoBadge;
        logoBadge.setFont(UITheme.bold(16f));
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Expense Tracker");
        titleLabel.setFont(UITheme.bold(18f));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Smart Spending & Monthly Budget Manager");
        subtitleLabel.setFont(UITheme.regular(12f));
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(subtitleLabel);

        brandLeft.add(logoBadge);
        brandLeft.add(titleBlock);

        // Storage indicator pill on the right
        JLabel storagePill = new JLabel("● Storage: Local CSV") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(UITheme.CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        storagePill.setFont(UITheme.bold(11.5f));
        storagePill.setForeground(UITheme.SUCCESS_TEXT);
        storagePill.setBorder(new EmptyBorder(5, 12, 5, 12));

        brandBar.add(brandLeft, BorderLayout.WEST);
        JLabel currencyLbl = new JLabel("Currency:");
        currencyLbl.setFont(UITheme.bold(12.5f));
        currencyLbl.setForeground(UITheme.TEXT_SECONDARY);
        currencyBox = UITheme.createComboBox(ExpenseStore.CURRENCIES);
        currencyBox.setPreferredSize(new Dimension(72, 32));
        currencyBox.setSelectedItem(store.getCurrency());
        currencyBox.addActionListener(e -> changeCurrency());

        JLabel themeLbl = new JLabel("Dark mode");
        themeLbl.setFont(UITheme.bold(12.5f));
        themeLbl.setForeground(UITheme.TEXT_SECONDARY);
        themeSwitch = new ToggleSwitch(UITheme.isDark());
        themeSwitch.setToolTipText("Slide to switch between light and dark mode");
        themeSwitch.addActionListener(e -> switchTheme(themeSwitch.isSelected()));

        JPanel brandRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        brandRight.setOpaque(false);
        brandRight.add(themeLbl);
        brandRight.add(themeSwitch);
        brandRight.add(createVerticalSeparator());
        brandRight.add(currencyLbl);
        brandRight.add(currencyBox);
        brandRight.add(storagePill);
        brandBar.add(brandRight, BorderLayout.EAST);

        // Filter Bar Card
        JPanel filterCard = UITheme.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        filterCard.setBorder(new EmptyBorder(6, 12, 6, 12));

        // Budget section
        budgetLbl = new JLabel("Monthly Budget (" + cur() + "):");
        budgetLbl.setFont(UITheme.bold(12.5f));
        budgetLbl.setForeground(UITheme.TEXT_SECONDARY);
        filterCard.add(budgetLbl);

        budgetBox = UITheme.createTextField(7);
        budgetBox.setText(store.getMonthlyBudget() > 0 ? plain(store.getMonthlyBudget()) : "");
        budgetBox.addActionListener(e -> setBudget());
        filterCard.add(budgetBox);

        JButton setBudgetBtn = UITheme.createAccentButton("Set Budget");
        setBudgetBtn.addActionListener(e -> setBudget());
        filterCard.add(setBudgetBtn);

        // Divider
        filterCard.add(createVerticalSeparator());

        // Month Filter
        JLabel monthLbl = new JLabel("Month:");
        monthLbl.setFont(UITheme.bold(12.5f));
        monthLbl.setForeground(UITheme.TEXT_SECONDARY);
        filterCard.add(monthLbl);

        monthFilter = UITheme.createComboBox();
        monthFilter.setPreferredSize(new Dimension(125, 34));
        monthFilter.addActionListener(e -> {
            if (!updatingFilters) {
                refreshView();
            }
        });
        filterCard.add(monthFilter);

        // Category Filter
        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(UITheme.bold(12.5f));
        catLbl.setForeground(UITheme.TEXT_SECONDARY);
        filterCard.add(catLbl);

        categoryFilter = UITheme.createComboBox();
        categoryFilter.setPreferredSize(new Dimension(140, 34));
        categoryFilter.addItem(ALL_CATEGORIES);
        for (String c : ExpenseStore.CATEGORIES) {
            categoryFilter.addItem(c);
        }
        categoryFilter.addActionListener(e -> refreshView());
        filterCard.add(categoryFilter);

        // Divider
        filterCard.add(createVerticalSeparator());

        // Search Box
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.bold(12.5f));
        searchLbl.setForeground(UITheme.TEXT_SECONDARY);
        filterCard.add(searchLbl);

        searchBox = UITheme.createTextField(11);
        searchBox.setToolTipText("Search description, category or amount");
        searchBox.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refreshView(); }
            @Override
            public void removeUpdate(DocumentEvent e) { refreshView(); }
            @Override
            public void changedUpdate(DocumentEvent e) { refreshView(); }
        });
        filterCard.add(searchBox);

        container.add(brandBar);
        container.add(filterCard);
        return container;
    }

    private static Component createVerticalSeparator() {
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(UITheme.CARD_BORDER);
                g.drawLine(getWidth() / 2, 4, getWidth() / 2, getHeight() - 5);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(14, 28));
        return sep;
    }

    // ---------- Left Panel: Add / Edit Form ----------

    private JPanel buildFormPanel() {
        formCard = UITheme.createCard();
        formCard.setLayout(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(260, 420));
        formCard.setBorder(new EmptyBorder(12, 18, 12, 18));

        int row = 0;

        // Header block
        formTitle = new JLabel("Add Expense");
        formTitle.setFont(UITheme.bold(16f));
        formTitle.setForeground(UITheme.TEXT_PRIMARY);

        formSubtitle = new JLabel("Record a new spending entry");
        formSubtitle.setFont(UITheme.regular(12f));
        formSubtitle.setForeground(UITheme.TEXT_MUTED);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 2, 0);
        formCard.add(formTitle, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 8, 0);
        formCard.add(formSubtitle, c);

        // Fields
        amountBox = UITheme.createTextField(14);
        categoryBox = UITheme.createComboBox(ExpenseStore.CATEGORIES);
        dateBox = UITheme.createTextField(10);
        dateBox.setText(LocalDate.now().toString());
        descBox = UITheme.createTextField(14);

        JButton todayButton = UITheme.createSecondaryButton("Today");
        todayButton.setPreferredSize(new Dimension(74, 34));
        todayButton.addActionListener(e -> dateBox.setText(LocalDate.now().toString()));

        JPanel dateRow = new JPanel(new BorderLayout(8, 0));
        dateRow.setOpaque(false);
        dateRow.add(dateBox, BorderLayout.CENTER);
        dateRow.add(todayButton, BorderLayout.EAST);

        row = addFormField(row, "Amount (" + cur() + ") *", amountBox);
        amountLabel = lastFormLabel;
        row = addFormField(row, "Category *", categoryBox);
        row = addFormField(row, "Date (yyyy-mm-dd) *", dateRow);
        row = addFormField(row, "Description (optional)", descBox);

        saveButton = UITheme.createPrimaryButton("+ Add Expense");
        saveButton.setPreferredSize(new Dimension(220, 38));
        saveButton.addActionListener(e -> submitExpense());
        descBox.addActionListener(e -> submitExpense());
        amountBox.addActionListener(e -> submitExpense());

        cancelEditButton = UITheme.createSecondaryButton("Cancel Edit");
        cancelEditButton.setPreferredSize(new Dimension(220, 34));
        cancelEditButton.addActionListener(e -> cancelEdit());
        cancelEditButton.setVisible(false);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 6, 0);
        formCard.add(saveButton, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        formCard.add(cancelEditButton, c);

        // Spacer to push everything to top
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.weighty = 1;
        formCard.add(Box.createGlue(), c);

        return formCard;
    }

    private int addFormField(int row, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.bold(12f));
        label.setForeground(UITheme.TEXT_SECONDARY);
        lastFormLabel = label;

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 3, 0);
        formCard.add(label, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 5, 0);
        formCard.add(field, c);
        return row;
    }

    // ---------- Center Panel: Table ----------

    private JPanel buildTablePanel() {
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(16, 16, 14, 16));

        // Top bar of table
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("Expenses History");
        tableTitle.setFont(UITheme.bold(16f));
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);

        tableCountBadge = new JLabel("0 expenses") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tableCountBadge.setFont(UITheme.bold(11.5f));
        tableCountBadge.setForeground(UITheme.PRIMARY);
        tableCountBadge.setBorder(new EmptyBorder(3, 10, 3, 10));

        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(tableCountBadge, BorderLayout.EAST);
        tableCard.add(tableHeaderPanel, BorderLayout.NORTH);

        // Table definition
        model = new DefaultTableModel(new String[]{"Date", "Category", "Description", "Amount (" + cur() + ")"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 3 ? Double.class : String.class;
            }
        };

        table = new JTable(model) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0) {
                    String line1 = store.getAll().isEmpty() ? "No expenses yet" : "No expenses match these filters";
                    String line2 = store.getAll().isEmpty() ? "Add your first one using the form on the left"
                            : "Try another month, category or search";
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    int cx = getWidth() / 2;
                    int cy = Math.max(getHeight(), 200) / 2;
                    g2.setFont(UITheme.bold(15f));
                    g2.setColor(UITheme.TEXT_SECONDARY);
                    g2.drawString(line1, cx - g2.getFontMetrics().stringWidth(line1) / 2, cy - 6);
                    g2.setFont(UITheme.regular(12.5f));
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.drawString(line2, cx - g2.getFontMetrics().stringWidth(line2) / 2, cy + 16);
                    g2.dispose();
                }
            }
        };
        UITheme.styleTable(table);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(new TableRowSorter<>(model));

        // Column widths & renderers
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(0).setMinWidth(105);
        table.getColumnModel().getColumn(1).setPreferredWidth(125);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setMinWidth(120);

        table.getColumnModel().getColumn(1).setCellRenderer(new UITheme.CategoryPillRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new ModernAmountRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setFont(UITheme.regular(12.5f));
                setForeground(UITheme.TEXT_SECONDARY);
                setBackground(isSelected ? UITheme.TABLE_SELECT_BG : (row % 2 == 1 ? UITheme.TABLE_ROW_ALT : UITheme.CARD_BG));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setFont(UITheme.regular(13f));
                setForeground(UITheme.TEXT_PRIMARY);
                setBackground(isSelected ? UITheme.TABLE_SELECT_BG : (row % 2 == 1 ? UITheme.TABLE_ROW_ALT : UITheme.CARD_BG));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    startEdit();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);
        UITheme.styleScrollPane(scrollPane);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons Row
        JButton editButton = UITheme.createAccentButton("✏ Edit Selected");
        editButton.addActionListener(e -> startEdit());

        JButton deleteButton = UITheme.createDangerButton("🗑 Delete");
        deleteButton.addActionListener(e -> deleteSelected());

        JButton undoButton = UITheme.createSecondaryButton("↶ Undo Delete");
        undoButton.addActionListener(e -> undoDelete());

        JButton exportButton = UITheme.createSuccessButton("⤓ Export CSV");
        exportButton.addActionListener(e -> exportCsv());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(4, 0, 0, 0));
        buttons.add(editButton);
        buttons.add(deleteButton);
        buttons.add(undoButton);
        buttons.add(exportButton);

        tableCard.add(buttons, BorderLayout.SOUTH);
        return tableCard;
    }

    // ---------- Right Panel: Summary & Donut Chart ----------

    private JPanel buildSummaryPanel() {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setPreferredSize(new Dimension(310, 420));
        column.setOpaque(false);

        // Top Summary Card (Metrics & Progress)
        JPanel summaryCard = UITheme.createCard();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel sumTitle = new JLabel("Monthly Overview");
        sumTitle.setFont(UITheme.bold(15f));
        sumTitle.setForeground(UITheme.TEXT_PRIMARY);
        sumTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCard.add(sumTitle);
        summaryCard.add(Box.createVerticalStrut(10));

        // Metric Tiles Row 1: Budget and Spent
        JPanel metricsGrid = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        metricsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        metricsGrid.setOpaque(false);
        metricsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel budgetTile = createMetricTile("BUDGET", cur() + "0.00", UITheme.PRIMARY);
        budgetValLabel = (JLabel) budgetTile.getComponent(2);

        JPanel spentTile = createMetricTile("SPENT", cur() + "0.00", UITheme.TEXT_PRIMARY);
        spentValLabel = (JLabel) spentTile.getComponent(2);

        metricsGrid.add(budgetTile);
        metricsGrid.add(spentTile);
        summaryCard.add(metricsGrid);
        summaryCard.add(Box.createVerticalStrut(8));

        // Remaining Balance Tile (Full width)
        JPanel remainingTile = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        remainingTile.setBackground(UITheme.SUCCESS_LIGHT);
        remainingTile.setBorder(new EmptyBorder(8, 12, 8, 12));
        remainingTile.setAlignmentX(Component.LEFT_ALIGNMENT);
        remainingTile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        remainingTitleLabel = new JLabel("REMAINING BUDGET");
        remainingTitleLabel.setFont(UITheme.bold(10.5f));
        remainingTitleLabel.setForeground(UITheme.SUCCESS_TEXT);

        remainingValLabel = new JLabel(cur() + "0.00");
        remainingValLabel.setFont(UITheme.bold(17f));
        remainingValLabel.setForeground(UITheme.SUCCESS_TEXT);

        remainingTile.add(remainingTitleLabel, BorderLayout.NORTH);
        remainingTile.add(remainingValLabel, BorderLayout.CENTER);
        summaryCard.add(remainingTile);
        summaryCard.add(Box.createVerticalStrut(10));

        // Progress bar
        usageBar = new ModernProgressBar();
        usageBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        usageBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        summaryCard.add(usageBar);
        summaryCard.add(Box.createVerticalStrut(6));

        usageLabel = new JLabel("Budget tracking works one month at a time");
        usageLabel.setFont(UITheme.regular(11.5f));
        usageLabel.setForeground(UITheme.TEXT_SECONDARY);
        usageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCard.add(usageLabel);
        summaryCard.add(Box.createVerticalStrut(6));

        topCategoryLabel = new JLabel("No expenses in this month yet");
        topCategoryLabel.setFont(UITheme.bold(11.5f));
        topCategoryLabel.setForeground(UITheme.TEXT_MUTED);
        topCategoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCard.add(topCategoryLabel);

        // Bottom Chart Card
        JPanel chartCard = UITheme.createCard();
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel chartTitle = new JLabel("Breakdown");
        chartTitle.setFont(UITheme.bold(15f));
        chartTitle.setForeground(UITheme.TEXT_PRIMARY);

        String[] chartViews = {"By category", "By month"};
        chartSwitch = UITheme.createComboBox(chartViews);
        chartSwitch.setPreferredSize(new Dimension(122, 30));

        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);
        chartHeader.setBorder(new EmptyBorder(0, 2, 4, 0));
        chartHeader.add(chartTitle, BorderLayout.WEST);
        chartHeader.add(chartSwitch, BorderLayout.EAST);

        chart = new ChartPanel();
        barChart = new MonthlyBarChart();

        java.awt.CardLayout chartCards = new java.awt.CardLayout();
        JPanel chartHolder = new JPanel(chartCards);
        chartHolder.setOpaque(false);
        chartHolder.add(chart, "category");
        chartHolder.add(barChart, "month");
        chartSwitch.addActionListener(e ->
                chartCards.show(chartHolder, chartSwitch.getSelectedIndex() == 0 ? "category" : "month"));

        chartCard.add(chartHeader, BorderLayout.NORTH);
        chartCard.add(chartHolder, BorderLayout.CENTER);

        summaryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, summaryCard.getPreferredSize().height));
        summaryCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(summaryCard);
        column.add(Box.createVerticalStrut(12));
        column.add(chartCard);
        return column;
    }

    private JPanel createMetricTile(String title, String initialVal, Color accent) {
        JPanel tile = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.TILE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.CARD_BORDER);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setOpaque(false);
        tile.setPreferredSize(new Dimension(110, 56));
        tile.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.bold(10f));
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel valLbl = new JLabel(initialVal);
        valLbl.setFont(UITheme.bold(14.5f));
        valLbl.setForeground(accent);

        tile.add(titleLbl);
        tile.add(Box.createVerticalStrut(2));
        tile.add(valLbl);
        return tile;
    }

    // ---------- Bottom Status Bar ----------

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(6, 4, 2, 4));

        statusLabel = new JLabel("● Ready. Data saved automatically in " + store.getDir());
        statusLabel.setFont(UITheme.regular(12f));
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel hintLabel = new JLabel("Double-click a row to edit • Ctrl+Z to undo");
        hintLabel.setFont(UITheme.regular(11.5f));
        hintLabel.setForeground(UITheme.TEXT_MUTED);

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(hintLabel, BorderLayout.EAST);
        return bar;
    }

    private void installShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undoDelete");
        getRootPane().getActionMap().put("undoDelete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoDelete();
            }
        });

        table.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRow");
        table.getActionMap().put("deleteRow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
    }

    // ---------- Actions ----------

    private void setBudget() {
        String text = budgetBox.getText().trim().replace(",", "");
        if (text.isEmpty()) {
            store.setMonthlyBudget(0);
            persist();
            status("Monthly budget cleared.");
            refreshView();
            return;
        }
        Double value = parseNumber(text);
        if (value == null || value < 0) {
            showError("Enter the budget as a number, for example 15000.");
            return;
        }
        store.setMonthlyBudget(value);
        persist();
        status("Monthly budget set to " + cur() + money(value) + ".");
        refreshView();
        String m = selectedMonth();
        if (m != null) {
            checkBudgetAlert(m, 0);
        }
    }

    private void submitExpense() {
        Double amount = parseNumber(amountBox.getText().trim().replace(",", ""));
        if (amount == null) {
            showError("Enter the amount as a number, for example 250.");
            return;
        }
        if (amount <= 0 || amount > 1_000_000_000) {
            showError("Amount must be greater than zero and below 1,000,000,000.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateBox.getText().trim());
        } catch (DateTimeParseException ex) {
            showError("Enter the date as yyyy-mm-dd, for example " + LocalDate.now() + ".");
            return;
        }

        String description = descBox.getText().trim();
        if (description.length() > 100) {
            showError("Keep the description under 100 characters.");
            return;
        }

        Expense expense = new Expense(date, categoryBox.getSelectedItem().toString(), description, amount);
        double spentBefore = spentInMonth(expense.getMonth());
        boolean wasEditing = editing != null;
        if (wasEditing) {
            store.replace(editing, expense);
        } else {
            store.add(expense);
        }
        persist();

        endEditMode();
        amountBox.setText("");
        descBox.setText("");
        onDataChanged();
        showMonth(expense.getMonth());
        status((wasEditing ? "Saved changes: " : "Added: ") + describe(expense));
        checkBudgetAlert(expense.getMonth(), spentBefore);
        amountBox.requestFocusInWindow();
    }

    private void startEdit() {
        Expense selected = selectedExpense();
        if (selected == null) {
            showError("Select an expense in the table first.");
            return;
        }
        editing = selected;
        amountBox.setText(plain(selected.getAmount()));
        categoryBox.setSelectedItem(selected.getCategory());
        dateBox.setText(selected.getDate().toString());
        descBox.setText(selected.getDescription());

        showEditModeUi(selected);

        formCard.repaint();
        amountBox.requestFocusInWindow();
        status("Editing: " + describe(selected));
    }

    private void showEditModeUi(Expense selected) {
        formTitle.setText("Edit Expense");
        formSubtitle.setText("Updating: " + describe(selected));
        formTitle.setForeground(UITheme.PRIMARY);
        saveButton.setText("✓ Save Changes");
        cancelEditButton.setVisible(true);
    }

    private void cancelEdit() {
        endEditMode();
        amountBox.setText("");
        descBox.setText("");
        status("Edit cancelled.");
    }

    private void endEditMode() {
        editing = null;
        formTitle.setText("Add Expense");
        formSubtitle.setText("Record a new spending entry");
        formTitle.setForeground(UITheme.TEXT_PRIMARY);
        saveButton.setText("+ Add Expense");
        cancelEditButton.setVisible(false);
        formCard.repaint();
    }

    private void deleteSelected() {
        Expense selected = selectedExpense();
        if (selected == null) {
            showError("Select an expense in the table first.");
            return;
        }
        if (selected == editing) {
            endEditMode();
        }
        store.remove(selected);
        lastDeleted = selected;
        persist();
        onDataChanged();
        status("Deleted: " + describe(selected) + ". Press Ctrl+Z or click Undo Delete to bring it back.");
    }

    private void undoDelete() {
        if (lastDeleted == null) {
            status("Nothing to undo.");
            return;
        }
        Expense restored = lastDeleted;
        lastDeleted = null;
        double spentBefore = spentInMonth(restored.getMonth());
        store.add(restored);
        persist();
        onDataChanged();
        showMonth(restored.getMonth());
        status("Restored: " + describe(restored));
        checkBudgetAlert(restored.getMonth(), spentBefore);
    }

    private void exportCsv() {
        if (visible.isEmpty()) {
            showError("There are no expenses in this view to export.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("expenses.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (PrintWriter out = new PrintWriter(chooser.getSelectedFile(), "UTF-8")) {
            out.println(ExpenseStore.CSV_HEADER);
            for (Expense e : visible) {
                out.println(ExpenseStore.toCsvLine(e, true));
            }
            status("Exported " + visible.size() + " expenses to " + chooser.getSelectedFile().getName());
        } catch (IOException ex) {
            showError("Could not save the file: " + ex.getMessage());
        }
    }

    // ---------- Refreshing the Screen ----------

    private void onDataChanged() {
        refreshMonthChoices();
        refreshView();
    }

    private void refreshMonthChoices() {
        Object previous = monthFilter.getSelectedItem();
        Set<String> months = new TreeSet<>(Comparator.reverseOrder());
        months.addAll(store.months());
        months.add(YearMonth.now().toString());

        updatingFilters = true;
        monthFilter.removeAllItems();
        monthFilter.addItem(ALL_MONTHS);
        for (String m : months) {
            monthFilter.addItem(m);
        }
        if (previous != null && (ALL_MONTHS.equals(previous) || months.contains(previous))) {
            monthFilter.setSelectedItem(previous);
        } else {
            monthFilter.setSelectedItem(YearMonth.now().toString());
        }
        updatingFilters = false;
    }

    private void showMonth(String month) {
        if (!ALL_MONTHS.equals(monthFilter.getSelectedItem())) {
            monthFilter.setSelectedItem(month);
        }
    }

    private void refreshView() {
        String month = selectedMonth();
        List<Expense> inMonth = new ArrayList<>();
        for (Expense e : store.getAll()) {
            if (month == null || month.equals(e.getMonth())) {
                inMonth.add(e);
            }
        }

        String category = (String) categoryFilter.getSelectedItem();
        String query = searchBox.getText().trim().toLowerCase(Locale.ROOT);
        visible.clear();
        for (Expense e : inMonth) {
            boolean categoryOk = category == null || ALL_CATEGORIES.equals(category)
                    || category.equals(e.getCategory());
            boolean searchOk = query.isEmpty()
                    || e.getDescription().toLowerCase(Locale.ROOT).contains(query)
                    || e.getCategory().toLowerCase(Locale.ROOT).contains(query)
                    || plain(e.getAmount()).contains(query);
            if (categoryOk && searchOk) {
                visible.add(e);
            }
        }
        visible.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        table.getRowSorter().setSortKeys(null);
        model.setRowCount(0);
        for (Expense e : visible) {
            model.addRow(new Object[]{e.getDate().toString(), e.getCategory(), e.getDescription(), e.getAmount()});
        }

        tableCountBadge.setText(visible.size() + " of " + inMonth.size() + " expenses");
        updateSummary(month, inMonth);
    }

    private void updateSummary(String month, List<Expense> inMonth) {
        double budget = store.getMonthlyBudget();
        double spent = ExpenseStore.total(inMonth);

        budgetValLabel.setText(budget > 0 ? cur() + money(budget) : "Not Set");
        spentValLabel.setText(cur() + money(spent));

        int percent = 0;
        if (month == null) {
            remainingTitleLabel.setText("COMPARING ALL MONTHS");
            remainingValLabel.setText("Select month");
            setRemainingStatus(UITheme.TEXT_SECONDARY, UITheme.APP_BG);
            usageLabel.setText("Select a specific month to track budget");
        } else if (budget <= 0) {
            remainingTitleLabel.setText("MONTHLY BUDGET");
            remainingValLabel.setText("Not Set");
            setRemainingStatus(UITheme.TEXT_MUTED, UITheme.APP_BG);
            usageLabel.setText("Enter budget at top and click Set Budget");
        } else {
            double remaining = budget - spent;
            percent = (int) Math.round(spent / budget * 100);
            if (remaining >= 0) {
                remainingTitleLabel.setText("REMAINING BUDGET");
                remainingValLabel.setText(cur() + money(remaining));
                boolean low = remaining <= budget * 0.2;
                if (low) {
                    setRemainingStatus(UITheme.WARNING_TEXT, UITheme.WARNING_LIGHT);
                } else {
                    setRemainingStatus(UITheme.SUCCESS_TEXT, UITheme.SUCCESS_LIGHT);
                }
            } else {
                remainingTitleLabel.setText("OVER BUDGET BY");
                remainingValLabel.setText(cur() + money(-remaining));
                setRemainingStatus(UITheme.DANGER_TEXT, UITheme.DANGER_LIGHT);
            }
            usageLabel.setText(percent + "% of the monthly budget spent");
        }
        usageBar.setValue(percent);

        Map<String, Double> totals = ExpenseStore.totalsByCategory(inMonth);
        if (totals.isEmpty()) {
            topCategoryLabel.setText("No expenses in this month yet");
            topCategoryLabel.setForeground(UITheme.TEXT_MUTED);
        } else {
            Map.Entry<String, Double> top = totals.entrySet().iterator().next();
            topCategoryLabel.setText("★ Top Category: " + top.getKey() + " (" + cur() + money(top.getValue()) + ")");
            topCategoryLabel.setForeground(UITheme.TEXT_PRIMARY);
        }
        chart.setData(totals);

        YearMonth end = month != null ? YearMonth.parse(month) : YearMonth.now();
        barChart.setData(ExpenseStore.totalsByMonth(store.getAll(), end, 6), month, budget, cur());
    }

    private void setRemainingStatus(Color textColor, Color bgColor) {
        remainingTitleLabel.setForeground(textColor);
        remainingValLabel.setForeground(textColor);
        Component parent = remainingValLabel.getParent();
        if (parent != null) {
            parent.setBackground(bgColor);
            parent.repaint();
        }
    }

    // ---------- Light / dark mode ----------

    private void switchTheme(boolean dark) {
        store.setDarkMode(dark);
        persist();
        // let the switch finish sliding, then rebuild the window in the new colours
        javax.swing.Timer later = new javax.swing.Timer(180, e -> rebuildForTheme());
        later.setRepeats(false);
        later.start();
    }

    private void rebuildForTheme() {
        String search = searchBox.getText();
        Object category = categoryFilter.getSelectedItem();
        Object month = monthFilter.getSelectedItem();
        String budgetText = budgetBox.getText();
        String amount = amountBox.getText();
        String date = dateBox.getText();
        String desc = descBox.getText();
        Object formCategory = categoryBox.getSelectedItem();
        int chartIndex = chartSwitch.getSelectedIndex();
        String statusText = statusLabel.getText();

        UITheme.apply(store.isDarkMode());
        buildUi();

        budgetBox.setText(budgetText);
        amountBox.setText(amount);
        dateBox.setText(date);
        descBox.setText(desc);
        categoryBox.setSelectedItem(formCategory);
        categoryFilter.setSelectedItem(category);
        monthFilter.setSelectedItem(month);
        searchBox.setText(search);
        chartSwitch.setSelectedIndex(chartIndex);
        statusLabel.setText(statusText);
        if (editing != null) {
            showEditModeUi(editing);
        }
        revalidate();
        repaint();
    }

    // ---------- Currency ----------

    private String cur() {
        return store.getCurrency();
    }

    private void changeCurrency() {
        if (updatingCurrency) {
            return;
        }
        store.setCurrency((String) currencyBox.getSelectedItem());
        persist();
        applyCurrencyLabels();
        refreshView();
        status("Currency changed to " + cur());
    }

    private void applyCurrencyLabels() {
        logoLabel.setText(" " + cur() + " ");
        budgetLbl.setText("Monthly Budget (" + cur() + "):");
        amountLabel.setText("Amount (" + cur() + ") *");
        table.getColumnModel().getColumn(3).setHeaderValue("Amount (" + cur() + ")");
        table.getTableHeader().repaint();
        chart.setCurrency(cur());
    }

    // ---------- Budget alerts ----------

    private double spentInMonth(String month) {
        double sum = 0;
        for (Expense e : store.getAll()) {
            if (month.equals(e.getMonth())) {
                sum += e.getAmount();
            }
        }
        return sum;
    }

    /** Pops up a warning when a month gets close to (80%) or goes over its budget. */
    private void checkBudgetAlert(String month, double spentBefore) {
        double budget = store.getMonthlyBudget();
        if (budget <= 0) {
            return;
        }
        double spentNow = spentInMonth(month);
        if (spentNow > budget) {
            String msg = "You are OVER budget for " + month + " by " + cur() + money(spentNow - budget)
                    + ".\nBudget: " + cur() + money(budget) + "   Spent: " + cur() + money(spentNow);
            status("Over budget for " + month + " by " + cur() + money(spentNow - budget) + "!");
            JOptionPane.showMessageDialog(this, msg, "Over budget!", JOptionPane.ERROR_MESSAGE);
        } else if (spentNow >= budget * 0.8 && spentBefore < budget * 0.8) {
            String msg = "Careful: you have used " + Math.round(spentNow / budget * 100) + "% of your budget for "
                    + month + ".\nOnly " + cur() + money(budget - spentNow) + " left.";
            status("Close to budget limit for " + month + ".");
            JOptionPane.showMessageDialog(this, msg, "Budget warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ---------- Helpers ----------

    private String selectedMonth() {
        String selected = (String) monthFilter.getSelectedItem();
        return (selected == null || ALL_MONTHS.equals(selected)) ? null : selected;
    }

    private Expense selectedExpense() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow < visible.size() ? visible.get(modelRow) : null;
    }

    private boolean persist() {
        try {
            store.save();
            return true;
        } catch (IOException ex) {
            showError("Could not save your data: " + ex.getMessage());
            return false;
        }
    }

    private void status(String message) {
        statusLabel.setText("● " + message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Expense Tracker", JOptionPane.WARNING_MESSAGE);
    }

    private static Double parseNumber(String text) {
        try {
            double value = Double.parseDouble(text);
            return (Double.isNaN(value) || Double.isInfinite(value)) ? null : value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String describe(Expense e) {
        String what = e.getDescription().isEmpty() ? e.getCategory() : e.getDescription();
        return what + " (" + cur() + money(e.getAmount()) + ", " + e.getDate() + ")";
    }

    private static String money(double value) {
        return String.format(Locale.ROOT, "%,.2f", value);
    }

    private static String plain(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("\\.00$", "");
    }

    // ---------- Custom UI Renderers & Components ----------

    /**
     * The page content. It keeps a comfortable minimum size; when the window is smaller than that,
     * scroll bars appear so everything can still be reached. When the window is bigger, it stretches.
     */
    private static class ScrollRoot extends JPanel implements javax.swing.Scrollable {
        private static final int MIN_WIDTH = 940;
        private static final int MIN_HEIGHT = 560;

        ScrollRoot(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width, MIN_WIDTH), Math.max(d.height, MIN_HEIGHT));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return orientation == javax.swing.SwingConstants.VERTICAL ? visible.height - 40 : visible.width - 40;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getParent() instanceof javax.swing.JViewport
                    && ((javax.swing.JViewport) getParent()).getWidth() >= MIN_WIDTH;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof javax.swing.JViewport
                    && ((javax.swing.JViewport) getParent()).getHeight() >= MIN_HEIGHT;
        }
    }


    private class ModernAmountRenderer extends DefaultTableCellRenderer {
        ModernAmountRenderer() {
            setHorizontalAlignment(RIGHT);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(UITheme.bold(13f));
            setForeground(UITheme.TEXT_PRIMARY);
            setBackground(isSelected ? UITheme.TABLE_SELECT_BG : (row % 2 == 1 ? UITheme.TABLE_ROW_ALT : UITheme.CARD_BG));
            setBorder(new EmptyBorder(0, 10, 0, 14));
            if (value instanceof Double) {
                setText(cur() + money((Double) value));
            }
            return c;
        }
    }

    private static class ModernProgressBar extends JComponent {
        private int percent = 0;

        public void setValue(int percent) {
            this.percent = Math.max(0, percent);
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(240, 10);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            // Track background
            g2.setColor(UITheme.TRACK_BG);
            g2.fillRoundRect(0, 0, w, h, h, h);

            // Progress fill
            int fillW = (int) Math.round((Math.min(percent, 100) / 100.0) * w);
            if (fillW > 0) {
                Color barColor = UITheme.SUCCESS;
                if (percent > 100) {
                    barColor = UITheme.DANGER;
                } else if (percent >= 80) {
                    barColor = UITheme.WARNING;
                }
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, fillW, h, h, h);
            }
            g2.dispose();
        }
    }
}
