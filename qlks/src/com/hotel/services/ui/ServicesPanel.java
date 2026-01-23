package com.hotel.services.ui;

import com.hotel.services.application.AddServiceToBookingRequest;
import com.hotel.services.application.AddServiceToBookingResponse;
import com.hotel.services.application.AddServiceToBookingUseCase;
import com.hotel.services.application.BookingListItemDTO;
import com.hotel.services.application.DeleteServiceUseCase;
import com.hotel.services.application.ListActiveBookingsUseCase;
import com.hotel.services.application.ListAvailableServicesUseCase;
import com.hotel.services.application.Result;
import com.hotel.services.application.SaveServiceUseCase;
import com.hotel.services.application.ServiceError;
import com.hotel.services.application.ServiceListItemDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ServicesPanel extends JPanel {
    private static final Color PRIMARY = new Color(41, 128, 185);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER = new Color(192, 57, 43);
    private static final Color CONTENT_BG = new Color(248, 249, 250);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final ListAvailableServicesUseCase listAvailableServicesUseCase;
    private final AddServiceToBookingUseCase addServiceToBookingUseCase;
    private final ListActiveBookingsUseCase listActiveBookingsUseCase;
    private final com.hotel.services.application.SaveServiceUseCase saveServiceUseCase;
    private final com.hotel.services.application.DeleteServiceUseCase deleteServiceUseCase;

    private final JTextField searchField = new JTextField();
    private final ServicesTableModel tableModel = new ServicesTableModel();
    private final JTable table = new JTable(tableModel);

    private final JComboBox<String> bookingCombo = new JComboBox<>();
    private final JTextField bookingIdField = new JTextField();
    private final JTextField quantityField = new JTextField("1");
    private final JLabel resultLabel = new JLabel(" ");
    
    // Form sửa/thêm dịch vụ
    private final JTextField serviceIdField = new JTextField();
    private final JTextField serviceNameField = new JTextField();
    private final JTextField servicePriceField = new JTextField();
    private final JTextField serviceStockField = new JTextField();
    private final JButton saveServiceBtn = new JButton("Lưu");
    private final JButton deleteServiceBtn = new JButton("Xóa");
    private final JButton newServiceBtn = new JButton("Tạo mới");
    private final JButton cancelServiceBtn = new JButton("Hủy");
    private String selectedServiceId = null;
    private boolean isNewMode = false;

    public ServicesPanel(ListAvailableServicesUseCase listAvailableServicesUseCase,
                         AddServiceToBookingUseCase addServiceToBookingUseCase,
                         ListActiveBookingsUseCase listActiveBookingsUseCase,
                         SaveServiceUseCase saveServiceUseCase,
                         DeleteServiceUseCase deleteServiceUseCase) {
        this.listAvailableServicesUseCase = listAvailableServicesUseCase;
        this.addServiceToBookingUseCase = addServiceToBookingUseCase;
        this.listActiveBookingsUseCase = listActiveBookingsUseCase;
        this.saveServiceUseCase = saveServiceUseCase;
        this.deleteServiceUseCase = deleteServiceUseCase;

        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        wireEvents();
        reloadServices();
        reloadBookings();
        styleTable();
    }

    private void styleTable() {
        table.setRowHeight(32);
        table.setFont(LABEL_FONT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(220, 220, 220));
        
        // Zebra striping
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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

    public void refreshData() {
        reloadServices();
        reloadBookings();
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));
        JLabel title = new JLabel("Dịch vụ");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(CONTENT_BG);
        
        JButton refreshBtn = new JButton("Làm mới");
        refreshBtn.setFont(LABEL_FONT);
        refreshBtn.setBackground(new Color(149, 165, 166));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setToolTipText("Làm mới danh sách dịch vụ");
        refreshBtn.addActionListener(e -> refreshData());

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(CONTENT_BG);
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(LABEL_FONT);
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setFont(LABEL_FONT);
        searchPanel.add(searchField, BorderLayout.CENTER);

        rightPanel.add(searchPanel);
        rightPanel.add(refreshBtn);

        panel.add(title, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JComponent buildContent() {
        // Panel chính chia 3 phần: Table bên trái, Form chi tiết giữa, Form thêm vào booking bên phải
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Table bên trái
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Danh sách dịch vụ"));
        tableScroll.setPreferredSize(new Dimension(500, 0));
        
        // Panel bên phải chứa 2 form
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        rightPanel.add(buildServiceDetailForm());
        rightPanel.add(buildAddToBookingForm());
        
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        return mainPanel;
    }
    
    private JPanel buildServiceDetailForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Chi tiết dịch vụ"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        serviceIdField.setEnabled(false);
        serviceIdField.setBackground(Color.LIGHT_GRAY);
        
        int row = 0;
        addRow(panel, gbc, row++, "Mã DV:", serviceIdField);
        addRow(panel, gbc, row++, "Tên DV:", serviceNameField);
        addRow(panel, gbc, row++, "Giá:", servicePriceField);
        addRow(panel, gbc, row++, "Tồn kho:", serviceStockField);

        // Button panel
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        
        newServiceBtn.setFont(LABEL_FONT);
        newServiceBtn.setBackground(PRIMARY);
        newServiceBtn.setForeground(Color.WHITE);
        newServiceBtn.setFocusPainted(false);
        newServiceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newServiceBtn.addActionListener(e -> handleNewService());
        
        saveServiceBtn.setFont(LABEL_FONT);
        saveServiceBtn.setBackground(SUCCESS);
        saveServiceBtn.setForeground(Color.WHITE);
        saveServiceBtn.setFocusPainted(false);
        saveServiceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveServiceBtn.setEnabled(false);
        saveServiceBtn.addActionListener(e -> handleSaveService());
        
        deleteServiceBtn.setFont(LABEL_FONT);
        deleteServiceBtn.setBackground(DANGER);
        deleteServiceBtn.setForeground(Color.WHITE);
        deleteServiceBtn.setFocusPainted(false);
        deleteServiceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteServiceBtn.setEnabled(false);
        deleteServiceBtn.addActionListener(e -> handleDeleteService());
        
        cancelServiceBtn.setFont(LABEL_FONT);
        cancelServiceBtn.setBackground(new Color(108, 117, 125));
        cancelServiceBtn.setForeground(Color.WHITE);
        cancelServiceBtn.setFocusPainted(false);
        cancelServiceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelServiceBtn.setEnabled(false);
        cancelServiceBtn.addActionListener(e -> handleCancelService());
        
        btnPanel.add(newServiceBtn);
        btnPanel.add(saveServiceBtn);
        btnPanel.add(deleteServiceBtn);
        btnPanel.add(cancelServiceBtn);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);
        
        return panel;
    }

    private JPanel buildAddToBookingForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Thêm vào booking"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addRow(panel, gbc, row++, "Chọn booking", bookingCombo);
        addRow(panel, gbc, row++, "Số lượng", quantityField);

        JButton addBtn = new JButton("Thêm dịch vụ");
        addBtn.setFont(LABEL_FONT);
        addBtn.setBackground(SUCCESS);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setToolTipText("Thêm dịch vụ đã chọn vào booking");
        addBtn.addActionListener(e -> handleAddService());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(addBtn, gbc);
        return panel;
    }

    private JComponent buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultLabel.setForeground(SUCCESS);
        panel.add(resultLabel, BorderLayout.CENTER);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label + ":"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(input, gbc);
    }

    private void wireEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { reloadServices(); }
            @Override
            public void removeUpdate(DocumentEvent e) { reloadServices(); }
            @Override
            public void changedUpdate(DocumentEvent e) { reloadServices(); }
        });
        
        // Khi click vào row trong table, load dữ liệu vào form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    ServiceListItemDTO service = tableModel.getAt(selectedRow);
                    loadServiceToForm(service);
                }
            }
        });
    }
    
    private void loadServiceToForm(ServiceListItemDTO service) {
        isNewMode = false;
        selectedServiceId = service.id();
        serviceIdField.setText(service.id());
        serviceNameField.setText(service.name());
        servicePriceField.setText(service.unitPrice().replaceAll("[^0-9.]", ""));
        serviceStockField.setText(String.valueOf(service.stock())); // Convert int to String
        saveServiceBtn.setEnabled(true);
        deleteServiceBtn.setEnabled(true);
        cancelServiceBtn.setEnabled(true);
    }
    
    private void clearServiceForm() {
        selectedServiceId = null;
        serviceIdField.setText("");
        serviceNameField.setText("");
        servicePriceField.setText("");
        serviceStockField.setText("");
        saveServiceBtn.setEnabled(false);
        deleteServiceBtn.setEnabled(false);
    }
    
    private void handleNewService() {
        isNewMode = true;
        clearServiceForm();
        serviceIdField.setText("SV" + System.currentTimeMillis());
        saveServiceBtn.setEnabled(true);
        cancelServiceBtn.setEnabled(true);
        deleteServiceBtn.setEnabled(false);
        serviceNameField.requestFocus();
    }
    
    private void handleCancelService() {
        isNewMode = false;
        clearServiceForm();
        saveServiceBtn.setEnabled(false);
        cancelServiceBtn.setEnabled(false);
        deleteServiceBtn.setEnabled(false);
    }
    
    private void handleSaveService() {
        String id = serviceIdField.getText().trim();
        String name = serviceNameField.getText().trim();
        String priceStr = servicePriceField.getText().trim();
        String stockStr = serviceStockField.getText().trim();
        
        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);
            
            saveServiceUseCase.execute(id, name, java.math.BigDecimal.valueOf(price), stock);
            showInfo("Lưu dịch vụ thành công!");
            reloadServices();
            handleCancelService();
            
        } catch (NumberFormatException ex) {
            showError("Giá và tồn kho phải là số!");
        } catch (Exception ex) {
            showError("Lỗi khi lưu: " + ex.getMessage());
        }
    }
    
    private void handleDeleteService() {
        if (selectedServiceId == null) {
            showError("Vui lòng chọn dịch vụ để xóa!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa dịch vụ: " + serviceNameField.getText() + "?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                deleteServiceUseCase.execute(selectedServiceId);
                showInfo("Xóa dịch vụ thành công!");
                reloadServices();
                handleCancelService();
            } catch (Exception ex) {
                showError("Lỗi khi xóa: " + ex.getMessage());
            }
        }
    }

    private void reloadServices() {
        List<ServiceListItemDTO> items = listAvailableServicesUseCase.execute(searchField.getText());
        tableModel.setData(items);
        if (!items.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void reloadBookings() {
        bookingCombo.removeAllItems();
        List<BookingListItemDTO> bookings = listActiveBookingsUseCase.execute();
        bookings.forEach(b -> bookingCombo.addItem(b.bookingId()));
    }

    private void handleAddService() {
        String selectedServiceId = getSelectedServiceId();
        if (selectedServiceId == null) {
            showError("Vui lòng chọn dịch vụ.");
            return;
        }
        String bookingId = resolveBookingId();
        if (bookingId == null) {
            showError("Vui lòng nhập hoặc chọn booking ACTIVE.");
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Quantity phải là số.");
            return;
        }

        AddServiceToBookingRequest request = new AddServiceToBookingRequest(bookingId, selectedServiceId, quantity);
        Result<AddServiceToBookingResponse, ServiceError> result = addServiceToBookingUseCase.execute(request);
        if (result.isSuccess()) {
            AddServiceToBookingResponse resp = result.getValue().orElseThrow();
            showInfo("Thêm dịch vụ thành công: " + resp.serviceName() + " x" + resp.quantity() + " (" + resp.lineTotal() + ")");
            reloadServices();
            reloadBookings();
        } else {
            ServiceError error = result.getError().orElse(new ServiceError("UNKNOWN", "Không rõ lỗi"));
            showError(error.message());
        }
    }

    private String getSelectedServiceId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return tableModel.getAt(row).id();
    }

    private String resolveBookingId() {
        Object selected = bookingCombo.getSelectedItem();
        return selected == null ? null : selected.toString();
    }

    private void showInfo(String message) {
        resultLabel.setForeground(SUCCESS);
        resultLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Dịch vụ", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        resultLabel.setForeground(DANGER);
        resultLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Dịch vụ", JOptionPane.ERROR_MESSAGE);
    }

    private static class ServicesTableModel extends AbstractTableModel {
        private final String[] columns = {"Mã", "Tên", "Đơn giá", "Tồn"};
        private List<ServiceListItemDTO> data = new ArrayList<>();

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
            ServiceListItemDTO row = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.id();
                case 1 -> row.name();
                case 2 -> row.unitPrice();
                case 3 -> row.stock();
                default -> "";
            };
        }

        public void setData(List<ServiceListItemDTO> items) {
            this.data = new ArrayList<>(items);
            fireTableDataChanged();
        }

        public ServiceListItemDTO getAt(int index) {
            return data.get(index);
        }
    }
}
