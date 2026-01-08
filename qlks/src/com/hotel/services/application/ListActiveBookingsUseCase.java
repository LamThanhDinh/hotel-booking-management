package com.hotel.services.application;

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

    public List<BookingListItemDTO> execute() {
        List<Booking> all = bookingRepository.findAll();
        return all.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACTIVE)
                .map(b -> new BookingListItemDTO(b.getBookingId(), b.getRoomId(), b.getStatus().name()))
                .collect(Collectors.toList());
    }
}
