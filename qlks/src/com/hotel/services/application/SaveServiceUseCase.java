package com.hotel.services.application;

import com.hotel.rooms.domain.Money;
import com.hotel.services.domain.ServiceItem;

import java.math.BigDecimal;

public class SaveServiceUseCase {
    private final ServiceRepository serviceRepository;

    public SaveServiceUseCase(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public void execute(String serviceId, String name, BigDecimal price, int stock) {
        ServiceItem service = new ServiceItem(serviceId, name, new Money(price, "VND"), stock, true);
        serviceRepository.save(service);
    }
}
