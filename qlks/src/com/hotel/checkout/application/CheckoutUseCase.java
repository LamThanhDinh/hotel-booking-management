package com.hotel.checkout.application;

import com.hotel.booking.application.BookingRepository;
import com.hotel.booking.domain.Booking;
import com.hotel.rooms.application.RoomRepository;
import com.hotel.rooms.domain.Money;
import com.hotel.rooms.domain.Room;
import com.hotel.checkout.domain.Invoice;
import com.hotel.checkout.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class CheckoutUseCase {
    private final CalculateCheckoutUseCase calculateCheckoutUseCase;
    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public CheckoutUseCase(CalculateCheckoutUseCase calculateCheckoutUseCase,
                           InvoiceRepository invoiceRepository,
                           BookingRepository bookingRepository,
                           RoomRepository roomRepository) {
        this.calculateCheckoutUseCase = calculateCheckoutUseCase;
        this.invoiceRepository = invoiceRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    public Result<CheckoutResponse, CheckoutError> execute(String bookingId) {
        Result<CalculatedCheckoutDTO, CheckoutError> calc = calculateCheckoutUseCase.execute(bookingId);
        if (!calc.isSuccess()) {
            return Result.failure(calc.getError().orElse(new CheckoutError("UNKNOWN", "Lỗi tính tiền")));
        }
        CalculatedCheckoutDTO dto = calc.getValue().orElseThrow();

        Optional<Booking> bookingOpt = bookingRepository.findById(dto.bookingId());
        if (bookingOpt.isEmpty()) {
            return Result.failure(new CheckoutError("BOOKING_NOT_FOUND", "Không tìm thấy booking."));
        }
        Booking booking = bookingOpt.get();

        Optional<Room> roomOpt = roomRepository.findById(dto.roomId());
        if (roomOpt.isEmpty()) {
            return Result.failure(new CheckoutError("ROOM_NOT_FOUND", "Không tìm thấy phòng."));
        }
        Room room = roomOpt.get();

        Money grandTotal = parseMoney(dto.grandTotal());

        Invoice invoice = new Invoice(
                UUID.randomUUID().toString(),
                booking.getBookingId(),
                parseMoney(dto.roomTotal()),
                parseMoney(dto.servicesTotal()),
                grandTotal,
                InvoiceStatus.PAID,
                Instant.now()
        );
        invoiceRepository.save(invoice);

        booking.markCheckedOut();
        bookingRepository.save(booking);

        // Sau checkout, phòng chuyển sang trạng thái "đang dọn" (DA_TRA)
        room.markReturned();
        roomRepository.save(room);

        CheckoutResponse response = new CheckoutResponse(
                invoice.getInvoiceId(),
                booking.getBookingId(),
                room.getStatus().name(),
                grandTotal.toString(),
                invoice.getPaidAt().toString()
        );
        return Result.success(response);
    }

    private Money parseMoney(String formatted) {
        // formatted like "123 VND"
        String[] parts = formatted.split(" ");
        BigDecimal amount = new BigDecimal(parts[0]);
        String currency = parts.length > 1 ? parts[1] : "VND";
        return new Money(amount, currency);
    }
}
