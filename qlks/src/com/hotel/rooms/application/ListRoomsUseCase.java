package com.hotel.rooms.application;

import com.hotel.rooms.domain.Room;

import java.util.List;
import java.util.stream.Collectors;

public class ListRoomsUseCase {
    private final RoomRepository roomRepository;

    public ListRoomsUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<RoomSummaryDTO> execute(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        List<Room> rooms = normalized.isEmpty()
                ? roomRepository.findAll()
                : roomRepository.searchByKeyword(normalized);

        return rooms.stream()
                .map(room -> new RoomSummaryDTO(
                        room.getId(),
                        room.getName(),
                        room.getRoomType().name(),
                        room.getBedType().name(),
                        room.getPrice().toString(),
                        room.getStatus().name()
                ))
                .collect(Collectors.toList());
    }
}