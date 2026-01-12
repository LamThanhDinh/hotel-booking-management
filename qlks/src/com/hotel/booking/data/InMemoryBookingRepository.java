package com.hotel.booking.data;

import com.hotel.booking.application.BookingRepository;
import com.hotel.booking.domain.Booking;
import com.hotel.booking.domain.BookingStatus;
import com.hotel.booking.domain.DateRange;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InMemoryBookingRepository implements BookingRepository {
    private final List<Booking> bookings = new ArrayList<>();

    public InMemoryBookingRepository() {
        seed();
    }

    @Override
    public Booking save(Booking booking) {
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getBookingId().equalsIgnoreCase(booking.getBookingId())) {
                bookings.set(i, booking);
                return booking;
            }
        }
        bookings.add(booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        return bookings.stream()
                .filter(b -> b.getBookingId().equalsIgnoreCase(bookingId))
                .findFirst();
    }

    @Override
    public List<Booking> findAll() {
        return Collections.unmodifiableList(bookings);
    }
    
    @Override
    public Optional<Booking> findActiveByRoomId(String roomId) {
        return bookings.stream()
                .filter(b -> b.getRoomId().equalsIgnoreCase(roomId) && b.getStatus() == BookingStatus.ACTIVE)
                .findFirst();
    }

    private void seed() {
        bookings.add(new Booking(
                "BKG-1001",
                "R102",
                "CUST-1001",
                new DateRange(LocalDate.now().minusDays(1), LocalDate.now().plusDays(2)),
                BookingStatus.ACTIVE,
                Instant.now().minusSeconds(3600)
        ));
        bookings.add(new Booking(
                "BKG-1002",
                "R303",
                "CUST-1002",
                new DateRange(LocalDate.now(), LocalDate.now().plusDays(3)),
                BookingStatus.ACTIVE,
                Instant.now().minusSeconds(7200)
        ));
    }
}
