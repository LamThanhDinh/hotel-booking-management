package com.hotel.rooms.application;

import com.hotel.rooms.domain.Room;
import com.hotel.rooms.domain.RoomStatus;

import java.util.Optional;

public class UpdateRoomStatusUseCase {
    private final RoomRepository roomRepository;

    public UpdateRoomStatusUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public boolean execute(String roomId, RoomStatus newStatus) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return false;
        }
        
        Room room = roomOpt.get();
        switch (newStatus) {
            case TRONG -> room.markEmpty();
            case DA_DAT -> room.markBooked();
            case DANG_SU_DUNG -> room.markInUse();
            case DA_TRA -> room.markReturned();
        }
        
        roomRepository.save(room);
        return true;
    }
    
    // Các action cụ thể
    public boolean markAsEmpty(String roomId) {
        return execute(roomId, RoomStatus.TRONG);
    }
    
    public boolean markAsInUse(String roomId) {
        return execute(roomId, RoomStatus.DANG_SU_DUNG);
    }
    
    public boolean markAsCleaning(String roomId) {
        return execute(roomId, RoomStatus.DA_TRA);
    }
    
    public boolean markAsBooked(String roomId) {
        return execute(roomId, RoomStatus.DA_DAT);
    }
}
