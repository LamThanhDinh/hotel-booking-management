package com.hotel.services.data;

import com.hotel.common.data.ConnectionProvider;
import com.hotel.rooms.domain.Money;
import com.hotel.services.application.ServiceRepository;
import com.hotel.services.domain.ServiceItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcServiceRepository implements ServiceRepository {
    private final ConnectionProvider connectionProvider;

    public JdbcServiceRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<ServiceItem> findAvailableServices(String keyword) {
        String sql = "SELECT service_id, name, unit_price_amount, currency, stock, is_active FROM services " +
                "WHERE is_active = 1 AND stock > 0 AND name LIKE ?";
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        return query(sql, stmt -> stmt.setString(1, like));
    }

    @Override
    public Optional<ServiceItem> findById(String serviceId) {
        String sql = "SELECT service_id, name, unit_price_amount, currency, stock, is_active FROM services WHERE service_id = ?";
        List<ServiceItem> list = query(sql, stmt -> stmt.setString(1, serviceId));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public ServiceItem save(ServiceItem serviceItem) {
        String sql = "INSERT INTO services (service_id, name, unit_price_amount, currency, stock, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name), unit_price_amount=VALUES(unit_price_amount), currency=VALUES(currency), " +
                "stock=VALUES(stock), is_active=VALUES(is_active)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serviceItem.getServiceId());
            stmt.setString(2, serviceItem.getName());
            stmt.setBigDecimal(3, serviceItem.getUnitPrice().getAmount());
            stmt.setString(4, serviceItem.getUnitPrice().getCurrency());
            stmt.setInt(5, serviceItem.getStock());
            stmt.setBoolean(6, serviceItem.isAvailable());
            stmt.executeUpdate();
            return serviceItem;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save service", e);
        }
    }

    private List<ServiceItem> query(String sql, SqlConsumer<PreparedStatement> binder) {
        List<ServiceItem> items = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.accept(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query services", e);
        }
        return items;
    }

    private ServiceItem mapRow(ResultSet rs) throws SQLException {
        return new ServiceItem(
                rs.getString("service_id"),
                rs.getString("name"),
                new Money(rs.getBigDecimal("unit_price_amount"), rs.getString("currency")),
                rs.getInt("stock"),
                rs.getBoolean("is_active")
        );
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}