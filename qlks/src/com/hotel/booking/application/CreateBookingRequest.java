package com.hotel.booking.application;

import java.time.LocalDate;

public record CreateBookingRequest(
        String customerName,
        String customerPhone,
        String customerIdentityNo,
        String roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}
