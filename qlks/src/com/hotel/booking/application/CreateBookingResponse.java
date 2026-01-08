package com.hotel.booking.application;

public record CreateBookingResponse(
        String bookingId,
        String roomId,
        String customerId,
        String roomStatusAfter,
        long totalRoomNights
) {
}
