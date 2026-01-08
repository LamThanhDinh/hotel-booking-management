package com.hotel.services.data;

import com.hotel.common.data.ConnectionProvider;
import com.hotel.services.application.ServiceUsageRepository;
import com.hotel.services.domain.ServiceUsage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JdbcServiceUsageRepository implements ServiceUsageRepository {
    private final ConnectionProvider connectionProvider;

    public JdbcServiceUsageRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public ServiceUsage addUsage(ServiceUsage usage) {
        String sql = "INSERT INTO service_usages (usage_id, booking_id, service_id, quantity, used_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usage.getUsageId());
            stmt.setString(2, usage.getBookingId());
            stmt.setString(3, usage.getServiceId());
            stmt.setInt(4, usage.getQuantity());
            stmt.setTimestamp(5, Timestamp.from(usage.getUsedAt()));
            stmt.executeUpdate();
            return usage;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add service usage", e);
        }
    }

    @Override
    public List<ServiceUsage> listByBookingId(String bookingId) {
        String sql = "SELECT usage_id, booking_id, service_id, quantity, used_at FROM service_usages WHERE booking_id = ? ORDER BY used_at";
        List<ServiceUsage> list = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query service usages", e);
        }
        return list;
    }

    private ServiceUsage mapRow(ResultSet rs) throws SQLException {
        return new ServiceUsage(
                rs.getString("usage_id"),
                rs.getString("booking_id"),
                rs.getString("service_id"),
                rs.getInt("quantity"),
                rs.getTimestamp("used_at").toInstant()
        );
    }
}