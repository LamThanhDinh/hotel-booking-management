package com.hotel.rooms.data;

import com.hotel.rooms.application.RoomRepository;
import com.hotel.rooms.domain.BedType;
import com.hotel.rooms.domain.Money;
import com.hotel.rooms.domain.Room;
import com.hotel.rooms.domain.RoomStatus;
import com.hotel.rooms.domain.RoomType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryRoomRepository implements RoomRepository {
    private final List<Room> rooms;

    public InMemoryRoomRepository() {
        this.rooms = seed();
    }

    @Override
    public List<Room> findAll() {
        return Collections.unmodifiableList(rooms);
    }

    @Override
    public List<Room> searchByKeyword(String keyword) {
        String lower = keyword.toLowerCase(Locale.ROOT);
        return rooms.stream()
                .filter(room -> room.getName().toLowerCase(Locale.ROOT).contains(lower)
                        || room.getId().toLowerCase(Locale.ROOT).contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Room> findById(String id) {
        return rooms.stream()
                .filter(room -> room.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    @Override
    public void save(Room room) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getId().equalsIgnoreCase(room.getId())) {
                rooms.set(i, room);
                return;
            }
        }
        rooms.add(room);
    }

    private List<Room> seed() {
        List<Room> list = new ArrayList<>();
        list.add(new Room("R101", "Phòng 101", RoomType.STANDARD, BedType.SINGLE, money(450_000), RoomStatus.TRONG));
        list.add(new Room("R102", "Phòng 102", RoomType.STANDARD, BedType.DOUBLE, money(500_000), RoomStatus.DA_DAT));
        list.add(new Room("R201", "Phòng 201", RoomType.DELUXE, BedType.QUEEN, money(700_000), RoomStatus.DANG_SU_DUNG));
        list.add(new Room("R202", "Phòng 202", RoomType.DELUXE, BedType.KING, money(850_000), RoomStatus.DA_TRA));
        list.add(new Room("R301", "Phòng 301", RoomType.SUITE, BedType.KING, money(1_200_000), RoomStatus.TRONG));
        list.add(new Room("R302", "Phòng 302", RoomType.SUITE, BedType.KING, money(1_250_000), RoomStatus.TRONG));
        list.add(new Room("R303", "Phòng 303", RoomType.SUITE, BedType.QUEEN, money(1_000_000), RoomStatus.DA_DAT));
        list.add(new Room("R304", "Phòng 304", RoomType.DELUXE, BedType.DOUBLE, money(750_000), RoomStatus.DANG_SU_DUNG));
        list.add(new Room("R401", "Phòng 401", RoomType.STANDARD, BedType.SINGLE, money(430_000), RoomStatus.TRONG));
        list.add(new Room("R402", "Phòng 402", RoomType.STANDARD, BedType.DOUBLE, money(520_000), RoomStatus.TRONG));
        return list;
    }

    private Money money(long vnd) {
        return new Money(BigDecimal.valueOf(vnd), "VND");
    }
}