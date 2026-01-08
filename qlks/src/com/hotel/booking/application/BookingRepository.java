package com.hotel.booking.application;

import com.hotel.booking.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);

    Optional<Booking> findById(String bookingId);

    List<Booking> findAll();
}
