package com.hotel.booking.ui;

import com.hotel.booking.application.CustomerDTO;
import com.hotel.booking.application.ListCustomersUseCase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomersPanel extends JPanel {
    private static final Color PRIMARY = new Color(41, 128, 185);
    private static final Color CONTENT_BG = new Color(248, 249, 250);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final ListCustomersUseCase listCustomersUseCase;

    private final JTextField searchField = new JTextField(20);
    private final JTable customerTable = new JTable(new CustomerTableModel());
    private final JLabel countLabel = new JLabel("0 khách hàng");

    public CustomersPanel(ListCustomersUseCase listCustomersUseCase) {
        this.listCustomersUseCase = listCustomersUseCase;
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        styleTable();
        loadCustomers("");
    }

    private void styleTable() {
        customerTable.setRowHeight(32);
        customerTable.setFont(LABEL_FONT);
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        customerTable.getTableHeader().setBackground(PRIMARY);
        customerTable.getTableHeader().setForeground(Color.WHITE);
        customerTable.setSelectionBackground(new Color(52, 152, 219));
        customerTable.setSelectionForeground(Color.WHITE);
        customerTable.setShowHorizontalLines(true);
        customerTable.setGridColor(new Color(220, 220, 220));
        
        // Zebra striping
        customerTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
        
        JLabel title = new JLabel("Khách hàng");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(CONTENT_BG);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchField.setPreferredSize(new Dimension(200, 32));
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Tìm");
        searchBtn.setFont(LABEL_FONT);
        searchBtn.setBackground(PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.addActionListener(e -> loadCustomers(searchField.getText()));
        searchPanel.add(searchBtn);
        
        JButton refreshBtn = new JButton("Làm mới");
        refreshBtn.setFont(LABEL_FONT);
        refreshBtn.setBackground(new Color(39, 174, 96));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadCustomers("");
        });
        searchPanel.add(refreshBtn);
        
        panel.add(title, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);
        return panel;
    }

    private JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        countLabel.setForeground(PRIMARY);
        panel.add(countLabel, BorderLayout.WEST);
        return panel;
    }

    private void loadCustomers(String searchText) {
        List<CustomerDTO> allCustomers = listCustomersUseCase.execute();
        
        List<CustomerDTO> filtered;
        if (searchText == null || searchText.isBlank()) {
            filtered = allCustomers;
        } else {
            String lower = searchText.toLowerCase();
            filtered = allCustomers.stream()
                    .filter(c -> c.name().toLowerCase().contains(lower) ||
                                c.phone().contains(lower) ||
                                c.identityNo().toLowerCase().contains(lower))
                    .toList();
        }
        
        ((CustomerTableModel) customerTable.getModel()).setData(filtered);
        countLabel.setText(filtered.size() + " khách hàng");
    }

    public void refreshData() {
        loadCustomers(searchField.getText());
    }

    private static class CustomerTableModel extends AbstractTableModel {
        private final String[] columns = {"Mã KH", "Họ tên", "Số điện thoại", "CMND/CCCD"};
        private List<CustomerDTO> data = new ArrayList<>();

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CustomerDTO row = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.customerId();
                case 1 -> row.name();
                case 2 -> row.phone();
                case 3 -> row.identityNo();
                default -> "";
            };
        }

        public void setData(List<CustomerDTO> rows) {
            this.data = new ArrayList<>(rows);
            fireTableDataChanged();
        }
    }
}
