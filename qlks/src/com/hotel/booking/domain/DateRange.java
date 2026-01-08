package com.hotel.booking.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class DateRange {
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;

    public DateRange(LocalDate checkInDate, LocalDate checkOutDate) {
        this.checkInDate = Objects.requireNonNull(checkInDate, "checkInDate");
        this.checkOutDate = Objects.requireNonNull(checkOutDate, "checkOutDate");
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("checkOutDate must be after checkInDate");
        }
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}
