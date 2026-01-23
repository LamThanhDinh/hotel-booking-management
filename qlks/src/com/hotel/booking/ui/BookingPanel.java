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
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
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
    private final JSpinner checkInSpinner;
    private final JSpinner checkOutSpinner;
    private final JLabel resultLabel = new JLabel(" ");

    public BookingPanel(CreateBookingUseCase createBookingUseCase, ListRoomsUseCase listRoomsUseCase, Runnable onBookingCreated) {
        this.createBookingUseCase = createBookingUseCase;
        this.listRoomsUseCase = listRoomsUseCase;
        this.onBookingCreated = onBookingCreated;
        
        // Khởi tạo date spinners
        this.checkInSpinner = createDateSpinner();
        this.checkOutSpinner = createDateSpinner();
        // Set check-out mặc định là 1 ngày sau check-in
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        checkOutSpinner.setValue(cal.getTime());
        
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        reloadAvailableRooms();
    }
    
    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setFont(LABEL_FONT);
        spinner.setPreferredSize(new Dimension(250, 32));
        return spinner;
    }
    
    // Set room ID từ RoomsPanel
    public void setRoomId(String roomId) {
        // Kiểm tra phòng có trong danh sách phòng trống không
        List<RoomSummaryDTO> rooms = listRoomsUseCase.execute(roomId);
        boolean available = rooms.stream().anyMatch(r -> r.id().equalsIgnoreCase(roomId) && "TRONG".equalsIgnoreCase(r.status()));
        
        if (!available) {
            String currentStatus = rooms.stream()
                    .filter(r -> r.id().equalsIgnoreCase(roomId))
                    .map(r -> r.status())
                    .findFirst()
                    .orElse("KHÔNG TÌM THẤY");
            showError("Phòng " + roomId + " không thể đặt! Trạng thái hiện tại: " + currentStatus + 
                     "\nChỉ có thể đặt phòng có trạng thái TRONG.");
            return;
        }
        
        // Chọn phòng trong combo box
        for (int i = 0; i < roomCombo.getItemCount(); i++) {
            if (roomCombo.getItemAt(i).equals(roomId)) {
                roomCombo.setSelectedIndex(i);
                nameField.requestFocus();
                return;
            }
        }
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
        addRow(card, gbc, row++, "Chọn phòng", roomCombo);
        addRow(card, gbc, row++, "Tên khách", nameField);
        addRow(card, gbc, row++, "Số điện thoại", phoneField);
        addRow(card, gbc, row++, "Số giấy tờ", identityField);
        addRow(card, gbc, row++, "Ngày check-in", checkInSpinner);
        addRow(card, gbc, row++, "Ngày check-out", checkOutSpinner);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton createBtn = new JButton("Tạo đặt phòng");
        createBtn.setFont(LABEL_FONT);
        createBtn.setBackground(SUCCESS);
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBtn.setToolTipText("Tạo đặt phòng mới cho khách");
        createBtn.addActionListener(e -> createBooking());
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
        } else if (input instanceof JComboBox) {
            input.setPreferredSize(new Dimension(250, 32));
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

    private void createBooking() {
        String roomId = resolveRoomId();
        if (roomId == null) {
            showError("Vui lòng chọn phòng.");
            return;
        }
        LocalDate checkIn = dateToLocalDate((Date) checkInSpinner.getValue());
        LocalDate checkOut = dateToLocalDate((Date) checkOutSpinner.getValue());
        
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            showError("Ngày check-out phải sau ngày check-in.");
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
            clearForm();
            reloadAvailableRooms();
            if (onBookingCreated != null) {
                onBookingCreated.run();
            }
        } else {
            BookingError error = result.getError().orElse(new BookingError("UNKNOWN", "Không rõ lỗi"));
            showError(error.message());
        }
    }
    
    private void clearForm() {
        roomCombo.setSelectedIndex(-1);
        nameField.setText("");
        phoneField.setText("");
        identityField.setText("");
        // Reset date spinners về mặc định
        checkInSpinner.setValue(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        checkOutSpinner.setValue(cal.getTime());
        resultLabel.setText(" ");
    }

    private LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String resolveRoomId() {
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
