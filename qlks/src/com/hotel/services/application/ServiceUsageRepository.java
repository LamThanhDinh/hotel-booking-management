package com.hotel.services.application;

import com.hotel.services.domain.ServiceUsage;

import java.util.List;

public interface ServiceUsageRepository {
    ServiceUsage addUsage(ServiceUsage usage);

    List<ServiceUsage> listByBookingId(String bookingId);
}
