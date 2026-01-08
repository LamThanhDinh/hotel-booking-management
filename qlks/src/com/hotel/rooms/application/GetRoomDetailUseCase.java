package com.hotel.rooms.application;

import java.util.Optional;

public class GetRoomDetailUseCase {
    private final RoomRepository roomRepository;

    public GetRoomDetailUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Optional<RoomDetailDTO> execute(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return Optional.empty();
        }
        return roomRepository.findById(roomId.trim())
                .map(room -> new RoomDetailDTO(
                        room.getId(),
                        room.getName(),
                        room.getRoomType().name(),
                        room.getBedType().name(),
                        room.getPrice().toString(),
                        room.getStatus().name()
                ));
    }
}