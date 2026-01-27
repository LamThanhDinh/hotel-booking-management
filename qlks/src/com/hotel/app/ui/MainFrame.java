package com.hotel.app.ui;

import com.hotel.booking.ui.BookingPanel;
import com.hotel.booking.ui.CustomersPanel;
import com.hotel.rooms.ui.RoomsPanel;
import com.hotel.services.ui.ServicesPanel;
import com.hotel.checkout.ui.CheckoutPanel;
import com.hotel.revenue.ui.RevenuePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.formdev.flatlaf.FlatClientProperties;
import com.hotel.common.data.MySqlConnectionProvider;

public class MainFrame extends JFrame {
    // Màu chủ đạo - Palette hiện đại
    private static final Color PRIMARY_COLOR = new Color(99, 102, 241);      // Indigo
    private static final Color PRIMARY_DARK = new Color(79, 70, 229);        // Indigo đậm
    private static final Color SIDEBAR_BG = new Color(30, 41, 59);           // Slate đậm
    private static final Color SIDEBAR_TEXT = new Color(203, 213, 225);      // Slate nhạt
    private static final Color SIDEBAR_HOVER = new Color(51, 65, 85);        // Slate hover
    private static final Color SIDEBAR_ACTIVE = new Color(99, 102, 241);     // Active item
    private static final Color CONTENT_BG = new Color(248, 250, 252);        // Nền sáng
    private static final Color STATUS_BG = new Color(30, 41, 59);            // Status bar
    private static final Color ACCENT = new Color(34, 197, 94);              // Xanh lá accent
    
        // Icons cho menu (vector icons to avoid missing glyphs on some fonts)
        // Order matches initScreens(): Phòng, Đặt phòng, Trả phòng, Dịch vụ, Khách hàng, Doanh thu
        private static final Icon[] MENU_ICONS = {
            Icons.home(16, Color.WHITE),
            Icons.booking(16, Color.WHITE),
            Icons.checkout(16, Color.WHITE),
            Icons.services(16, Color.WHITE),
            Icons.customer(16, Color.WHITE),
            Icons.revenue(16, Color.WHITE)
        };

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JPanel> screens = new LinkedHashMap<>();
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JLabel dbDotLabel;
    private JLabel dbTextLabel;

    private JLabel pageTitleLabel;
    private JLabel pageSubtitleLabel;
    
    // References to panels
    private BookingPanel bookingPanel;
    private CheckoutPanel checkoutPanel;
    private JPanel[] menuButtons;
    private String[] menuNames;

    public MainFrame(RoomsPanel roomsPanel, BookingPanel bookingPanel, ServicesPanel servicesPanel, CheckoutPanel checkoutPanel, RevenuePanel revenuePanel, CustomersPanel customersPanel) {
        super("Quản lý khách sạn");
        this.bookingPanel = bookingPanel;
        this.checkoutPanel = checkoutPanel;
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        contentPanel.setBackground(CONTENT_BG);
        initScreens(roomsPanel, bookingPanel, servicesPanel, checkoutPanel, revenuePanel, customersPanel);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(CONTENT_BG);
        center.add(buildTopBar(), BorderLayout.NORTH);
        center.add(contentPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);
        
        setupKeyboardShortcuts();
        startClock();
        
        // Setup room action callbacks
        setupRoomActions(roomsPanel);

        // Set initial header state and DB indicator
        setCurrentScreen("Phòng");
        refreshDbStatusAsync();
    }
    
    private void setupRoomActions(RoomsPanel roomsPanel) {
        // Khi click "Đặt phòng" từ RoomsPanel
        roomsPanel.setOnBookRoom(roomId -> {
            navigateTo("Đặt phòng", 1);
            if (bookingPanel != null) {
                bookingPanel.setRoomId(roomId);
            }
            updateStatus("Đặt phòng: " + roomId);
        });
        
        // Khi click "Trả phòng" từ RoomsPanel  
        roomsPanel.setOnCheckoutRoom(roomId -> {
            navigateTo("Trả phòng", 2);
            if (checkoutPanel != null) {
                checkoutPanel.setRoomIdForCheckout(roomId);
            }
            updateStatus("Trả phòng: " + roomId);
        });
    }
    
    private void navigateTo(String screenName, int menuIndex) {
        cardLayout.show(contentPanel, screenName);
        setCurrentScreen(screenName);
        // Update menu selection
        if (menuButtons != null && menuIndex >= 0 && menuIndex < menuButtons.length) {
            for (JPanel btn : menuButtons) {
                btn.putClientProperty("selected", false);
                btn.repaint();
            }
            menuButtons[menuIndex].putClientProperty("selected", true);
            menuButtons[menuIndex].repaint();
        }
    }
    private void initScreens(RoomsPanel roomsPanel, BookingPanel bookingPanel, ServicesPanel servicesPanel, CheckoutPanel checkoutPanel, RevenuePanel revenuePanel, CustomersPanel customersPanel) {
        screens.put("Phòng", roomsPanel);
        screens.put("Đặt phòng", bookingPanel);
        screens.put("Trả phòng", checkoutPanel);
        screens.put("Dịch vụ", servicesPanel);
        screens.put("Khách hàng", customersPanel);
        screens.put("Doanh thu", revenuePanel);

        screens.forEach((name, panel) -> {
            panel.setBackground(CONTENT_BG);
            contentPanel.add(panel, name);
        });
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 41, 59),
                    0, getHeight(), new Color(15, 23, 42)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBackground(SIDEBAR_BG);

