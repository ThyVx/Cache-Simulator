package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Map;

public class CacheGUI extends JFrame {

    private Cache cache;
    private MainMemory memory;

    private JComboBox<ReplacementPolicy> replacementBox;
    private JComboBox<WritePolicy> writePolicyBox;

    private JSpinner cacheSizeSpinner;
    private JSpinner blockSizeSpinner;
    private JSpinner associativitySpinner;

    private JTextField addressField;
    private JTextArea historyArea;

    private JLabel statsLabel;
    private JLabel configLabel;

    private DefaultTableModel cacheTableModel;
    private DefaultTableModel memTableModel;

    // === COLORS ===
    private final Color BG = new Color(18, 18, 24);
    private final Color PANEL = new Color(28, 24, 40);
    private final Color ACCENT = new Color(120, 80, 200);
    private final Color TEXT = new Color(230, 230, 240);
    private final Color HIGHLIGHT = new Color(180, 140, 255);

    public CacheGUI() {
        setTitle("Cache Memory Simulator");
        setSize(1200, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        initTopPanel();
        initCacheTable();
        initRightPanel();
        initBottomPanel();

        resetCache();
    }

    // ================= TOP =================
    private void initTopPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);

        cacheSizeSpinner = styledSpinner(new JSpinner(new SpinnerNumberModel(64, 16, 4096, 16)));
        blockSizeSpinner = styledSpinner(new JSpinner(new SpinnerNumberModel(8, 1, 1024, 1)));
        associativitySpinner = styledSpinner(new JSpinner(new SpinnerNumberModel(2, 1, 64, 1)));

        replacementBox = styledComboBox(new JComboBox<>(ReplacementPolicy.values()));
        writePolicyBox = styledComboBox(new JComboBox<>(WritePolicy.values()));

        addressField = new JTextField(8);
        addressField.setBackground(BG);
        addressField.setForeground(TEXT);
        addressField.setCaretColor(TEXT);

        JButton readBtn = styledButton("READ");
        JButton writeBtn = styledButton("WRITE");
        JButton resetBtn = styledButton("RESET");

        JButton test1Btn = styledMiniButton("1");
        JButton test2Btn = styledMiniButton("2");
        JButton test3Btn = styledMiniButton("3");

        readBtn.addActionListener(e -> access(true));
        writeBtn.addActionListener(e -> access(false));
        resetBtn.addActionListener(e -> resetCache());

        test1Btn.addActionListener(e -> runTest1());
        test2Btn.addActionListener(e -> runTest2());
        test3Btn.addActionListener(e -> runTest3());

        panel.add(styledLabel("Cache(B):"));
        panel.add(cacheSizeSpinner);
        panel.add(styledLabel("Block(B):"));
        panel.add(blockSizeSpinner);
        panel.add(styledLabel("Ways:"));
        panel.add(associativitySpinner);

        panel.add(styledLabel("Replacement:"));
        panel.add(replacementBox);
        panel.add(styledLabel("Write:"));
        panel.add(writePolicyBox);

        panel.add(styledLabel("Address:"));
        panel.add(addressField);
        panel.add(readBtn);
        panel.add(writeBtn);
        panel.add(resetBtn);

        panel.add(Box.createHorizontalStrut(10));
        panel.add(styledLabel("Tests:"));
        panel.add(test1Btn);
        panel.add(test2Btn);
        panel.add(test3Btn);

