package com.hotel.app;

import com.hotel.app.ui.MainFrame;
import com.hotel.booking.ui.BookingPanel;
import com.hotel.rooms.ui.RoomsPanel;
import com.hotel.services.ui.ServicesPanel;
import com.hotel.checkout.ui.CheckoutPanel;
import com.hotel.revenue.ui.RevenuePanel;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

public class MainApplication {
    public static void main(String[] args) {
        // Áp dụng FlatLaf Look and Feel hiện đại
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AppCompositionRoot root = new AppCompositionRoot();
            RoomsPanel roomsPanel = root.buildRoomsPanel();
            BookingPanel bookingPanel = root.buildBookingPanel(roomsPanel::refreshData);
            ServicesPanel servicesPanel = root.buildServicesPanel();
            CheckoutPanel checkoutPanel = root.buildCheckoutPanel(() -> {
                roomsPanel.refreshData();
                servicesPanel.refreshData();
            });
            RevenuePanel revenuePanel = root.buildRevenuePanel();
            MainFrame frame = new MainFrame(roomsPanel, bookingPanel, servicesPanel, checkoutPanel, revenuePanel);
            frame.setVisible(true);
        });
    }
}