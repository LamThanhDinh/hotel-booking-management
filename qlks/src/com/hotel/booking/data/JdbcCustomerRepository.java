package com.hotel.booking.data;

import com.hotel.booking.application.CustomerRepository;
import com.hotel.booking.domain.Customer;
import com.hotel.common.data.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCustomerRepository implements CustomerRepository {
    private final ConnectionProvider connectionProvider;

    public JdbcCustomerRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Optional<Customer> findByPhoneOrIdentity(String phone, String identityNo) {
        String sql = "SELECT customer_id, full_name, phone, identity_no FROM customers WHERE phone = ? OR identity_no = ? LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            stmt.setString(2, identityNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer", e);
        }
        return Optional.empty();
    }

    @Override
    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (customer_id, full_name, phone, identity_no) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), phone=VALUES(phone), identity_no=VALUES(identity_no)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getIdentityNo());
            stmt.executeUpdate();
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save customer", e);
        }
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT customer_id, full_name, phone, identity_no FROM customers";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query customers", e);
        }
        return list;
    }
    
    @Override
    public Optional<Customer> findById(String customerId) {
        String sql = "SELECT customer_id, full_name, phone, identity_no FROM customers WHERE customer_id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by id", e);
        }
        return Optional.empty();
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("customer_id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("identity_no")
        );
    }
}