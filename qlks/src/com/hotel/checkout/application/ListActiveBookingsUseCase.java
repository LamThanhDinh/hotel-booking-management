package com.hotel.checkout.application;

import com.hotel.booking.application.BookingRepository;
import com.hotel.booking.domain.Booking;
import com.hotel.booking.domain.BookingStatus;

import java.util.List;
import java.util.stream.Collectors;

public class ListActiveBookingsUseCase {
    private final BookingRepository bookingRepository;

    public ListActiveBookingsUseCase(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<SimpleBookingDTO> execute() {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.ACTIVE)
                .map(b -> new SimpleBookingDTO(b.getBookingId(), b.getRoomId()))
                .collect(Collectors.toList());
    }
}
