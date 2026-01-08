package com.hotel.services.application;

public record AddServiceToBookingRequest(
        String bookingId,
        String serviceId,
        int quantity
) {
}
