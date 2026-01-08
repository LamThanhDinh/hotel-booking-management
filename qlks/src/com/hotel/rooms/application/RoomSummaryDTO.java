package com.hotel.rooms.application;

public record RoomSummaryDTO(
        String id,
        String name,
        String roomType,
        String bedType,
        String price,
        String status
) {
}