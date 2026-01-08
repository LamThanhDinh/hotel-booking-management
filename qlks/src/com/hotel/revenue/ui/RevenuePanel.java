package com.hotel.revenue.ui;

import com.hotel.revenue.application.GetRevenueReportUseCase;
import com.hotel.revenue.application.RevenueDailyLineDTO;
import com.hotel.revenue.application.RevenueReportDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RevenuePanel extends JPanel {
    private static final Color PRIMARY = new Color(41, 128, 185);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color CONTENT_BG = new Color(248, 249, 250);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final GetRevenueReportUseCase getRevenueReportUseCase;

    private final JTextField fromField = new JTextField();
    private final JTextField toField = new JTextField();
    private final JLabel totalLabel = new JLabel("-");
    private final JLabel countLabel = new JLabel("-");
    private final JTable dailyTable = new JTable(new DailyTableModel());

    public RevenuePanel(GetRevenueReportUseCase getRevenueReportUseCase) {
        this.getRevenueReportUseCase = getRevenueReportUseCase;
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        styleTable();
    }

    private void styleTable() {
        dailyTable.setRowHeight(32);
        dailyTable.setFont(LABEL_FONT);
        dailyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dailyTable.getTableHeader().setBackground(PRIMARY);
        dailyTable.getTableHeader().setForeground(Color.WHITE);
        dailyTable.setSelectionBackground(new Color(52, 152, 219));
        dailyTable.setSelectionForeground(Color.WHITE);
        dailyTable.setShowHorizontalLines(true);
        dailyTable.setGridColor(new Color(220, 220, 220));
        
        // Zebra striping
        dailyTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 247, 250));
                }
                return c;
            }
        });
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));
        JLabel title = new JLabel("Doanh thu");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.WEST);
        return panel;
    }

    private JComponent buildContent() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.3);
        split.setTopComponent(buildForm());
        split.setBottomComponent(buildReport());
        return split;
    }

    private JComponent buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Tham số"
            ),
            new EmptyBorder(10, 15, 10, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addRow(panel, gbc, row++, "Từ (yyyy-mm-dd)", fromField);
        addRow(panel, gbc, row++, "Đến (yyyy-mm-dd)", toField);

        JButton btn = new JButton("Tạo báo cáo");
        btn.setFont(LABEL_FONT);
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Tạo báo cáo doanh thu theo khoảng thời gian");
        btn.addActionListener(e -> handleGenerate());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(btn, gbc);
        return panel;
    }

    private JComponent buildReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Kết quả"
            ),
            new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel summary = new JPanel(new GridLayout(2, 2, 10, 8));
        summary.setBackground(Color.WHITE);
        JLabel lbl1 = new JLabel("Tổng doanh thu:"); lbl1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel lbl2 = new JLabel("Số hóa đơn:"); lbl2.setFont(LABEL_FONT);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(SUCCESS);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summary.add(lbl1); summary.add(totalLabel);
        summary.add(lbl2); summary.add(countLabel);

        dailyTable.setModel(new DailyTableModel());
        JScrollPane scroll = new JScrollPane(dailyTable);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            "Doanh thu theo ngày"
        ));

        panel.add(summary, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel hint = new JLabel("(*) Chỉ tính invoice PAID");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(new Color(127, 140, 141));
        panel.add(hint, BorderLayout.WEST);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent input) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1; panel.add(input, gbc);
    }

    private void handleGenerate() {
        try {
            LocalDate from = parseDate(fromField.getText());
            LocalDate to = parseDate(toField.getText());
            RevenueReportDTO dto = getRevenueReportUseCase.execute(from, to);
            totalLabel.setText(dto.totalRevenue());
            countLabel.setText(String.valueOf(dto.invoiceCount()));
            ((DailyTableModel) dailyTable.getModel()).setData(dto.dailyLines());
            JOptionPane.showMessageDialog(this, "Đã tạo báo cáo", "Doanh thu", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ (yyyy-mm-dd)", "Doanh thu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static class DailyTableModel extends AbstractTableModel {
        private final String[] columns = {"Ngày", "Số tiền"};
        private List<RevenueDailyLineDTO> data = new ArrayList<>();

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RevenueDailyLineDTO row = data.get(rowIndex);
            return columnIndex == 0 ? row.date() : row.amount();
        }

        public void setData(List<RevenueDailyLineDTO> rows) {
            this.data = new ArrayList<>(rows);
            fireTableDataChanged();
        }
    }
}
