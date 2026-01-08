package com.hotel.services.application;

public record ServiceListItemDTO(
        String id,
        String name,
        String unitPrice,
        int stock
) {
}
