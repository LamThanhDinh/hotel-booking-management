package com.hotel.rooms.ui;

import com.hotel.rooms.application.DeleteRoomUseCase;
import com.hotel.rooms.application.GetRoomDetailUseCase;
import com.hotel.rooms.application.ListRoomsUseCase;
import com.hotel.rooms.application.RoomDetailDTO;
import com.hotel.rooms.application.RoomSummaryDTO;
import com.hotel.rooms.application.SaveRoomUseCase;
import com.hotel.rooms.application.UpdateRoomStatusUseCase;
import com.hotel.rooms.domain.RoomStatus;
import com.hotel.app.ui.Icons;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RoomsPanel extends JPanel {
    // Màu chủ đạo - Palette hiện đại
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color CONTENT_BG = new Color(248, 250, 252);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // Màu trạng thái phòng - Modern pastel palette
    private static final Color STATUS_AVAILABLE = new Color(34, 197, 94);      // Emerald - Trống
    private static final Color STATUS_OCCUPIED = new Color(239, 68, 68);       // Red - Đang sử dụng
    private static final Color STATUS_CLEANING = new Color(251, 191, 36);      // Amber - Đang dọn
    private static final Color STATUS_MAINTENANCE = new Color(156, 163, 175);  // Gray - Bảo trì
    private static final Color STATUS_RESERVED = new Color(139, 92, 246);      // Violet - Đã đặt
    
    private final ListRoomsUseCase listRoomsUseCase;
    private final GetRoomDetailUseCase getRoomDetailUseCase;
    private final UpdateRoomStatusUseCase updateRoomStatusUseCase;
    private final SaveRoomUseCase saveRoomUseCase;
    private final DeleteRoomUseCase deleteRoomUseCase;
    
    // Callbacks for actions
    private Consumer<String> onBookRoom;      // Callback khi đặt phòng (roomId)
    private Consumer<String> onCheckoutRoom;  // Callback khi thanh toán (roomId)

    private final JTextField searchField = new JTextField();
    private final JPanel roomsGrid = new JPanel();
    private final JLabel emptyStateLabel = new JLabel("Không có phòng nào phù hợp", SwingConstants.CENTER);
    private final List<RoomCard> roomCards = new ArrayList<>();
    private RoomCard selectedCard = null;
    private String currentRoomId = null;
    private String currentRoomStatus = null;

    // Chi tiết phòng
    private final JLabel idLabel = new JLabel("-");
    private final JLabel nameLabel = new JLabel("-");
    private final JLabel typeLabel = new JLabel("-");
    private final JLabel bedLabel = new JLabel("-");
    private final JLabel priceLabel = new JLabel("-");
    private final JLabel statusLabel = new JLabel("-");
    private final JLabel customerLabel = new JLabel("-");
    private final JLabel checkInLabel = new JLabel("-");
    private final JLabel checkOutLabel = new JLabel("-");
    
    // Action buttons
    private JButton bookButton;
    private JButton checkoutButton;
    private JButton checkInButton;
    private JButton markCleanButton;
    private JButton markEmptyButton;
    private JPanel actionPanel;

    public RoomsPanel(ListRoomsUseCase listRoomsUseCase, GetRoomDetailUseCase getRoomDetailUseCase, 
                      UpdateRoomStatusUseCase updateRoomStatusUseCase,
                      SaveRoomUseCase saveRoomUseCase,
                      DeleteRoomUseCase deleteRoomUseCase) {
        this.listRoomsUseCase = listRoomsUseCase;
        this.getRoomDetailUseCase = getRoomDetailUseCase;
        this.updateRoomStatusUseCase = updateRoomStatusUseCase;
        this.saveRoomUseCase = saveRoomUseCase;
        this.deleteRoomUseCase = deleteRoomUseCase;
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);

        wireEvents();
        refreshRooms();
    }

    public void refreshData() {
        refreshRooms();
    }
    
    // Setter cho callback đặt phòng
    public void setOnBookRoom(Consumer<String> callback) {
        this.onBookRoom = callback;
    }
    
    // Setter cho callback thanh toán
    public void setOnCheckoutRoom(Consumer<String> callback) {
        this.onCheckoutRoom = callback;
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(CONTENT_BG);
        JLabel icon = new JLabel(Icons.home(18, PRIMARY));
        icon.setBorder(new EmptyBorder(0, 0, 0, 8));
        JLabel lbl = new JLabel("Quản lý Phòng");
        lbl.setFont(TITLE_FONT);
        lbl.setForeground(new Color(30, 41, 59));
        titlePanel.add(icon);
        titlePanel.add(lbl);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setBackground(CONTENT_BG);
        
        // Search field với style mới
        JPanel searchPanel = new JPanel(new BorderLayout(0, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel searchIcon = new JLabel(Icons.search(16, new Color(148, 163, 184)));
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 8));
        searchField.setBorder(null);
        searchField.setPreferredSize(new Dimension(220, 20));
        searchField.setFont(LABEL_FONT);
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo mã/tên/loại phòng...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Nút Thêm phòng mới
        JButton addBtn = createStyledButton("Thêm phòng", new Color(34, 197, 94));
        addBtn.setToolTipText("Thêm phòng mới");
        addBtn.addActionListener(e -> handleAddRoom());
        
        // Nút Refresh với style mới
        JButton refreshBtn = createStyledButton("Làm mới", new Color(99, 102, 241));
        refreshBtn.setToolTipText("Làm mới danh sách phòng");
        refreshBtn.addActionListener(e -> refreshRooms());
        
        rightPanel.add(searchPanel);
        rightPanel.add(addBtn);
        rightPanel.add(refreshBtn);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2d.setFont(getFont());
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        btn.setFont(LABEL_FONT);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(100, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JComponent buildContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.7);
        split.setDividerLocation(680);
        split.setBorder(null);
        split.setDividerSize(1);

        // Panel chứa grid phòng với card style
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header cho grid
        JLabel gridTitle = new JLabel("Sơ đồ phòng");
        gridTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gridTitle.setForeground(new Color(51, 65, 85));
        gridTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        roomsGrid.setLayout(new GridLayout(0, 4, 12, 12));
        roomsGrid.setOpaque(false);

        emptyStateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptyStateLabel.setForeground(new Color(100, 116, 139));
        emptyStateLabel.setBorder(new EmptyBorder(60, 20, 60, 20));
        
        JScrollPane scrollPane = new JScrollPane(roomsGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        leftPanel.add(gridTitle, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel chi tiết
        JPanel detailPanel = buildDetailPanel();
        
        split.setLeftComponent(leftPanel);
        split.setRightComponent(detailPanel);
        return split;
    }

    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel detailTitle = new JLabel("Chi tiết phòng");
        detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        detailTitle.setForeground(new Color(51, 65, 85));
        detailTitle.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailRow(infoPanel, gbc, 0, "Mã phòng:", idLabel);
        addDetailRow(infoPanel, gbc, 1, "Tên phòng:", nameLabel);
        addDetailRow(infoPanel, gbc, 2, "Loại phòng:", typeLabel);
        addDetailRow(infoPanel, gbc, 3, "Loại giường:", bedLabel);
        addDetailRow(infoPanel, gbc, 4, "Giá/đêm:", priceLabel);
        addDetailRow(infoPanel, gbc, 5, "Trạng thái:", statusLabel);
        addDetailRow(infoPanel, gbc, 6, "Khách hàng:", customerLabel);
        addDetailRow(infoPanel, gbc, 7, "Check-in:", checkInLabel);
        addDetailRow(infoPanel, gbc, 8, "Check-out:", checkOutLabel);

        // Spacer để đẩy info lên trên
        gbc.gridy = 9;
        gbc.weighty = 1;
        infoPanel.add(Box.createVerticalGlue(), gbc);
        
        // Action buttons panel - 2 rows
        actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Nút đặt phòng - cho phòng TRONG
        bookButton = createActionButton("Đặt phòng", STATUS_AVAILABLE);
        bookButton.addActionListener(e -> {
            if (currentRoomId != null && onBookRoom != null) {
                onBookRoom.accept(currentRoomId);
            }
        });
        
        // Nút thanh toán - cho phòng DANG_SU_DUNG
        checkoutButton = createActionButton("Thanh toán", STATUS_OCCUPIED);
        checkoutButton.addActionListener(e -> {
            if (currentRoomId != null && onCheckoutRoom != null) {
                onCheckoutRoom.accept(currentRoomId);
            }
        });
        
        // Nút check-in - cho phòng DA_DAT
        checkInButton = createActionButton("Nhận phòng", STATUS_RESERVED);
        checkInButton.addActionListener(e -> {
            if (currentRoomId != null) {
                updateRoomStatusUseCase.markAsInUse(currentRoomId);
                refreshRooms();
                loadDetail(currentRoomId);
                JOptionPane.showMessageDialog(this, 
                    "Da nhan phong thanh cong!", 
                    "Thong bao", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Nút đánh dấu đã dọn xong - cho phòng DA_TRA (đang dọn)
        markEmptyButton = createActionButton("Đã dọn xong", STATUS_AVAILABLE);
        markEmptyButton.addActionListener(e -> {
            if (currentRoomId != null) {
                updateRoomStatusUseCase.markAsEmpty(currentRoomId);
                refreshRooms();
                loadDetail(currentRoomId);
                JOptionPane.showMessageDialog(this, 
                    "Phong da san sang!", 
                    "Thong bao", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Nút đánh dấu cần dọn - cho phòng DANG_SU_DUNG (sau checkout)
        markCleanButton = createActionButton("Cần dọn dẹp", STATUS_CLEANING);
        markCleanButton.addActionListener(e -> {
            if (currentRoomId != null) {
                updateRoomStatusUseCase.markAsCleaning(currentRoomId);
                refreshRooms();
                loadDetail(currentRoomId);
            }
        });
        
        actionPanel.add(bookButton);
        actionPanel.add(checkoutButton);
        actionPanel.add(checkInButton);
        actionPanel.add(markEmptyButton);
        
        // Nút Sửa thông tin phòng
        JButton editButton = createActionButton("Sửa thông tin", new Color(52, 152, 219));
        editButton.addActionListener(e -> handleEditRoom());
        
        // Nút Xóa phòng
        JButton deleteButton = createActionButton("Xóa phòng", new Color(231, 76, 60));
        deleteButton.addActionListener(e -> handleDeleteRoom());
        
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        // Initially hide all buttons
        hideAllActionButtons();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(detailTitle, BorderLayout.NORTH);
        
        // Wrap infoPanel in JScrollPane for scrolling
        JScrollPane infoScrollPane = new JScrollPane(infoPanel);
        infoScrollPane.setBorder(null);
        infoScrollPane.setOpaque(false);
        infoScrollPane.getViewport().setOpaque(false);
        infoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        wrapperPanel.add(infoScrollPane, BorderLayout.CENTER);
        wrapperPanel.add(actionPanel, BorderLayout.SOUTH);
        
        panel.add(wrapperPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void hideAllActionButtons() {
        bookButton.setVisible(false);
        checkoutButton.setVisible(false);
        checkInButton.setVisible(false);
        markEmptyButton.setVisible(false);
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color color = bgColor;
                if (getModel().isPressed()) {
                    color = bgColor.darker();
                } else if (getModel().isRollover()) {
                    color = bgColor.brighter();
                }
                
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                g2d.setFont(getFont());
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void updateActionButtons(String status) {
        // Ẩn tất cả các nút trước
        hideAllActionButtons();
        
        if (status == null) {
            return;
        }
        
        String s = status.toUpperCase().replace("_", "");
        
        // TRONG - có thể đặt phòng
        if (s.contains("TRONG") || s.equals("AVAILABLE")) {
            bookButton.setVisible(true);
        }
        // DANG_SU_DUNG - có thể thanh toán
        else if (s.contains("DANGSUDUNG") || s.contains("OCCUPIED")) {
            checkoutButton.setVisible(true);
        }
        // DA_DAT - có thể nhận phòng (check-in)
        else if (s.contains("DADAT") || s.contains("BOOKED") || s.contains("RESERVED")) {
            checkInButton.setVisible(true);
        }
        // DA_TRA - đang dọn, có thể đánh dấu đã dọn xong
        else if (s.contains("DATRA") || s.contains("CLEANING")) {
            markEmptyButton.setVisible(true);
        }
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(100, 116, 139));
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(new Color(30, 41, 59));
        panel.add(valueLabel, gbc);
    }

    private JPanel buildLegend() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 5, 0));

        panel.add(createLegendItem(STATUS_AVAILABLE, "Trống"));
        panel.add(createLegendItem(STATUS_OCCUPIED, "Đang sử dụng"));
        panel.add(createLegendItem(STATUS_CLEANING, "Đang dọn"));
        panel.add(createLegendItem(STATUS_MAINTENANCE, "Bảo trì"));
        panel.add(createLegendItem(STATUS_RESERVED, "Đã đặt"));

        return panel;
    }

    private JPanel createLegendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);
        
        // Bo góc cho color box
        JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2d.dispose();
            }
        };
        colorBox.setPreferredSize(new Dimension(14, 14));
        colorBox.setOpaque(false);
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(71, 85, 105));
        
        item.add(colorBox);
        item.add(label);
        return item;
    }

    private void wireEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refreshRooms(); }
            @Override
            public void removeUpdate(DocumentEvent e) { refreshRooms(); }
            @Override
            public void changedUpdate(DocumentEvent e) { refreshRooms(); }
        });
    }

    private void refreshRooms() {
        List<RoomSummaryDTO> rooms = listRoomsUseCase.execute(searchField.getText());
        
        roomsGrid.removeAll();
        roomCards.clear();
        selectedCard = null;

        // Empty state
        if (rooms == null || rooms.isEmpty()) {
            roomsGrid.setLayout(new BorderLayout());
            roomsGrid.add(emptyStateLabel, BorderLayout.CENTER);
            clearDetail();
            roomsGrid.revalidate();
            roomsGrid.repaint();
            return;
        }

        // Ensure grid layout when there is data
        if (!(roomsGrid.getLayout() instanceof GridLayout)) {
            roomsGrid.setLayout(new GridLayout(0, 4, 12, 12));
        }

        for (RoomSummaryDTO room : rooms) {
            RoomCard card = new RoomCard(room);
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectRoom(card);
                }
            });
            roomCards.add(card);
            roomsGrid.add(card);
        }

        // Chọn phòng đầu tiên nếu có
        selectRoom(roomCards.get(0));

        roomsGrid.revalidate();
        roomsGrid.repaint();
    }

    private void selectRoom(RoomCard card) {
        // Bỏ chọn card cũ
        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }
        
        // Chọn card mới
        selectedCard = card;
        selectedCard.setSelected(true);
        
        // Load chi tiết
        loadDetail(card.getRoomId());
    }

    private void loadDetail(String roomId) {
        Optional<RoomDetailDTO> detail = getRoomDetailUseCase.execute(roomId);
        if (detail.isPresent()) {
            RoomDetailDTO dto = detail.get();
            currentRoomId = dto.id();
            currentRoomStatus = dto.status();
            
            idLabel.setText(dto.id());
            nameLabel.setText(dto.name());
            typeLabel.setText(dto.roomType());
            bedLabel.setText(dto.bedType());
            priceLabel.setText(dto.price());
            statusLabel.setText(dto.status());
            
            // Hiển thị thông tin booking nếu có
            if (dto.customerName() != null && !dto.customerName().isEmpty()) {
                customerLabel.setText(dto.customerName());
                checkInLabel.setText(dto.checkInDate());
                checkOutLabel.setText(dto.checkOutDate());
            } else {
                customerLabel.setText("-");
                checkInLabel.setText("-");
                checkOutLabel.setText("-");
            }
            
            // Đổi màu trạng thái
            statusLabel.setForeground(getStatusColor(dto.status()));
            
            // Cập nhật buttons theo trạng thái
            updateActionButtons(dto.status());
        } else {
            clearDetail();
        }
    }

    private void clearDetail() {
        currentRoomId = null;
        currentRoomStatus = null;
        idLabel.setText("-");
        nameLabel.setText("-");
        typeLabel.setText("-");
        bedLabel.setText("-");
        priceLabel.setText("-");
        statusLabel.setText("-");
        statusLabel.setForeground(new Color(44, 62, 80));
        updateActionButtons(null);
    }

    private Color getStatusColor(String status) {
        if (status == null) return STATUS_MAINTENANCE;
        String s = status.toUpperCase().replace("_", "").replace(" ", "");
        
        // TRONG - Trống/Available
        if (s.contains("TRONG") || s.equals("AVAILABLE")) {
            return STATUS_AVAILABLE;
        }
        // DANG_SU_DUNG - Đang sử dụng/Occupied  
        else if (s.contains("DANGSUDUNG") || s.contains("OCCUPIED") || s.contains("SUDUNG")) {
            return STATUS_OCCUPIED;
        }
        // DA_TRA - Đã trả (cần dọn)/Cleaning
        else if (s.contains("DATRA") || s.contains("CLEANING") || s.contains("DONDEP")) {
            return STATUS_CLEANING;
        }
        // DA_DAT - Đã đặt/Reserved
        else if (s.contains("DADAT") || s.contains("RESERVED")) {
            return STATUS_RESERVED;
        }
        // Mặc định - Bảo trì
        return STATUS_MAINTENANCE;
    }

    // Inner class: Card hiển thị phòng
    private class RoomCard extends JPanel {
        private final RoomSummaryDTO room;
        private boolean isSelected = false;
        private boolean isHovered = false;
        private final Color statusColor;
        private final int iconType;

        public RoomCard(RoomSummaryDTO room) {
            this.room = room;
            this.statusColor = getStatusColor(room.status());
            this.iconType = getRoomIconType(room.roomType());
            
            setPreferredSize(new Dimension(145, 115));
            // Opaque background avoids black edge artifacts on some GPUs/DPIs.
            setOpaque(true);
            setBackground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setLayout(new BorderLayout(5, 5));
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setToolTipText(buildTooltip());
            
            // Hover effect
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });

            // Panel chứa icon
            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    drawRoomIcon((Graphics2D) g, getWidth(), getHeight());
                }
            };
            iconPanel.setOpaque(false);
            iconPanel.setPreferredSize(new Dimension(60, 45));

            // Tên phòng
            JLabel nameLabel = new JLabel(room.name());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // Loại phòng
            JLabel typeLabel = new JLabel(room.roomType());
            typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            typeLabel.setForeground(new Color(255, 255, 255, 200));
            typeLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // Layout
            JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            centerPanel.setOpaque(false);
            centerPanel.add(iconPanel);
            
            JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            bottomPanel.setOpaque(false);
            bottomPanel.add(nameLabel);
            bottomPanel.add(typeLabel);

            add(centerPanel, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        private int getRoomIconType(String roomType) {
            if (roomType == null) return 0;
            String t = roomType.toUpperCase();
            if (t.contains("VIP") || t.contains("SUITE")) return 1;
            if (t.contains("DELUXE") || t.contains("DOI") || t.contains("DOUBLE")) return 2;
            if (t.contains("DON") || t.contains("SINGLE")) return 3;
            if (t.contains("GIA DINH") || t.contains("FAMILY")) return 4;
            return 0; // Standard
        }

        private void drawRoomIcon(Graphics2D g2d, int w, int h) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            
            int cx = w / 2;
            int cy = h / 2;
            
            switch (iconType) {
                case 1 -> drawVipIcon(g2d, cx, cy);      // VIP - ngôi sao
                case 2 -> drawDoubleIcon(g2d, cx, cy);  // Double/Deluxe - giường đôi
                case 3 -> drawSingleIcon(g2d, cx, cy);  // Single - giường đơn
                case 4 -> drawFamilyIcon(g2d, cx, cy);  // Family - nhà
                default -> drawStandardIcon(g2d, cx, cy); // Standard - giường thường
            }
        }

        // Vẽ icon giường chuẩn
        private void drawStandardIcon(Graphics2D g, int cx, int cy) {
            g.setStroke(new BasicStroke(2f));
            // Khung giường
            g.drawRoundRect(cx - 20, cy - 5, 40, 20, 5, 5);
            // Gối
            g.fillRoundRect(cx - 17, cy - 2, 12, 8, 3, 3);
            // Chân giường
            g.fillRect(cx - 18, cy + 15, 4, 6);
            g.fillRect(cx + 14, cy + 15, 4, 6);
        }

        // Vẽ icon VIP - ngôi sao
        private void drawVipIcon(Graphics2D g, int cx, int cy) {
            int[] xPoints = new int[10];
            int[] yPoints = new int[10];
            int outerR = 18;
            int innerR = 8;
            
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI / 2 + i * Math.PI / 5;
                int r = (i % 2 == 0) ? outerR : innerR;
                xPoints[i] = cx + (int) (r * Math.cos(angle));
                yPoints[i] = cy - (int) (r * Math.sin(angle));
            }
            g.fillPolygon(xPoints, yPoints, 10);
            
            // Thêm chữ VIP nhỏ bên dưới
            g.setFont(new Font("Segoe UI", Font.BOLD, 9));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("VIP", cx - fm.stringWidth("VIP") / 2, cy + 28);
        }

        // Vẽ icon giường đôi
        private void drawDoubleIcon(Graphics2D g, int cx, int cy) {
            g.setStroke(new BasicStroke(2f));
            // Khung giường rộng hơn
            g.drawRoundRect(cx - 25, cy - 5, 50, 22, 5, 5);
            // 2 gối
            g.fillRoundRect(cx - 22, cy - 2, 10, 8, 3, 3);
            g.fillRoundRect(cx + 12, cy - 2, 10, 8, 3, 3);
            // Đường chia giữa
            g.drawLine(cx, cy - 2, cx, cy + 14);
            // Chân giường
            g.fillRect(cx - 23, cy + 17, 4, 6);
            g.fillRect(cx + 19, cy + 17, 4, 6);
        }

        // Vẽ icon giường đơn nhỏ
        private void drawSingleIcon(Graphics2D g, int cx, int cy) {
            g.setStroke(new BasicStroke(2f));
            // Khung giường nhỏ
            g.drawRoundRect(cx - 15, cy - 5, 30, 18, 5, 5);
            // 1 gối
            g.fillRoundRect(cx - 12, cy - 2, 10, 7, 3, 3);
            // Chân giường
            g.fillRect(cx - 13, cy + 13, 3, 5);
            g.fillRect(cx + 10, cy + 13, 3, 5);
        }

        // Vẽ icon gia đình - hình nhà
        private void drawFamilyIcon(Graphics2D g, int cx, int cy) {
            g.setStroke(new BasicStroke(2f));
            // Mái nhà
            int[] xRoof = {cx - 22, cx, cx + 22};
            int[] yRoof = {cy - 2, cy - 18, cy - 2};
            g.fillPolygon(xRoof, yRoof, 3);
            // Thân nhà
            g.fillRect(cx - 18, cy - 2, 36, 22);
            // Cửa
            g.setColor(statusColor);
            g.fillRect(cx - 5, cy + 5, 10, 15);
            g.setColor(Color.WHITE);
            // Cửa sổ
            g.fillRect(cx - 15, cy + 2, 7, 7);
            g.fillRect(cx + 8, cy + 2, 7, 7);
        }

        private String buildTooltip() {
            return "<html><b>" + room.name() + "</b><br>" +
                   "Loại: " + room.roomType() + "<br>" +
                   "Giường: " + room.bedType() + "<br>" +
                   "Giá: " + room.price() + "<br>" +
                   "Trạng thái: " + room.status() + "</html>";
        }

        public String getRoomId() {
            return room.id();
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int hoverLift = isHovered ? -2 : 0;
                int x = 2;
                int y = 2 + hoverLift;
                int w = getWidth() - 6;
                int h = getHeight() - 8;
                float arc = 16f;

                if (w <= 0 || h <= 0) {
                    return;
                }

                // Soft shadow (multi-layer for smoother look)
                // Keep shadow very subtle to avoid dark borders
                if (isHovered) {
                    Shape s = new RoundRectangle2D.Float(x + 1, y + 5, w, h, arc, arc);
                    g2d.setColor(new Color(0, 0, 0, 10));
                    g2d.fill(s);
                }

                // Gradient background based on status color (less "flat" and avoids banding artifacts)
                Color top = lighten(statusColor, isHovered ? 0.18f : 0.12f);
                Color bottom = darken(statusColor, isHovered ? 0.08f : 0.14f);
                Paint bg = new GradientPaint(0, y, top, 0, y + h, bottom);
                Shape card = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
                g2d.setPaint(bg);
                g2d.fill(card);

                // Subtle gloss overlay
                Paint gloss = new GradientPaint(0, y, new Color(255, 255, 255, isHovered ? 55 : 35), 0, y + h, new Color(255, 255, 255, 0));
                g2d.setPaint(gloss);
                g2d.fill(card);

                // No extra border (keeps edges clean)

                // Selected border
                if (isSelected) {
                    g2d.setColor(new Color(255, 255, 255, 220));
                    g2d.setStroke(new BasicStroke(2.2f));
                    Shape sel = new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, arc - 2, arc - 2);
                    g2d.draw(sel);
                }
            } finally {
                g2d.dispose();
            }
        }

        private Color lighten(Color c, float amount) {
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            hsb[2] = Math.min(1f, hsb[2] + amount);
            hsb[1] = Math.max(0f, hsb[1] - amount * 0.25f);
            return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        }

        private Color darken(Color c, float amount) {
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            hsb[2] = Math.max(0f, hsb[2] - amount);
            return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        }
    }
    
    private void handleEditRoom() {
        if (currentRoomId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create edit dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa thông tin phòng", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Room ID (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã phòng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField idField = new JTextField(currentRoomId);
        idField.setEditable(false);
        idField.setFocusable(false);
        idField.setBackground(new Color(220, 220, 220));
        idField.setForeground(new Color(100, 100, 100));
        formPanel.add(idField, gbc);
        
        // Room Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Tên phòng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField nameField = new JTextField(nameLabel.getText());
        formPanel.add(nameField, gbc);
        
        // Room Price
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Giá phòng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        String priceText = priceLabel.getText().replaceAll("[^0-9.]", "");
        JTextField priceField = new JTextField(priceText);
        formPanel.add(priceField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Lưu");
        JButton cancelBtn = new JButton("Hủy");
        
        saveBtn.addActionListener(e -> {
            try {
                String newName = nameField.getText().trim();
                String priceStr = priceField.getText().trim();
                
                if (newName.isEmpty() || priceStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double price = Double.parseDouble(priceStr);
                saveRoomUseCase.execute(currentRoomId, newName, java.math.BigDecimal.valueOf(price));
                
                JOptionPane.showMessageDialog(dialog, "Cập nhật thông tin phòng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshRooms();
                loadDetail(currentRoomId);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá phòng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void handleAddRoom() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm phòng mới", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Room ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Mã phòng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField idField = new JTextField("R" + System.currentTimeMillis());
        formPanel.add(idField, gbc);
        
        // Room Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Tên phòng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField nameField = new JTextField();
        formPanel.add(nameField, gbc);
        
        // Room Type
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Loại phòng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite", "VIP"});
        formPanel.add(typeCombo, gbc);
        
        // Bed Type
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Loại giường:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JComboBox<String> bedCombo = new JComboBox<>(new String[]{"Single", "Double", "Twin", "King"});
        formPanel.add(bedCombo, gbc);
        
        // Room Price
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(new JLabel("Giá phòng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField priceField = new JTextField();
        formPanel.add(priceField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Lưu");
        JButton cancelBtn = new JButton("Hủy");
        
        saveBtn.addActionListener(e -> {
            try {
                String roomId = idField.getText().trim();
                String name = nameField.getText().trim();
                String roomType = (String) typeCombo.getSelectedItem();
                String bedType = (String) bedCombo.getSelectedItem();
                String priceStr = priceField.getText().trim();
                
                if (roomId.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double price = Double.parseDouble(priceStr);
                
                // Tạo phòng mới thông qua SaveRoomUseCase
                // Note: Cần thêm CreateRoomUseCase riêng, tạm thời dùng SaveRoomUseCase
                saveRoomUseCase.execute(roomId, name, java.math.BigDecimal.valueOf(price));
                
                JOptionPane.showMessageDialog(dialog, "Thêm phòng mới thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshRooms();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá phòng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void handleDeleteRoom() {
        if (currentRoomId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa phòng: " + nameLabel.getText() + "?\\nChỉ có thể xóa phòng đang trống hoặc đã trả.",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = deleteRoomUseCase.execute(currentRoomId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa phòng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    refreshRooms();
                    clearDetail();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa phòng! Phòng phải ở trạng thái Trống hoặc Đã trả.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}