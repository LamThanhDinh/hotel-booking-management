package com.hotel.checkout.data;

import com.hotel.checkout.application.InvoiceRepository;
import com.hotel.checkout.domain.Invoice;
import com.hotel.checkout.domain.InvoiceStatus;
import com.hotel.common.data.ConnectionProvider;
import com.hotel.rooms.domain.Money;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JdbcInvoiceRepository implements InvoiceRepository {
    private final ConnectionProvider connectionProvider;

    public JdbcInvoiceRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Invoice save(Invoice invoice) {
        String sql = "INSERT INTO invoices (invoice_id, booking_id, room_total_amount, services_total_amount, grand_total_amount, currency, status, paid_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE room_total_amount=VALUES(room_total_amount), services_total_amount=VALUES(services_total_amount), " +
                "grand_total_amount=VALUES(grand_total_amount), currency=VALUES(currency), status=VALUES(status), paid_at=VALUES(paid_at)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, invoice.getInvoiceId());
            stmt.setString(2, invoice.getBookingId());
            stmt.setBigDecimal(3, invoice.getRoomTotal().getAmount());
            stmt.setBigDecimal(4, invoice.getServicesTotal().getAmount());
            stmt.setBigDecimal(5, invoice.getGrandTotal().getAmount());
            stmt.setString(6, invoice.getGrandTotal().getCurrency());
            stmt.setString(7, invoice.getStatus().name());
            stmt.setTimestamp(8, Timestamp.from(invoice.getPaidAt()));
            stmt.executeUpdate();
            return invoice;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save invoice", e);
        }
    }

    @Override
    public List<Invoice> listPaidBetween(Instant from, Instant to) {
        String sql = "SELECT invoice_id, booking_id, room_total_amount, services_total_amount, grand_total_amount, currency, status, paid_at " +
                "FROM invoices WHERE paid_at BETWEEN ? AND ? AND status = 'PAID'";
        return query(sql, stmt -> {
            stmt.setTimestamp(1, Timestamp.from(from));
            stmt.setTimestamp(2, Timestamp.from(to));
        });
    }

    @Override
    public List<Invoice> findAll() {
        String sql = "SELECT invoice_id, booking_id, room_total_amount, services_total_amount, grand_total_amount, currency, status, paid_at FROM invoices";
        return query(sql, stmt -> {});
    }

    private List<Invoice> query(String sql, SqlConsumer<PreparedStatement> binder) {
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.accept(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query invoices", e);
        }
        return list;
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Money roomTotal = new Money(rs.getBigDecimal("room_total_amount"), rs.getString("currency"));
        Money servicesTotal = new Money(rs.getBigDecimal("services_total_amount"), rs.getString("currency"));
        Money grandTotal = new Money(rs.getBigDecimal("grand_total_amount"), rs.getString("currency"));
        return new Invoice(
                rs.getString("invoice_id"),
                rs.getString("booking_id"),
                roomTotal,
                servicesTotal,
                grandTotal,
                InvoiceStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("paid_at").toInstant()
        );
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}