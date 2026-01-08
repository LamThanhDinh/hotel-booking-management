package com.hotel.services.application;

import com.hotel.services.domain.ServiceItem;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository {
    List<ServiceItem> findAvailableServices(String keyword);

    Optional<ServiceItem> findById(String serviceId);

    ServiceItem save(ServiceItem serviceItem);
}
