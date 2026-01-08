package com.hotel.checkout.application;

import com.hotel.booking.application.BookingRepository;
import com.hotel.booking.domain.Booking;
import com.hotel.booking.domain.BookingStatus;
import com.hotel.rooms.application.RoomRepository;
import com.hotel.rooms.domain.Money;
import com.hotel.rooms.domain.Room;
import com.hotel.services.application.ServiceRepository;
import com.hotel.services.application.ServiceUsageRepository;
import com.hotel.services.domain.ServiceItem;
import com.hotel.services.domain.ServiceUsage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CalculateCheckoutUseCase {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final ServiceUsageRepository serviceUsageRepository;
    private final ServiceRepository serviceRepository;

    public CalculateCheckoutUseCase(BookingRepository bookingRepository,
                                    RoomRepository roomRepository,
                                    ServiceUsageRepository serviceUsageRepository,
                                    ServiceRepository serviceRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.serviceUsageRepository = serviceUsageRepository;
        this.serviceRepository = serviceRepository;
    }

    public Result<CalculatedCheckoutDTO, CheckoutError> execute(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            return Result.failure(new CheckoutError("INVALID_INPUT", "BookingId không hợp lệ."));
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId.trim());
        if (bookingOpt.isEmpty()) {
            return Result.failure(new CheckoutError("BOOKING_NOT_FOUND", "Không tìm thấy booking."));
        }
        Booking booking = bookingOpt.get();
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            return Result.failure(new CheckoutError("BOOKING_NOT_ACTIVE", "Booking không ở trạng thái ACTIVE."));
        }

        Optional<Room> roomOpt = roomRepository.findById(booking.getRoomId());
        if (roomOpt.isEmpty()) {
            return Result.failure(new CheckoutError("ROOM_NOT_FOUND", "Không tìm thấy phòng."));
        }
        Room room = roomOpt.get();

        long nights = booking.getDateRange().nights();
        Money roomTotal = room.getPrice().multiply((int) nights);

        List<ServiceUsage> usages = serviceUsageRepository.listByBookingId(booking.getBookingId());
        List<ServiceLineDTO> lines = new ArrayList<>();
        Money servicesTotal = new Money(BigDecimal.ZERO, room.getPrice().getCurrency());
        for (ServiceUsage usage : usages) {
            Optional<ServiceItem> serviceItemOpt = serviceRepository.findById(usage.getServiceId());
            if (serviceItemOpt.isEmpty()) {
                return Result.failure(new CheckoutError("SERVICE_NOT_FOUND", "Dịch vụ " + usage.getServiceId() + " không tồn tại."));
            }
            ServiceItem item = serviceItemOpt.get();
            Money lineTotal = item.getUnitPrice().multiply(usage.getQuantity());
            servicesTotal = servicesTotal.add(lineTotal);
            lines.add(new ServiceLineDTO(item.getServiceId(), item.getName(), usage.getQuantity(), item.getUnitPrice().toString(), lineTotal.toString()));
        }

        Money grandTotal = roomTotal.add(servicesTotal);
        CalculatedCheckoutDTO dto = new CalculatedCheckoutDTO(
                booking.getBookingId(),
                booking.getRoomId(),
                nights,
                roomTotal.toString(),
                servicesTotal.toString(),
                grandTotal.toString(),
                lines
        );
        return Result.success(dto);
    }
}
