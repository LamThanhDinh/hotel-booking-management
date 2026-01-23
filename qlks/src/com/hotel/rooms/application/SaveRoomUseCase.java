package com.hotel.rooms.application;

import com.hotel.rooms.domain.Room;
import com.hotel.rooms.domain.Money;
import java.math.BigDecimal;
import java.util.Optional;

public class SaveRoomUseCase {
    private final RoomRepository roomRepository;

    public SaveRoomUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public void execute(String roomId, String name, BigDecimal price) {
        Optional<Room> existing = roomRepository.findById(roomId);
        if (existing.isPresent()) {
            Room room = existing.get();
            Room updated = new Room(
                room.getId(),
                name,
                room.getRoomType(),
                room.getBedType(),
                new Money(price, "VND"),
                room.getStatus()
            );
            roomRepository.save(updated);
        }
    }
}
