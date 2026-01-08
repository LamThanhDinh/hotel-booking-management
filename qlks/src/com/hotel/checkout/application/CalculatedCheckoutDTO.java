package com.hotel.checkout.application;

import java.util.List;

public record CalculatedCheckoutDTO(
        String bookingId,
        String roomId,
        long nights,
        String roomTotal,
        String servicesTotal,
        String grandTotal,
        List<ServiceLineDTO> serviceLines
) {
}