        add(panel, BorderLayout.NORTH);
    }

    // ================= CACHE =================
    private void initCacheTable() {
        cacheTableModel = new DefaultTableModel(
                new Object[]{"Set", "Way", "Tag", "Block#", "Value", "Valid", "Dirty"}, 0
        );

        JTable table = new JTable(cacheTableModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT));

        add(scrollPane, BorderLayout.CENTER);
    }

    // ================= MEMORY + HISTORY =================
    private void initRightPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG);
        right.setPreferredSize(new Dimension(420, 0));

        memTableModel = new DefaultTableModel(
                new Object[]{"Block#", "Value", "Version"}, 0
        );
        JTable memTable = new JTable(memTableModel);
        styleTable(memTable);

        JScrollPane memScroll = new JScrollPane(memTable);
        memScroll.setBackground(BG);
        memScroll.getViewport().setBackground(BG);
        memScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT),
                "Main Memory",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                TEXT
        ));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setBackground(BG);
        historyArea.setForeground(HIGHLIGHT);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane histScroll = new JScrollPane(historyArea);
        histScroll.setBackground(BG);
        histScroll.getViewport().setBackground(BG);
        histScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT),
                "Action History",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                TEXT
        ));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, memScroll, histScroll);
        split.setResizeWeight(0.45);
        split.setDividerSize(6);
        split.setBackground(BG);

        right.add(split, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    // ================= BOTTOM =================
    private void initBottomPanel() {
        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.setBackground(PANEL);

        configLabel = new JLabel("Config: -");
        configLabel.setForeground(TEXT);
        configLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 2, 12));
        configLabel.setOpaque(true);
        configLabel.setBackground(PANEL);

        statsLabel = new JLabel("Hits: 0 | Misses: 0 | Hit Rate: 0%");
        statsLabel.setForeground(TEXT);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(2, 12, 6, 12));
        statsLabel.setOpaque(true);
        statsLabel.setBackground(PANEL);

        bottom.add(configLabel);
        bottom.add(statsLabel);

        add(bottom, BorderLayout.SOUTH);
    }

    // ================= CORE LOGIC =================
    private void resetCache() {
        rebuildCache(true, true);
    }

    private void rebuildCache(boolean clearHistory, boolean logResetLine) {
        int cacheSize = (Integer) cacheSizeSpinner.getValue();
        int blockSize = (Integer) blockSizeSpinner.getValue();
        int ways = (Integer) associativitySpinner.getValue();

        String error = validateConfig(cacheSize, blockSize, ways);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Invalid Cache Configuration", JOptionPane.ERROR_MESSAGE);
            return;
        }

        memory = new MainMemory();

        cache = Cache.create(
                cacheSize, blockSize, ways,
                (WritePolicy) writePolicyBox.getSelectedItem(),
                (ReplacementPolicy) replacementBox.getSelectedItem(),
                memory
        );

        cache.setLogger(this::log);

        if (clearHistory) historyArea.setText("");

        int lines = cacheSize / blockSize;
        int sets = cacheSize / (blockSize * ways);

        configLabel.setText("Config: Lines=" + lines + " | Sets=" + sets + " | Ways=" + ways +
                " | Block=" + blockSize + "B | Cache=" + cacheSize + "B" +
                " | Rep=" + replacementBox.getSelectedItem() +
                " | Write=" + writePolicyBox.getSelectedItem());

        if (logResetLine) {
            log("RESET -> cache=" + cacheSize + "B block=" + blockSize + "B ways=" + ways +
                    " replacement=" + replacementBox.getSelectedItem() +
                    " write=" + writePolicyBox.getSelectedItem());
        }

        refreshAll();
    }

    private String validateConfig(int cacheSize, int blockSize, int ways) {
        if (cacheSize <= 0 || blockSize <= 0 || ways <= 0) return "Cache size, block size, and ways must be positive.";
        if (cacheSize % blockSize != 0) return "Cache size must be divisible by block size.";
        int lines = cacheSize / blockSize;
        if (ways > lines) return "Ways cannot exceed total cache lines (" + lines + ").";
        if (cacheSize % (blockSize * ways) != 0) {
            return "Invalid config: cacheSize must be divisible by (blockSize * ways).";
        }
        return null;
    }

    private void access(boolean isRead) {
        try {
            int addr = Integer.parseInt(addressField.getText().trim());
            if (addr < 0) {
                JOptionPane.showMessageDialog(this, "Address must be >= 0");
                return;
            }
            if (isRead) cache.read(addr);
            else cache.write(addr);
            refreshAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid address");
        }
    }

    private void refreshAll() {
        refreshCacheTable();
        refreshMemoryTable();
        refreshStats();
    }

    private void refreshCacheTable() {
        cacheTableModel.setRowCount(0);

        ICacheSet[] sets = cache.getSets();
        for (int set = 0; set < sets.length; set++) {
            int way = 0;
            for (CacheLine line : sets[set].getLines()) {
                cacheTableModel.addRow(new Object[]{
                        set,
                        way++,
                        line.valid ? line.tag : "-",
                        line.valid ? line.blockNumber : "-",
                        line.valid ? line.value : "-",
                        line.valid,
                        line.dirty
                });
            }
        }
    }

    private void refreshMemoryTable() {
        memTableModel.setRowCount(0);
        for (Map.Entry<Integer, MemoryBlock> e : cache.getMemory().snapshot().entrySet()) {
            MemoryBlock b = e.getValue();
            memTableModel.addRow(new Object[]{b.blockNumber, b.value, b.version});
        }
    }

    private void refreshStats() {
        double total = cache.getHits() + cache.getMisses();
        double hitRate = total > 0 ? (cache.getHits() / total) * 100 : 0;

        statsLabel.setText(String.format(
                "Hits: %d | Misses: %d | Hit Rate: %.2f%%",
                cache.getHits(),
                cache.getMisses(),
                hitRate
        ));
    }

    private void log(String msg) {
        historyArea.append(msg + "\n");
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    // ================= TESTS =================

    private void applyConfigForTest(int cacheB, int blockB, int ways,
                                    ReplacementPolicy rp, WritePolicy wp) {
        cacheSizeSpinner.setValue(cacheB);
        blockSizeSpinner.setValue(blockB);
        associativitySpinner.setValue(ways);
        replacementBox.setSelectedItem(rp);
        writePolicyBox.setSelectedItem(wp);
        rebuildCache(false, true);
    }

    private void runTest1() {
        historyArea.setText("");
        log("TEST 1");

        applyConfigForTest(64, 8, 2, ReplacementPolicy.LRU, WritePolicy.WRITE_THROUGH);

        int h0 = cache.getHits();
        int m0 = cache.getMisses();

        cache.read(0);
        cache.read(0);

        int dh = cache.getHits() - h0;
        int dm = cache.getMisses() - m0;

        log("hits+=" + dh + " misses+=" + dm);
        log((dh == 1 && dm == 1) ? "PASS" : "FAIL");

        refreshAll();
    }

    private void runTest2() {
        historyArea.setText("");
        log("TEST 2");

        applyConfigForTest(64, 8, 2, ReplacementPolicy.LRU, WritePolicy.WRITE_THROUGH);

        int h0 = cache.getHits();
        int m0 = cache.getMisses();

        // set conflict: 0, 32, 64 -> same set for 64/8/2
        cache.read(0);   // miss
        cache.read(32);  // miss
        cache.read(0);   // hit
        cache.read(64);  // miss (evict 32)
        cache.read(32);  // miss (because evicted)

        int dh = cache.getHits() - h0;
        int dm = cache.getMisses() - m0;

        log("hits+=" + dh + " misses+=" + dm);
        log((dh == 1 && dm == 4) ? "PASS" : "FAIL");

        refreshAll();
    }

    private void runTest3() {
        historyArea.setText("");
        log("TEST 3");

        applyConfigForTest(64, 8, 2, ReplacementPolicy.LRU, WritePolicy.WRITE_BACK);

        // force dirty eviction in same set
        cache.write(0);
        cache.write(32);
        cache.write(64); // should evict one dirty line and write-back

        int memEntries = cache.getMemory().snapshot().size();

        log("memoryEntries=" + memEntries);
        log(memEntries > 0 ? "PASS" : "FAIL");

        refreshAll();
    }

    // ================= STYLING =================
    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        return label;
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private JButton styledMiniButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(42, 28));
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private <T> JComboBox<T> styledComboBox(JComboBox<T> box) {
        box.setBackground(BG);
        box.setForeground(TEXT);
        return box;
    }

    private JSpinner styledSpinner(JSpinner spinner) {
        spinner.setBackground(BG);
        spinner.setForeground(TEXT);

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(BG);
            de.getTextField().setForeground(TEXT);
            de.getTextField().setCaretColor(TEXT);
        }
        return spinner;
    }

    private void styleTable(JTable table) {
        table.setBackground(BG);
        table.setForeground(TEXT);
        table.setGridColor(PANEL);
        table.setRowHeight(24);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PANEL);
        header.setForeground(TEXT);
        header.setOpaque(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CacheGUI().setVisible(true));
    }
}
