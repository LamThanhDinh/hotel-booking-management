package com.hotel.checkout.ui;

import com.hotel.checkout.application.CalculateCheckoutUseCase;
import com.hotel.checkout.application.CalculatedCheckoutDTO;
import com.hotel.checkout.application.CheckoutResponse;
import com.hotel.checkout.application.CheckoutUseCase;
import com.hotel.checkout.application.ListActiveBookingsUseCase;
import com.hotel.checkout.application.Result;
import com.hotel.checkout.application.ServiceLineDTO;
import com.hotel.checkout.application.CheckoutError;
import com.hotel.checkout.application.SimpleBookingDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CheckoutPanel extends JPanel {
    private static final Color PRIMARY = new Color(41, 128, 185);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER = new Color(192, 57, 43);
    private static final Color CONTENT_BG = new Color(248, 249, 250);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final ListActiveBookingsUseCase listActiveBookingsUseCase;
    private final CalculateCheckoutUseCase calculateCheckoutUseCase;
    private final CheckoutUseCase checkoutUseCase;
    private final Runnable onCheckoutSuccess;

    private final JComboBox<String> bookingCombo = new JComboBox<>();
    private final JTextField bookingIdField = new JTextField();
    private final JLabel roomTotalLabel = new JLabel("-");
    private final JLabel servicesTotalLabel = new JLabel("-");
    private final JLabel grandTotalLabel = new JLabel("-");
    private final JTable serviceTable = new JTable(new ServiceTableModel());

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
        add(buildFooter(), BorderLayout.SOUTH);

        reloadBookings();
        styleTable();
    }
    
    // Set room ID để thanh toán từ RoomsPanel
    public void setRoomIdForCheckout(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            showError("Không tìm thấy booking ACTIVE cho phòng");
            return;
        }
        List<SimpleBookingDTO> actives = listActiveBookingsUseCase.execute();
        SimpleBookingDTO match = actives.stream()
                .filter(b -> roomId.equalsIgnoreCase(b.roomId()))
                .findFirst()
                .orElse(null);
        if (match != null) {
            bookingIdField.setText(match.bookingId());
            bookingCombo.setSelectedItem(match.bookingId());
            SwingUtilities.invokeLater(this::handleCalculate);
        } else {
            bookingIdField.setText("");
            bookingCombo.setSelectedItem(null);
            showError("Không tìm thấy booking ACTIVE cho phòng " + roomId);
        }
    }

    private void styleTable() {
        serviceTable.setRowHeight(32);
        serviceTable.setFont(LABEL_FONT);
        serviceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        serviceTable.getTableHeader().setBackground(PRIMARY);
        serviceTable.getTableHeader().setForeground(Color.WHITE);
        serviceTable.setSelectionBackground(new Color(52, 152, 219));
        serviceTable.setSelectionForeground(Color.WHITE);
        serviceTable.setShowHorizontalLines(true);
        serviceTable.setGridColor(new Color(220, 220, 220));
        
        // Zebra striping
        serviceTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
        JLabel title = new JLabel("Thanh toán");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.WEST);
        return panel;
    }

    private JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildForm(), BorderLayout.WEST);
        panel.add(buildDetails(), BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Booking"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addRow(panel, gbc, row++, "Mã booking (nhập tay)", bookingIdField);
        addRow(panel, gbc, row++, "Booking đang ACTIVE", bookingCombo);

        JButton calcBtn = new JButton("Tính tiền");
        calcBtn.setFont(LABEL_FONT);
        calcBtn.setBackground(new Color(52, 152, 219));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setFocusPainted(false);
        calcBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calcBtn.setToolTipText("Tính tổng tiền phòng và dịch vụ");
        calcBtn.addActionListener(e -> handleCalculate());
        JButton payBtn = new JButton("Thanh toán");
        payBtn.setFont(LABEL_FONT);
        payBtn.setBackground(SUCCESS);
        payBtn.setForeground(Color.WHITE);
        payBtn.setFocusPainted(false);
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payBtn.setToolTipText("Xác nhận thanh toán và trả phòng");
        payBtn.addActionListener(e -> handlePay());

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.add(calcBtn); btns.add(payBtn);
        panel.add(btns, gbc);
        return panel;
    }

    private JComponent buildDetails() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "Tổng kết"
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel totals = new JPanel(new GridLayout(3, 2, 10, 8));
        totals.setBackground(Color.WHITE);
        JLabel lbl1 = new JLabel("Tiền phòng:"); lbl1.setFont(LABEL_FONT);
        JLabel lbl2 = new JLabel("Tiền dịch vụ:"); lbl2.setFont(LABEL_FONT);
        JLabel lbl3 = new JLabel("Tổng cộng:"); lbl3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roomTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        servicesTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        grandTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        grandTotalLabel.setForeground(SUCCESS);
        totals.add(lbl1); totals.add(roomTotalLabel);
        totals.add(lbl2); totals.add(servicesTotalLabel);
        totals.add(lbl3); totals.add(grandTotalLabel);

        serviceTable.setModel(new ServiceTableModel());
        JScrollPane tableScroll = new JScrollPane(serviceTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            "Chi tiết dịch vụ"
        ));

        panel.add(totals, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel hint = new JLabel("(*) Chỉ áp dụng cho booking ACTIVE");
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

    private void reloadBookings() {
        bookingCombo.removeAllItems();
        listActiveBookingsUseCase.execute().forEach(b -> bookingCombo.addItem(b.bookingId()));
    }

    private String resolveBookingId() {
        if (bookingIdField.getText() != null && !bookingIdField.getText().isBlank()) {
            return bookingIdField.getText().trim();
        }
        Object selected = bookingCombo.getSelectedItem();
        return selected == null ? null : selected.toString();
    }

    private void handleCalculate() {
        String bookingId = resolveBookingId();
        if (bookingId == null) {
            showError("Vui lòng chọn booking ACTIVE.");
            return;
        }
        Result<CalculatedCheckoutDTO, CheckoutError> result = calculateCheckoutUseCase.execute(bookingId);
        if (result.isSuccess()) {
            CalculatedCheckoutDTO dto = result.getValue().orElseThrow();
            roomTotalLabel.setText(dto.roomTotal());
            servicesTotalLabel.setText(dto.servicesTotal());
            grandTotalLabel.setText(dto.grandTotal());
            ((ServiceTableModel) serviceTable.getModel()).setData(dto.serviceLines());
            showInfo("Tính tiền thành công cho booking " + dto.bookingId());
        } else {
            showError(result.getError().orElse(new CheckoutError("UNKNOWN", "Không rõ lỗi")).message());
        }
    }

    private void handlePay() {
        String bookingId = resolveBookingId();
        if (bookingId == null) {
            showError("Vui lòng chọn booking ACTIVE.");
            return;
        }
        Result<CheckoutResponse, CheckoutError> result = checkoutUseCase.execute(bookingId);
        if (result.isSuccess()) {
            CheckoutResponse resp = result.getValue().orElseThrow();
            showInfo("Thanh toán thành công. Invoice: " + resp.invoiceId() + " | Tổng: " + resp.grandTotal());
            roomTotalLabel.setText("-");
            servicesTotalLabel.setText("-");
            grandTotalLabel.setText("-");
            ((ServiceTableModel) serviceTable.getModel()).setData(new ArrayList<>());
            reloadBookings();
            if (onCheckoutSuccess != null) {
                onCheckoutSuccess.run();
            }
        } else {
            showError(result.getError().orElse(new CheckoutError("UNKNOWN", "Không rõ lỗi")).message());
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thanh toán", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Thanh toán", JOptionPane.ERROR_MESSAGE);
    }

    private static class ServiceTableModel extends AbstractTableModel {
        private final String[] columns = {"Dịch vụ", "SL", "Đơn giá", "Thành tiền"};
        private List<ServiceLineDTO> data = new ArrayList<>();

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
            ServiceLineDTO row = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.serviceName();
                case 1 -> row.quantity();
                case 2 -> row.unitPrice();
                case 3 -> row.lineTotal();
                default -> "";
            };
        }

        public void setData(List<ServiceLineDTO> rows) {
            this.data = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        public List<ServiceLineDTO> getData() {
            return data;
        }
    }
}
