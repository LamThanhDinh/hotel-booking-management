package com.hotel.services.application;

public record AddServiceToBookingResponse(
        String usageId,
        String bookingId,
        String serviceId,
        String serviceName,
        int quantity,
        String lineTotal
) {
}
