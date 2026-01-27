package com.hotel.app;

import com.hotel.app.ui.MainFrame;
import com.hotel.booking.ui.BookingPanel;
import com.hotel.booking.ui.CustomersPanel;
import com.hotel.rooms.ui.RoomsPanel;
import com.hotel.services.ui.ServicesPanel;
import com.hotel.checkout.ui.CheckoutPanel;
import com.hotel.revenue.ui.RevenuePanel;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.*;

public class MainApplication {
    public static void main(String[] args) {
        // Better text rendering on Windows
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Áp dụng FlatLaf Look and Feel hiện đại
        try {
            FlatLightLaf.setup();
            // Keep typography consistent across the app
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
            FlatLaf.setPreferredFontFamily("Segoe UI");
            UIManager.put("defaultFont", defaultFont);
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
            CustomersPanel customersPanel = root.buildCustomersPanel();
            MainFrame frame = new MainFrame(roomsPanel, bookingPanel, servicesPanel, checkoutPanel, revenuePanel, customersPanel);
            frame.setVisible(true);
        });
    }
}