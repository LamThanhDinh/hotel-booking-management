package com.hotel.checkout.ui;

import com.hotel.checkout.application.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CheckoutPanel extends JPanel {
    private static final Color PRIMARY = new Color(41, 128, 185);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER = new Color(192, 57, 43);
    private static final Color WARNING = new Color(243, 156, 18);
    private static final Color CONTENT_BG = new Color(248, 249, 250);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    private final ListActiveBookingsUseCase listActiveBookingsUseCase;
    private final CalculateCheckoutUseCase calculateCheckoutUseCase;
    private final CheckoutUseCase checkoutUseCase;
    private final Runnable onCheckoutSuccess;

    private final JTable invoiceTable = new JTable(new InvoiceTableModel());
    private final JLabel detailBookingId = new JLabel("-");
    private final JLabel detailRoomId = new JLabel("-");
    private final JLabel detailCustomer = new JLabel("-");
    private final JLabel detailCheckIn = new JLabel("-");
    private final JLabel detailCheckOut = new JLabel("-");
    private final JLabel detailRoomTotal = new JLabel("-");
    private final JLabel detailServiceTotal = new JLabel("-");
    private final JLabel detailGrandTotal = new JLabel("-");
    private final JLabel detailStatus = new JLabel("-");
    private final JLabel detailPaidAt = new JLabel("-");
    private final JButton checkoutButton = new JButton("Thanh toán");
    private final JButton calculateButton = new JButton("Tính lại tiền");
    
    private String selectedBookingId = null;

    public CheckoutPanel(ListActiveBookingsUseCase listActiveBookingsUseCase,
                         CalculateCheckoutUseCase calculateCheckoutUseCase,
                         CheckoutUseCase checkoutUseCase,
                         Runnable onCheckoutSuccess) {
        this.listActiveBookingsUseCase = listActiveBookingsUseCase;
        this.calculateCheckoutUseCase = calculateCheckoutUseCase;
        this.checkoutUseCase = checkoutUseCase;
        this.onCheckoutSuccess = onCheckoutSuccess;

        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        
        styleTable();
        wireEvents();
        refreshData();
    }
    
    public void refreshData() {
        loadInvoices();
    }
    
    public void setRoomIdForCheckout(String roomId) {
        // Tìm booking cho phòng này và chọn trong bảng
        refreshData();
        InvoiceTableModel model = (InvoiceTableModel) invoiceTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            InvoiceRowDTO row = model.getRowAt(i);
            if (roomId.equalsIgnoreCase(row.roomId)) {
                invoiceTable.setRowSelectionInterval(i, i);
                invoiceTable.scrollRectToVisible(invoiceTable.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Trả phòng & Hóa đơn");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY);
        
        JButton refreshBtn = new JButton("Làm mới");
        refreshBtn.setFont(LABEL_FONT);
        refreshBtn.setBackground(new Color(149, 165, 166));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());
        
        panel.add(title, BorderLayout.WEST);
        panel.add(refreshBtn, BorderLayout.EAST);
        return panel;
    }

    private JComponent buildContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.65);
        split.setDividerLocation(750);
        split.setBorder(null);
        
        split.setLeftComponent(buildTablePanel());
        split.setRightComponent(buildDetailPanel());
        return split;
    }

    private JComponent buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel detailTitle = new JLabel("Chi tiết hóa đơn");
        detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        detailTitle.setForeground(PRIMARY);
        detailTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addDetailRow(infoPanel, gbc, row++, "Mã booking:", detailBookingId);
        addDetailRow(infoPanel, gbc, row++, "Phòng:", detailRoomId);
        addDetailRow(infoPanel, gbc, row++, "Khách hàng:", detailCustomer);
        addDetailRow(infoPanel, gbc, row++, "Check-in:", detailCheckIn);
        addDetailRow(infoPanel, gbc, row++, "Check-out:", detailCheckOut);
        addDetailRow(infoPanel, gbc, row++, "Tiền phòng:", detailRoomTotal);
        addDetailRow(infoPanel, gbc, row++, "Tiền dịch vụ:", detailServiceTotal);
        addDetailRow(infoPanel, gbc, row++, "Tổng cộng:", detailGrandTotal);
        addDetailRow(infoPanel, gbc, row++, "Trạng thái:", detailStatus);
        addDetailRow(infoPanel, gbc, row++, "Ngày thanh toán:", detailPaidAt);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        calculateButton.setFont(LABEL_FONT);
        calculateButton.setBackground(PRIMARY);
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFocusPainted(false);
        calculateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calculateButton.setEnabled(false);
        
        checkoutButton.setFont(LABEL_FONT);
        checkoutButton.setBackground(SUCCESS);
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);
        checkoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkoutButton.setEnabled(false);
        
        buttonPanel.add(calculateButton);
        buttonPanel.add(checkoutButton);

        panel.add(detailTitle, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(new Color(100, 116, 139));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valueLabel.setForeground(new Color(30, 41, 59));
        panel.add(valueLabel, gbc);
    }

    private void styleTable() {
        invoiceTable.setRowHeight(32);
        invoiceTable.setFont(LABEL_FONT);
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        invoiceTable.getTableHeader().setBackground(PRIMARY);
        invoiceTable.getTableHeader().setForeground(Color.WHITE);
        invoiceTable.setSelectionBackground(new Color(52, 152, 219));
        invoiceTable.setSelectionForeground(Color.WHITE);
        invoiceTable.setShowHorizontalLines(true);
        invoiceTable.setGridColor(new Color(220, 220, 220));
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Đặt độ rộng cột
        invoiceTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Booking ID
        invoiceTable.getColumnModel().getColumn(1).setPreferredWidth(70);  // Phòng
        invoiceTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Khách hàng
        invoiceTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Check-in
        invoiceTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Check-out
        invoiceTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Tiền phòng
        invoiceTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Tiền DV
        invoiceTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Tổng
        invoiceTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Trạng thái
        
        // Custom renderer cho trạng thái
        invoiceTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = value != null ? value.toString() : "";
                    if ("ĐÃ THANH TOÁN".equals(status) || "PAID".equals(status)) {
                        c.setBackground(new Color(212, 237, 218));
                        c.setForeground(new Color(25, 135, 84));
                    } else {
                        c.setBackground(new Color(255, 243, 205));
                        c.setForeground(new Color(133, 100, 4));
                    }
                }
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                return c;
                }
        });
        
        // Zebra striping
        invoiceTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && column != 8) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 247, 250));
                }
                return c;
            }
        });
    }

    private void wireEvents() {
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = invoiceTable.getSelectedRow();
                if (selectedRow >= 0) {
                    InvoiceTableModel model = (InvoiceTableModel) invoiceTable.getModel();
                    InvoiceRowDTO row = model.getRowAt(selectedRow);
                    selectedBookingId = row.bookingId;
                    loadDetail(row);
                } else {
                    clearDetail();
                }
            }
        });
        
        calculateButton.addActionListener(e -> handleCalculate());
        checkoutButton.addActionListener(e -> handleCheckout());
    }

    private void loadInvoices() {
        // Lấy tất cả booking ACTIVE và tính toán hóa đơn
        List<SimpleBookingDTO> bookings = listActiveBookingsUseCase.execute();
        List<InvoiceRowDTO> rows = new ArrayList<>();
        
        for (SimpleBookingDTO booking : bookings) {
            Result<CalculatedCheckoutDTO, CheckoutError> result = calculateCheckoutUseCase.execute(booking.bookingId());
            if (result.isSuccess()) {
                CalculatedCheckoutDTO calc = result.getValue().orElseThrow();
                InvoiceRowDTO row = new InvoiceRowDTO(
                    booking.bookingId(),
                    booking.roomId(),
                    "Khách hàng",  // Tạm thời
                    "-",  // Check-in date
                    "-",  // Check-out date
                    calc.roomTotal(),
                    calc.servicesTotal(),
                    calc.grandTotal(),
                    "CHƯA TT"
                );
                rows.add(row);
            }
        }
        
        ((InvoiceTableModel) invoiceTable.getModel()).setData(rows);
    }

    private void loadDetail(InvoiceRowDTO row) {
        detailBookingId.setText(row.bookingId);
        detailRoomId.setText(row.roomId);
        detailCustomer.setText(row.customerName);
        detailCheckIn.setText(row.checkInDate);
        detailCheckOut.setText(row.checkOutDate);
        detailRoomTotal.setText(row.roomTotal);
        detailServiceTotal.setText(row.serviceTotal);
        detailGrandTotal.setText(row.grandTotal);
        detailStatus.setText(row.status);
        detailPaidAt.setText("-");
        
        boolean isPaid = "ĐÃ THANH TOÁN".equals(row.status) || "PAID".equals(row.status);
        calculateButton.setEnabled(!isPaid);
        checkoutButton.setEnabled(!isPaid);
    }

    private void clearDetail() {
        detailBookingId.setText("-");
        detailRoomId.setText("-");
        detailCustomer.setText("-");
        detailCheckIn.setText("-");
        detailCheckOut.setText("-");
        detailRoomTotal.setText("-");
        detailServiceTotal.setText("-");
        detailGrandTotal.setText("-");
        detailStatus.setText("-");
        detailPaidAt.setText("-");
        calculateButton.setEnabled(false);
        checkoutButton.setEnabled(false);
    }

    private void handleCalculate() {
        if (selectedBookingId == null) {
            showError("Vui lòng chọn hóa đơn để tính lại.");
            return;
        }
        refreshData();
        showInfo("Đã tính lại tiền cho booking: " + selectedBookingId);
    }

    private void handleCheckout() {
        if (selectedBookingId == null) {
            showError("Vui lòng chọn hóa đơn để thanh toán.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xác nhận thanh toán và trả phòng?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        Result<CheckoutResponse, CheckoutError> result = checkoutUseCase.execute(selectedBookingId);
        if (result.isSuccess()) {
            CheckoutResponse resp = result.getValue().orElseThrow();
            showInfo("Thanh toán thành công!\nHóa đơn: " + resp.invoiceId() + "\nTổng tiền: " + formatMoney(resp.grandTotal()));
            refreshData();
            if (onCheckoutSuccess != null) {
                onCheckoutSuccess.run();
            }
        } else {
            CheckoutError error = result.getError().orElse(new CheckoutError("UNKNOWN", "Lỗi không xác định"));
            showError(error.message());
        }
    }

    private String formatMoney(String amount) {
        try {
            double val = Double.parseDouble(amount.replaceAll("[^0-9.]", ""));
            return String.format("%,.0f VNĐ", val);
        } catch (Exception e) {
            return amount;
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Trả phòng", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // DTO for table rows
    private static class InvoiceRowDTO {
        final String bookingId;
        final String roomId;
        final String customerName;
        final String checkInDate;
        final String checkOutDate;
        final String roomTotal;
        final String serviceTotal;
        final String grandTotal;
        final String status;

        InvoiceRowDTO(String bookingId, String roomId, String customerName, String checkInDate, String checkOutDate,
                     String roomTotal, String serviceTotal, String grandTotal, String status) {
            this.bookingId = bookingId;
            this.roomId = roomId;
            this.customerName = customerName;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.roomTotal = roomTotal;
            this.serviceTotal = serviceTotal;
            this.grandTotal = grandTotal;
            this.status = status;
        }
    }

    // Table Model
    private static class InvoiceTableModel extends AbstractTableModel {
        private final String[] columns = {"Mã Booking", "Phòng", "Khách hàng", "Check-in", "Check-out", 
                                         "Tiền phòng", "Tiền DV", "Tổng tiền", "Trạng thái"};
        private List<InvoiceRowDTO> data = new ArrayList<>();

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
            InvoiceRowDTO row = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.bookingId;
                case 1 -> row.roomId;
                case 2 -> row.customerName;
                case 3 -> row.checkInDate;
                case 4 -> row.checkOutDate;
                case 5 -> row.roomTotal;
                case 6 -> row.serviceTotal;
                case 7 -> row.grandTotal;
                case 8 -> row.status;
                default -> "";
            };
        }

        public void setData(List<InvoiceRowDTO> rows) {
            this.data = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        public InvoiceRowDTO getRowAt(int index) {
            return data.get(index);
        }
    }
}
