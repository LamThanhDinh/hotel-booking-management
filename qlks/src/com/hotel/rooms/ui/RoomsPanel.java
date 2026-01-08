package com.hotel.rooms.ui;

import com.hotel.rooms.application.GetRoomDetailUseCase;
import com.hotel.rooms.application.ListRoomsUseCase;
import com.hotel.rooms.application.RoomDetailDTO;
import com.hotel.rooms.application.RoomSummaryDTO;
import com.hotel.rooms.application.UpdateRoomStatusUseCase;
import com.hotel.rooms.domain.RoomStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    
    // Callbacks for actions
    private Consumer<String> onBookRoom;      // Callback khi đặt phòng (roomId)
    private Consumer<String> onCheckoutRoom;  // Callback khi thanh toán (roomId)

    private final JTextField searchField = new JTextField();
    private final JPanel roomsGrid = new JPanel();
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
    
    // Action buttons
    private JButton bookButton;
    private JButton checkoutButton;
    private JButton checkInButton;
    private JButton markCleanButton;
    private JButton markEmptyButton;
    private JPanel actionPanel;

    public RoomsPanel(ListRoomsUseCase listRoomsUseCase, GetRoomDetailUseCase getRoomDetailUseCase, 
                      UpdateRoomStatusUseCase updateRoomStatusUseCase) {
        this.listRoomsUseCase = listRoomsUseCase;
        this.getRoomDetailUseCase = getRoomDetailUseCase;
        this.updateRoomStatusUseCase = updateRoomStatusUseCase;
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
        JLabel icon = new JLabel("# ");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(PRIMARY);
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
        JLabel searchIcon = new JLabel("O ");
        searchIcon.setForeground(new Color(148, 163, 184));
        searchField.setBorder(null);
        searchField.setPreferredSize(new Dimension(180, 20));
        searchField.setFont(LABEL_FONT);
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Nút Refresh với style mới
        JButton refreshBtn = createStyledButton("Làm mới", new Color(99, 102, 241));
        refreshBtn.setToolTipText("Làm mới danh sách phòng");
        refreshBtn.addActionListener(e -> refreshRooms());
        
        rightPanel.add(searchPanel);
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
        JLabel gridTitle = new JLabel("So do phong");
        gridTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gridTitle.setForeground(new Color(51, 65, 85));
        gridTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        roomsGrid.setLayout(new GridLayout(0, 4, 12, 12));
        roomsGrid.setOpaque(false);
        
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
        JLabel detailTitle = new JLabel("Chi tiet phong");
        detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        detailTitle.setForeground(new Color(51, 65, 85));
        detailTitle.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailRow(infoPanel, gbc, 0, "Ma phong:", idLabel);
        addDetailRow(infoPanel, gbc, 1, "Ten phong:", nameLabel);
        addDetailRow(infoPanel, gbc, 2, "Loai phong:", typeLabel);
        addDetailRow(infoPanel, gbc, 3, "Loai giuong:", bedLabel);
        addDetailRow(infoPanel, gbc, 4, "Gia/dem:", priceLabel);
        addDetailRow(infoPanel, gbc, 5, "Trang thai:", statusLabel);

        // Spacer để đẩy info lên trên
        gbc.gridy = 6;
        gbc.weighty = 1;
        infoPanel.add(Box.createVerticalGlue(), gbc);
        
        // Action buttons panel - 2 rows
        actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Nút đặt phòng - cho phòng TRONG
        bookButton = createActionButton("Dat phong", STATUS_AVAILABLE);
        bookButton.addActionListener(e -> {
            if (currentRoomId != null && onBookRoom != null) {
                onBookRoom.accept(currentRoomId);
            }
        });
        
        // Nút thanh toán - cho phòng DANG_SU_DUNG
        checkoutButton = createActionButton("Thanh toan", STATUS_OCCUPIED);
        checkoutButton.addActionListener(e -> {
            if (currentRoomId != null && onCheckoutRoom != null) {
                onCheckoutRoom.accept(currentRoomId);
            }
        });
        
        // Nút check-in - cho phòng DA_DAT
        checkInButton = createActionButton("Nhan phong", STATUS_RESERVED);
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
        markEmptyButton = createActionButton("Da don xong", STATUS_AVAILABLE);
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
        markCleanButton = createActionButton("Can don dep", STATUS_CLEANING);
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
        
        // Initially hide all buttons
        hideAllActionButtons();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(detailTitle, BorderLayout.NORTH);
        wrapperPanel.add(infoPanel, BorderLayout.CENTER);
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

        panel.add(createLegendItem(STATUS_AVAILABLE, "Trong"));
        panel.add(createLegendItem(STATUS_OCCUPIED, "Dang su dung"));
        panel.add(createLegendItem(STATUS_CLEANING, "Dang don"));
        panel.add(createLegendItem(STATUS_MAINTENANCE, "Bao tri"));
        panel.add(createLegendItem(STATUS_RESERVED, "Da dat"));

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
        if (!roomCards.isEmpty()) {
            selectRoom(roomCards.get(0));
        } else {
            clearDetail();
        }

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
            setOpaque(false);
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
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int offsetY = isHovered ? -2 : 0;
            
            // Shadow
            if (isHovered || isSelected) {
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRoundRect(4, 6 + offsetY, getWidth() - 4, getHeight() - 4, 16, 16);
            }
            
            // Card background
            g2d.setColor(statusColor);
            g2d.fillRoundRect(2, 2 + offsetY, getWidth() - 6, getHeight() - 8, 16, 16);
            
            // Gradient overlay
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 255, 255, isHovered ? 70 : 40),
                0, getHeight(), new Color(0, 0, 0, 20)
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(2, 2 + offsetY, getWidth() - 6, getHeight() - 8, 16, 16);
            
            // Selected border
            if (isSelected) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawRoundRect(3, 3 + offsetY, getWidth() - 8, getHeight() - 10, 14, 14);
            }
            
            g2d.dispose();
        }
    }
}