package com.hotel.services.application;

import com.hotel.booking.application.BookingRepository;
import com.hotel.booking.domain.Booking;
import com.hotel.booking.domain.BookingStatus;
import com.hotel.rooms.domain.Money;
import com.hotel.services.domain.ServiceItem;
import com.hotel.services.domain.ServiceUsage;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class AddServiceToBookingUseCase {
    private final ServiceRepository serviceRepository;
    private final ServiceUsageRepository serviceUsageRepository;
    private final BookingRepository bookingRepository;

    public AddServiceToBookingUseCase(ServiceRepository serviceRepository,
                                      ServiceUsageRepository serviceUsageRepository,
                                      BookingRepository bookingRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceUsageRepository = serviceUsageRepository;
        this.bookingRepository = bookingRepository;
    }

    public Result<AddServiceToBookingResponse, ServiceError> execute(AddServiceToBookingRequest request) {
        ServiceError validationError = validate(request);
        if (validationError != null) {
            return Result.failure(validationError);
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(request.bookingId());
        if (bookingOpt.isEmpty()) {
            return Result.failure(new ServiceError("BOOKING_NOT_FOUND", "Không tìm thấy booking."));
        }
        Booking booking = bookingOpt.get();
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            return Result.failure(new ServiceError("BOOKING_NOT_ACTIVE", "Booking không ở trạng thái ACTIVE."));
        }

        Optional<ServiceItem> serviceOpt = serviceRepository.findById(request.serviceId());
        if (serviceOpt.isEmpty()) {
            return Result.failure(new ServiceError("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ."));
        }
        ServiceItem serviceItem = serviceOpt.get();
        if (!serviceItem.canUse(request.quantity())) {
            return Result.failure(new ServiceError("SERVICE_UNAVAILABLE", "Dịch vụ không đủ tồn kho hoặc không khả dụng."));
        }

        ServiceUsage usage = new ServiceUsage(
                UUID.randomUUID().toString(),
                booking.getBookingId(),
                serviceItem.getServiceId(),
                request.quantity(),
                Instant.now()
        );
        serviceUsageRepository.addUsage(usage);

        serviceItem.decreaseStock(request.quantity());
        serviceRepository.save(serviceItem);

        Money lineTotal = serviceItem.getUnitPrice().multiply(request.quantity());
        AddServiceToBookingResponse response = new AddServiceToBookingResponse(
                usage.getUsageId(),
                usage.getBookingId(),
                serviceItem.getServiceId(),
                serviceItem.getName(),
                request.quantity(),
                lineTotal.toString()
        );
        return Result.success(response);
    }

    private ServiceError validate(AddServiceToBookingRequest request) {
        if (request == null) {
            return new ServiceError("INVALID_REQUEST", "Yêu cầu không hợp lệ.");
        }
        if (isBlank(request.bookingId())) {
            return new ServiceError("INVALID_BOOKING", "BookingId bắt buộc.");
        }
        if (isBlank(request.serviceId())) {
            return new ServiceError("INVALID_SERVICE", "ServiceId bắt buộc.");
        }
        if (request.quantity() <= 0) {
            return new ServiceError("INVALID_QUANTITY", "Số lượng phải > 0.");
        }
        return null;
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
