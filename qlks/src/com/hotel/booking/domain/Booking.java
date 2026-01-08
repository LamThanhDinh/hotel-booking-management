package com.hotel.booking.domain;

import java.time.Instant;
import java.util.Objects;

public class Booking {
    private final String bookingId;
    private final String roomId;
    private final String customerId;
    private final DateRange dateRange;
    private BookingStatus status;
    private final Instant createdAt;

    public Booking(String bookingId, String roomId, String customerId, DateRange dateRange, BookingStatus status, Instant createdAt) {
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId");
        this.roomId = Objects.requireNonNull(roomId, "roomId");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.dateRange = Objects.requireNonNull(dateRange, "dateRange");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markCheckedOut() {
        this.status = BookingStatus.CHECKED_OUT;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }
}
