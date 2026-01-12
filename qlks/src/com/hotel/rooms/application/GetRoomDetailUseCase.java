package com.hotel.rooms.application;

import com.hotel.booking.application.BookingRepository;
import com.hotel.booking.application.CustomerRepository;
import com.hotel.booking.domain.Booking;
import com.hotel.booking.domain.Customer;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class GetRoomDetailUseCase {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GetRoomDetailUseCase(RoomRepository roomRepository, BookingRepository bookingRepository, CustomerRepository customerRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
    }

    public Optional<RoomDetailDTO> execute(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return Optional.empty();
        }
        return roomRepository.findById(roomId.trim())
                .map(room -> {
                    String customerName = "";
                    String checkInDate = "";
                    String checkOutDate = "";
                    
                    // Lấy thông tin booking nếu phòng đang được sử dụng hoặc đã đặt
                    Optional<Booking> booking = bookingRepository.findActiveByRoomId(roomId);
                    if (booking.isPresent()) {
                        Booking b = booking.get();
                        checkInDate = b.getDateRange().getCheckInDate().format(DATE_FORMATTER);
                        checkOutDate = b.getDateRange().getCheckOutDate().format(DATE_FORMATTER);
                        
                        // Lấy tên khách hàng
                        Optional<Customer> customer = customerRepository.findById(b.getCustomerId());
                        customerName = customer.map(Customer::getName).orElse("");
                    }
                    
                    return new RoomDetailDTO(
                            room.getId(),
                            room.getName(),
                            room.getRoomType().name(),
                            room.getBedType().name(),
                            room.getPrice().toString(),
                            room.getStatus().name(),
                            customerName,
                            checkInDate,
                            checkOutDate
                    );
                });
    }
}