        // Header với logo đẹp
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_COLOR,
                    getWidth(), getHeight(), PRIMARY_DARK
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(220, 80));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        header.setOpaque(false);
        
        JLabel logoIcon = new JLabel("H");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setPreferredSize(new Dimension(50, 50));
        logoIcon.setHorizontalAlignment(SwingConstants.CENTER);
        logoIcon.setBorder(BorderFactory.createLineBorder(new Color(255,255,255,100), 2));
        
        JPanel logoTextPanel = new JPanel(new GridLayout(2, 1));
        logoTextPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("HOTEL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        JLabel subtitleLabel = new JLabel("Management");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));
        logoTextPanel.add(titleLabel);
        logoTextPanel.add(subtitleLabel);
        
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoPanel.setOpaque(false);
        logoPanel.add(logoIcon);
        logoPanel.add(logoTextPanel);
        header.add(logoPanel, BorderLayout.CENTER);

        // Menu panel với custom buttons
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(15, 12, 15, 12));
        
        menuNames = screens.keySet().toArray(new String[0]);
        menuButtons = new JPanel[menuNames.length];
        
        for (int i = 0; i < menuNames.length; i++) {
            final int index = i;
            final String name = menuNames[i];
            
            JPanel btn = createMenuButton(MENU_ICONS[i], name, "F" + (i + 1), index == 0);
            menuButtons[i] = btn;
            
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Reset all buttons
                    for (JPanel b : menuButtons) {
                        b.setBackground(new Color(0,0,0,0));
                        b.putClientProperty("selected", false);
                        b.repaint();
                    }
                    // Select this button
                    btn.putClientProperty("selected", true);
                    btn.repaint();
                    cardLayout.show(contentPanel, name);
                    setCurrentScreen(name);
                    updateStatus("Đang xem: " + name);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!Boolean.TRUE.equals(btn.getClientProperty("selected"))) {
                        btn.setBackground(SIDEBAR_HOVER);
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!Boolean.TRUE.equals(btn.getClientProperty("selected"))) {
                        btn.setBackground(new Color(0,0,0,0));
                    }
                }
            });
            
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(5));
        }
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createMenuButton(Icon icon, String text, String shortcut, boolean selected) {
        JPanel btn = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (Boolean.TRUE.equals(getClientProperty("selected"))) {
                    // Gradient cho item được chọn
                    GradientPaint gp = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        getWidth(), 0, PRIMARY_DARK
                    );
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (getBackground().getAlpha() > 0) {
                    g2d.setColor(getBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2d.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(12, 15, 12, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("selected", selected);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        
        // Icon circle
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillOval(0, 0, getWidth(), getHeight());
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        iconLabel.setIcon(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(28, 28));
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(SIDEBAR_TEXT);
        
        JLabel shortcutLabel = new JLabel(shortcut);
        shortcutLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        shortcutLabel.setForeground(new Color(148, 163, 184));
        
        btn.add(iconLabel, BorderLayout.WEST);
        btn.add(textLabel, BorderLayout.CENTER);
        btn.add(shortcutLabel, BorderLayout.EAST);
        
        return btn;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 41, 59),
                    getWidth(), 0, new Color(15, 23, 42)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        panel.setPreferredSize(new Dimension(0, 40));

        statusLabel = new JLabel(">> Sẵn sàng");
        statusLabel.setForeground(new Color(148, 163, 184));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        timeLabel = new JLabel();
        timeLabel.setForeground(new Color(148, 163, 184));
        timeLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // DB Status với icon dot
        JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dbPanel.setOpaque(false);
        dbDotLabel = new JLabel();
        dbDotLabel.setIcon(Icons.dot(8, new Color(148, 163, 184)));
        dbTextLabel = new JLabel("In-memory");
        dbTextLabel.setForeground(new Color(148, 163, 184));
        dbTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dbPanel.add(dbDotLabel);
        dbPanel.add(dbTextLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(dbPanel);
        rightPanel.add(timeLabel);

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(CONTENT_BG);
        bar.setBorder(new EmptyBorder(18, 22, 12, 22));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        pageTitleLabel = new JLabel("Phòng");
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitleLabel.setForeground(new Color(15, 23, 42));

        pageSubtitleLabel = new JLabel("F1–F6 để chuyển nhanh");
        pageSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageSubtitleLabel.setForeground(new Color(100, 116, 139));

        titlePanel.add(pageTitleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(pageSubtitleLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton refreshButton = new JButton("Làm mới");
        refreshButton.setIcon(Icons.refresh(16, new Color(71, 85, 105)));
        refreshButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        refreshButton.setFocusable(false);
        refreshButton.addActionListener(e -> refreshCurrentScreen());

        JButton helpButton = new JButton("Phím tắt");
        helpButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        helpButton.setFocusable(false);
        helpButton.addActionListener(e -> showShortcutsDialog());

        actions.add(refreshButton);
        actions.add(helpButton);

        bar.add(titlePanel, BorderLayout.WEST);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private void setCurrentScreen(String screenName) {
        if (pageTitleLabel != null) {
            pageTitleLabel.setText(screenName);
        }
        if (pageSubtitleLabel != null) {
            switch (screenName) {
                case "Phòng" -> pageSubtitleLabel.setText("Xem nhanh trạng thái & thao tác phòng");
                case "Đặt phòng" -> pageSubtitleLabel.setText("Tạo booking mới và chọn phòng");
                case "Trả phòng" -> pageSubtitleLabel.setText("Tính tiền, dịch vụ và xuất hoá đơn");
                case "Dịch vụ" -> pageSubtitleLabel.setText("Quản lý dịch vụ và sử dụng dịch vụ");
                case "Khách hàng" -> pageSubtitleLabel.setText("Danh sách khách hàng");
                case "Doanh thu" -> pageSubtitleLabel.setText("Báo cáo doanh thu theo hoá đơn");
                default -> pageSubtitleLabel.setText("F1–F6 để chuyển nhanh");
            }
        }
    }

    private void refreshCurrentScreen() {
        String currentName = pageTitleLabel != null ? pageTitleLabel.getText() : null;
        if (currentName == null || currentName.isBlank()) {
            return;
        }
        JPanel panel = screens.get(currentName);
        if (panel == null) {
            updateStatus(">> Không tìm thấy màn hình: " + currentName);
            return;
        }

        // Best-effort refresh: call refreshData() if present
        try {
            panel.getClass().getMethod("refreshData").invoke(panel);
            updateStatus(">> Đã làm mới: " + currentName);
        } catch (NoSuchMethodException ignored) {
            updateStatus(">> Màn hình này không hỗ trợ làm mới");
        } catch (Exception ex) {
            updateStatus(">> Lỗi làm mới: " + ex.getClass().getSimpleName());
        }
    }

    private void showShortcutsDialog() {
        String message = "Phím tắt:\n" +
                "- F1: Phòng\n" +
                "- F2: Đặt phòng\n" +
                "- F3: Trả phòng\n" +
                "- F4: Dịch vụ\n" +
                "- F5: Khách hàng\n" +
                "- F6: Doanh thu\n";
        JOptionPane.showMessageDialog(this, message, "Phím tắt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshDbStatusAsync() {
        Properties props = loadProperties();
        boolean enabled = Boolean.parseBoolean(props.getProperty("db.enabled", "false"));

        if (dbDotLabel == null || dbTextLabel == null) {
            return;
        }

        if (!enabled) {
            setDbIndicator(new Color(148, 163, 184), "In-memory", "db.enabled=false (không dùng MySQL)");
            return;
        }

        // Show checking state immediately
        setDbIndicator(new Color(251, 191, 36), "MySQL (đang kiểm tra...)", null);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Exception failure;

            @Override
            protected Void doInBackground() {
                try {
                    MySqlConnectionProvider provider = MySqlConnectionProvider.fromProperties(props);
                    try (var conn = provider.openNewConnection()) {
                        // ok
                    }
                } catch (Exception ex) {
                    failure = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure == null) {
                    setDbIndicator(ACCENT, "MySQL connected", "db.enabled=true");
                } else {
                    setDbIndicator(new Color(239, 68, 68), "MySQL lỗi kết nối", failure.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setDbIndicator(Color color, String text, String tooltip) {
        if (dbDotLabel != null) {
            dbDotLabel.setIcon(Icons.dot(8, color));
        }
        if (dbTextLabel != null) {
            dbTextLabel.setForeground(color);
            dbTextLabel.setText(text);
            dbTextLabel.setToolTipText(tooltip);
        }
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("app.properties")) {
            properties.load(fis);
        } catch (IOException ignored) {
            // defaults
        }
        return properties;
    }

    private void setupKeyboardShortcuts() {
        // F1-F6 để chuyển tab
        String[] keys = {"Phòng", "Đặt phòng", "Trả phòng", "Dịch vụ", "Khách hàng", "Doanh thu"};
        for (int i = 0; i < keys.length; i++) {
            final int index = i;
            final String screenName = keys[i];
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i, 0), "switch" + i);
            getRootPane().getActionMap().put("switch" + i, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    navigateTo(screenName, index);
                    updateStatus("Chuyển đến: " + screenName);
                }
            });
        }

        // Escape để đóng dialog
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        getRootPane().getActionMap().put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Không làm gì, có thể mở rộng sau
            }
        });
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            timeLabel.setText(time);
        });
        timer.start();
        // Cập nhật ngay lập tức
        timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private JPanel placeholder(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        JLabel label = new JLabel("[!] " + name + " (chưa triển khai)", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(new Color(127, 140, 141));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}