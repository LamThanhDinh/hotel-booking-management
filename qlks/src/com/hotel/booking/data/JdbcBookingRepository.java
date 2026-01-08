package com.hotel.booking.data;

import com.hotel.booking.application.BookingRepository;
import com.hotel.booking.domain.Booking;
import com.hotel.booking.domain.BookingStatus;
import com.hotel.booking.domain.DateRange;
import com.hotel.common.data.ConnectionProvider;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookingRepository implements BookingRepository {
    private final ConnectionProvider connectionProvider;

    public JdbcBookingRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Booking save(Booking booking) {
        String sql = "INSERT INTO bookings (booking_id, room_id, customer_id, check_in_date, check_out_date, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE room_id=VALUES(room_id), customer_id=VALUES(customer_id), check_in_date=VALUES(check_in_date), " +
                "check_out_date=VALUES(check_out_date), status=VALUES(status)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, booking.getBookingId());
            stmt.setString(2, booking.getRoomId());
            stmt.setString(3, booking.getCustomerId());
            stmt.setDate(4, Date.valueOf(booking.getDateRange().getCheckInDate()));
            stmt.setDate(5, Date.valueOf(booking.getDateRange().getCheckOutDate()));
            stmt.setString(6, booking.getStatus().name());
            stmt.setTimestamp(7, Timestamp.from(booking.getCreatedAt()));
            stmt.executeUpdate();
            return booking;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save booking", e);
        }
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        String sql = "SELECT booking_id, room_id, customer_id, check_in_date, check_out_date, status, created_at FROM bookings WHERE booking_id = ?";
        List<Booking> list = query(sql, stmt -> stmt.setString(1, bookingId));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Booking> findAll() {
        String sql = "SELECT booking_id, room_id, customer_id, check_in_date, check_out_date, status, created_at FROM bookings";
        return query(sql, stmt -> {});
    }

    private List<Booking> query(String sql, SqlConsumer<PreparedStatement> binder) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.accept(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query bookings", e);
        }
        return bookings;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getString("booking_id"),
                rs.getString("room_id"),
                rs.getString("customer_id"),
                new DateRange(rs.getDate("check_in_date").toLocalDate(), rs.getDate("check_out_date").toLocalDate()),
                BookingStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}