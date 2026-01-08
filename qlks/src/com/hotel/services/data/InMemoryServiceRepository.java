package com.hotel.services.data;

import com.hotel.rooms.domain.Money;
import com.hotel.services.application.ServiceRepository;
import com.hotel.services.domain.ServiceItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryServiceRepository implements ServiceRepository {
    private final List<ServiceItem> items = new ArrayList<>();

    public InMemoryServiceRepository() {
        seed();
    }

    @Override
    public List<ServiceItem> findAvailableServices(String keyword) {
        String lower = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);
        return items.stream()
                .filter(ServiceItem::isAvailable)
                .filter(item -> lower.isEmpty()
                        || item.getName().toLowerCase(Locale.ROOT).contains(lower)
                        || item.getServiceId().toLowerCase(Locale.ROOT).contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceItem> findById(String serviceId) {
        return items.stream()
                .filter(item -> item.getServiceId().equalsIgnoreCase(serviceId))
                .findFirst();
    }

    @Override
    public ServiceItem save(ServiceItem serviceItem) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getServiceId().equalsIgnoreCase(serviceItem.getServiceId())) {
                items.set(i, serviceItem);
                return serviceItem;
            }
        }
        items.add(serviceItem);
        return serviceItem;
    }

    private void seed() {
        items.add(new ServiceItem("SV01", "Laundry", money(50000), 10, true));
        items.add(new ServiceItem("SV02", "Breakfast", money(80000), 20, true));
        items.add(new ServiceItem("SV03", "Airport Pickup", money(200000), 5, true));
        items.add(new ServiceItem("SV04", "Spa Package", money(300000), 3, true));
        items.add(new ServiceItem("SV05", "Mini Bar", money(120000), 15, true));
        items.add(new ServiceItem("SV06", "Extra Bed", money(150000), 0, true)); // stock 0
        items.add(new ServiceItem("SV07", "City Tour", money(400000), 0, true)); // stock 0
        items.add(new ServiceItem("SV08", "Late Checkout", money(100000), 7, true));
        items.add(new ServiceItem("SV09", "Printing", money(20000), 30, true));
    }

    private Money money(long vnd) {
        return new Money(BigDecimal.valueOf(vnd), "VND");
    }
}
