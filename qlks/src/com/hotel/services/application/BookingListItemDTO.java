package com.hotel.services.application;

public record BookingListItemDTO(
        String bookingId,
        String roomId,
        String status
) {
}
