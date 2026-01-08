package com.hotel.services.domain;

import java.time.Instant;
import java.util.Objects;

public class ServiceUsage {
    private final String usageId;
    private final String bookingId;
    private final String serviceId;
    private final int quantity;
    private final Instant usedAt;

    public ServiceUsage(String usageId, String bookingId, String serviceId, int quantity, Instant usedAt) {
        this.usageId = Objects.requireNonNull(usageId, "usageId");
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId");
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.quantity = quantity;
        this.usedAt = Objects.requireNonNull(usedAt, "usedAt");
    }

    public String getUsageId() {
        return usageId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getUsedAt() {
        return usedAt;
    }
}
