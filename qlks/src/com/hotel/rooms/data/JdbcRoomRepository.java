package com.hotel.rooms.data;

import com.hotel.rooms.application.RoomRepository;
import com.hotel.rooms.domain.BedType;
import com.hotel.rooms.domain.Money;
import com.hotel.rooms.domain.Room;
import com.hotel.rooms.domain.RoomStatus;
import com.hotel.rooms.domain.RoomType;
import com.hotel.common.data.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcRoomRepository implements RoomRepository {
    private final ConnectionProvider connectionProvider;

    public JdbcRoomRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<Room> findAll() {
        String sql = "SELECT room_id, room_name, room_type, bed_type, price_amount, currency, status FROM rooms";
        return queryRooms(sql, stmt -> {});
    }

    @Override
    public List<Room> searchByKeyword(String keyword) {
        String sql = "SELECT room_id, room_name, room_type, bed_type, price_amount, currency, status FROM rooms " +
                "WHERE room_name LIKE ? OR room_type LIKE ? OR bed_type LIKE ?";
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        return queryRooms(sql, stmt -> {
            stmt.setString(1, like);
            stmt.setString(2, like);
            stmt.setString(3, like);
        });
    }

    @Override
    public Optional<Room> findById(String id) {
        String sql = "SELECT room_id, room_name, room_type, bed_type, price_amount, currency, status FROM rooms WHERE room_id = ?";
        List<Room> list = queryRooms(sql, stmt -> stmt.setString(1, id));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public void save(Room room) {
        String sql = "INSERT INTO rooms (room_id, room_name, room_type, bed_type, price_amount, currency, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE room_name=VALUES(room_name), room_type=VALUES(room_type), bed_type=VALUES(bed_type), " +
                "price_amount=VALUES(price_amount), currency=VALUES(currency), status=VALUES(status)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getId());
            stmt.setString(2, room.getName());
            stmt.setString(3, room.getRoomType().name());
            stmt.setString(4, room.getBedType().name());
            stmt.setBigDecimal(5, room.getPrice().getAmount());
            stmt.setString(6, room.getPrice().getCurrency());
            stmt.setString(7, room.getStatus().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save room", e);
        }
    }

    private List<Room> queryRooms(String sql, SqlConsumer<PreparedStatement> binder) {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.accept(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query rooms", e);
        }
        return rooms;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getString("room_id"),
                rs.getString("room_name"),
                RoomType.valueOf(rs.getString("room_type")),
                BedType.valueOf(rs.getString("bed_type")),
                new Money(rs.getBigDecimal("price_amount"), rs.getString("currency")),
                RoomStatus.valueOf(rs.getString("status"))
        );
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}