package com.hotel.booking.ui;

import com.hotel.booking.application.CreateBookingRequest;
import com.hotel.booking.application.CreateBookingResponse;
import com.hotel.booking.application.CreateBookingUseCase;
import com.hotel.booking.application.Result;
import com.hotel.booking.application.BookingError;
import com.hotel.rooms.application.ListRoomsUseCase;
import com.hotel.rooms.application.RoomSummaryDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BookingPanel extends JPanel {
    private static final Color PRIMARY = new Color(41, 128, 185);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER = new Color(192, 57, 43);
    private static final Color CONTENT_BG = new Color(248, 249, 250);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final CreateBookingUseCase createBookingUseCase;
    private final ListRoomsUseCase listRoomsUseCase;
    private final Runnable onBookingCreated;

    private final JComboBox<String> roomCombo = new JComboBox<>();
    private final JTextField roomIdField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField identityField = new JTextField();
    private final JTextField checkInField = new JTextField();
    private final JTextField checkOutField = new JTextField();
    private final JLabel resultLabel = new JLabel(" ");

    public BookingPanel(CreateBookingUseCase createBookingUseCase, ListRoomsUseCase listRoomsUseCase, Runnable onBookingCreated) {
        this.createBookingUseCase = createBookingUseCase;
        this.listRoomsUseCase = listRoomsUseCase;
        this.onBookingCreated = onBookingCreated;
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        reloadAvailableRooms();
    }
    
    // Set room ID từ RoomsPanel
    public void setRoomId(String roomId) {
        roomIdField.setText(roomId);
        // Focus vào field tên khách
        nameField.requestFocus();
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));
        JLabel title = new JLabel("Đặt phòng");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.WEST);
        return panel;
    }

    private JComponent buildForm() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 25, 20, 25)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addRow(card, gbc, row++, "Mã phòng (nhập tay)", roomIdField);
        addRow(card, gbc, row++, "Phòng (đang trống)", roomCombo);
        addRow(card, gbc, row++, "Tên khách", nameField);
        addRow(card, gbc, row++, "Số điện thoại", phoneField);
        addRow(card, gbc, row++, "Số giấy tờ", identityField);
        addRow(card, gbc, row++, "Check-in (yyyy-mm-dd)", checkInField);
        addRow(card, gbc, row++, "Check-out (yyyy-mm-dd)", checkOutField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton checkBtn = new JButton("Kiểm tra phòng trống");
        checkBtn.setFont(LABEL_FONT);
        checkBtn.setBackground(new Color(52, 152, 219));
        checkBtn.setForeground(Color.WHITE);
        checkBtn.setFocusPainted(false);
        checkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBtn.setToolTipText("Kiểm tra xem phòng có đang trống không");
        checkBtn.addActionListener(e -> checkAvailability());
        JButton createBtn = new JButton("Tạo đặt phòng");
        createBtn.setFont(LABEL_FONT);
        createBtn.setBackground(SUCCESS);
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBtn.setToolTipText("Tạo đặt phòng mới cho khách");
        createBtn.addActionListener(e -> createBooking());
        buttonPanel.add(checkBtn);
        buttonPanel.add(createBtn);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        card.add(buttonPanel, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.setBackground(CONTENT_BG);
        wrapper.add(card);
        return wrapper;
    }

    private JComponent buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultLabel.setForeground(SUCCESS);
        panel.add(resultLabel, BorderLayout.CENTER);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(LABEL_FONT);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        input.setFont(LABEL_FONT);
        if (input instanceof JTextField) {
            ((JTextField) input).setPreferredSize(new Dimension(250, 32));
        }
        panel.add(input, gbc);
    }

    private void reloadAvailableRooms() {
        roomCombo.removeAllItems();
        List<RoomSummaryDTO> rooms = listRoomsUseCase.execute("");
        rooms.stream()
                .filter(r -> "TRONG".equalsIgnoreCase(r.status()))
                .forEach(r -> roomCombo.addItem(r.id()));
    }

    private void checkAvailability() {
        String roomId = resolveRoomId();
        if (roomId == null) {
            showError("Vui lòng nhập hoặc chọn phòng.");
            return;
        }
        List<RoomSummaryDTO> rooms = listRoomsUseCase.execute(roomId);
        boolean available = rooms.stream().anyMatch(r -> r.id().equalsIgnoreCase(roomId) && "TRONG".equalsIgnoreCase(r.status()));
        if (available) {
            showInfo("Phòng " + roomId + " đang TRỐNG.");
        } else {
            showError("Phòng " + roomId + " không trống.");
        }
    }

    private void createBooking() {
        String roomId = resolveRoomId();
        if (roomId == null) {
            showError("Vui lòng nhập hoặc chọn phòng.");
            return;
        }
        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = parseDate(checkInField.getText());
            checkOut = parseDate(checkOutField.getText());
        } catch (DateTimeParseException ex) {
            showError("Định dạng ngày không hợp lệ (yyyy-mm-dd).");
            return;
        }

        CreateBookingRequest request = new CreateBookingRequest(
                nameField.getText(),
                phoneField.getText(),
                identityField.getText(),
                roomId,
                checkIn,
                checkOut
        );

        Result<CreateBookingResponse, BookingError> result = createBookingUseCase.execute(request);
        if (result.isSuccess()) {
            CreateBookingResponse resp = result.getValue().orElseThrow();
            showInfo("Đặt phòng thành công. BookingId: " + resp.bookingId() + " | Trạng thái phòng: " + resp.roomStatusAfter());
            reloadAvailableRooms();
            if (onBookingCreated != null) {
                onBookingCreated.run();
            }
        } else {
            BookingError error = result.getError().orElse(new BookingError("UNKNOWN", "Không rõ lỗi"));
            showError(error.message());
        }
    }

    private LocalDate parseDate(String value) {
        return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String resolveRoomId() {
        if (roomIdField.getText() != null && !roomIdField.getText().isBlank()) {
            return roomIdField.getText().trim();
        }
        Object selected = roomCombo.getSelectedItem();
        return selected == null ? null : selected.toString();
    }

    private void showInfo(String message) {
        resultLabel.setForeground(SUCCESS);
        resultLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Đặt phòng", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        resultLabel.setForeground(DANGER);
        resultLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Đặt phòng", JOptionPane.ERROR_MESSAGE);
    }
}
