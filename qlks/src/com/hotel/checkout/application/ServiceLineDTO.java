package com.hotel.checkout.application;

public record ServiceLineDTO(
        String serviceId,
        String serviceName,
        int quantity,
        String unitPrice,
        String lineTotal
) {
}
