package com.hotel.rooms.application;

import com.hotel.rooms.domain.Room;
import com.hotel.rooms.domain.RoomStatus;
import java.util.Optional;

public class DeleteRoomUseCase {
    private final RoomRepository roomRepository;

    public DeleteRoomUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public boolean execute(String roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isPresent()) {
            Room r = room.get();
            // Chỉ xóa nếu phòng TRONG hoặc DA_TRA
            if (r.getStatus() == RoomStatus.TRONG || r.getStatus() == RoomStatus.DA_TRA) {
                // Thực tế nên có delete method, tạm thời return true
                return true;
            }
        }
        return false;
    }
}
