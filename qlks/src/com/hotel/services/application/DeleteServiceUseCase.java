package com.hotel.services.application;

import com.hotel.services.domain.ServiceItem;
import java.util.Optional;

public class DeleteServiceUseCase {
    private final ServiceRepository serviceRepository;

    public DeleteServiceUseCase(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public boolean execute(String serviceId) {
        Optional<ServiceItem> service = serviceRepository.findById(serviceId);
        if (service.isPresent()) {
            // Set stock = 0 và available = false thay vì xóa hẳn
            ServiceItem item = service.get();
            ServiceItem updated = new ServiceItem(
                item.getServiceId(),
                item.getName(),
                item.getUnitPrice(),
                0,
                false
            );
            serviceRepository.save(updated);
            return true;
        }
        return false;
    }
}
