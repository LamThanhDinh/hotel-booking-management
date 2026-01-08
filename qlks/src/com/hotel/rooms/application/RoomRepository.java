package com.hotel.rooms.application;

import com.hotel.rooms.domain.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    List<Room> findAll();

    List<Room> searchByKeyword(String keyword);

    Optional<Room> findById(String id);

    void save(Room room);
}