package com.hotel.rooms.application;

public record RoomDetailDTO(
        String id,
        String name,
        String roomType,
        String bedType,
        String price,
        String status,
        String customerName,
        String checkInDate,
        String checkOutDate
) {
}