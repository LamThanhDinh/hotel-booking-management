package com.hotel.services.data;

import com.hotel.services.application.ServiceUsageRepository;
import com.hotel.services.domain.ServiceUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryServiceUsageRepository implements ServiceUsageRepository {
    private final List<ServiceUsage> usages = new ArrayList<>();

    @Override
    public ServiceUsage addUsage(ServiceUsage usage) {
        usages.add(usage);
        return usage;
    }

    @Override
    public List<ServiceUsage> listByBookingId(String bookingId) {
        return Collections.unmodifiableList(
                usages.stream()
                        .filter(u -> u.getBookingId().equalsIgnoreCase(bookingId))
                        .collect(Collectors.toList())
        );
    }
}
