package com.hotel.services.application;

import com.hotel.services.domain.ServiceItem;

import java.util.List;
import java.util.stream.Collectors;

public class ListAvailableServicesUseCase {
    private final ServiceRepository serviceRepository;

    public ListAvailableServicesUseCase(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceListItemDTO> execute(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        List<ServiceItem> items = serviceRepository.findAvailableServices(normalized);
        return items.stream()
                .map(item -> new ServiceListItemDTO(
                        item.getServiceId(),
                        item.getName(),
                        item.getUnitPrice().toString(),
                        item.getStock()
                ))
                .collect(Collectors.toList());
    }
}
