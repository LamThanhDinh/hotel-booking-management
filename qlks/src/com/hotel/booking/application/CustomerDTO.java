package com.hotel.booking.application;

public record CustomerDTO(
        String customerId,
        String name,
        String phone,
        String identityNo
) {
}
