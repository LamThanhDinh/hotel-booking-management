package com.hotel.booking.application;

import com.hotel.booking.domain.Booking;
import com.hotel.booking.domain.BookingStatus;
import com.hotel.booking.domain.Customer;
import com.hotel.booking.domain.DateRange;
import com.hotel.rooms.application.RoomRepository;
import com.hotel.rooms.domain.Room;
import com.hotel.rooms.domain.RoomStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class CreateBookingUseCase {
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;

    public CreateBookingUseCase(BookingRepository bookingRepository, CustomerRepository customerRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
    }

    public Result<CreateBookingResponse, BookingError> execute(CreateBookingRequest request) {
        BookingError validationError = validate(request);
        if (validationError != null) {
            return Result.failure(validationError);
        }

        DateRange dateRange;
        try {
            dateRange = new DateRange(request.checkInDate(), request.checkOutDate());
        } catch (IllegalArgumentException ex) {
            return Result.failure(new BookingError("INVALID_DATE_RANGE", "Ngày trả phải sau ngày nhận."));
        }

        Optional<Room> roomOpt = roomRepository.findById(request.roomId());
        if (roomOpt.isEmpty()) {
            return Result.failure(new BookingError("ROOM_NOT_FOUND", "Không tìm thấy phòng."));
        }

        Room room = roomOpt.get();
        if (room.getStatus() != RoomStatus.TRONG) {
            return Result.failure(new BookingError("ROOM_NOT_AVAILABLE", "Phòng không còn trống."));
        }

        Customer customer = findOrCreateCustomer(request.customerName(), request.customerPhone(), request.customerIdentityNo());

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                room.getId(),
                customer.getCustomerId(),
                dateRange,
                BookingStatus.ACTIVE,
                Instant.now()
        );

        bookingRepository.save(booking);

        room.markBooked();
        roomRepository.save(room);

        CreateBookingResponse response = new CreateBookingResponse(
                booking.getBookingId(),
                booking.getRoomId(),
                booking.getCustomerId(),
                room.getStatus().name(),
                dateRange.nights()
        );
        return Result.success(response);
    }

    private BookingError validate(CreateBookingRequest request) {
        if (request == null) {
            return new BookingError("INVALID_REQUEST", "Yêu cầu không hợp lệ.");
        }
        if (isBlank(request.customerName())) {
            return new BookingError("INVALID_CUSTOMER", "Tên khách hàng bắt buộc.");
        }
        if (isBlank(request.customerPhone())) {
            return new BookingError("INVALID_CUSTOMER", "Số điện thoại bắt buộc.");
        }
        if (isBlank(request.roomId())) {
            return new BookingError("INVALID_ROOM", "Mã phòng bắt buộc.");
        }
        LocalDate checkIn = request.checkInDate();
        LocalDate checkOut = request.checkOutDate();
        if (checkIn == null || checkOut == null) {
            return new BookingError("INVALID_DATE", "Ngày nhận/trả không được để trống.");
        }
        return null;
    }

    private Customer findOrCreateCustomer(String name, String phone, String identityNo) {
        Optional<Customer> existing = customerRepository.findByPhoneOrIdentity(phone, identityNo);
        if (existing.isPresent()) {
            return existing.get();
        }
        Customer customer = new Customer(UUID.randomUUID().toString(), name, phone, identityNo);
        return customerRepository.save(customer);
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
