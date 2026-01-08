package com.hotel.services.domain;

import com.hotel.rooms.domain.Money;

import java.util.Objects;

public class ServiceItem {
    private final String serviceId;
    private final String name;
    private final Money unitPrice;
    private int stock;
    private boolean available;

    public ServiceItem(String serviceId, String name, Money unitPrice, int stock, boolean available) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
        this.name = Objects.requireNonNull(name, "name");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
        if (stock < 0) {
            throw new IllegalArgumentException("stock must be non-negative");
        }
        this.stock = stock;
        this.available = available;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getStock() {
        return stock;
    }

    public boolean isAvailable() {
        return available && stock > 0;
    }

    public boolean canUse(int quantity) {
        return quantity > 0 && isAvailable() && stock >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (quantity > stock) {
            throw new IllegalArgumentException("quantity exceeds stock");
        }
        this.stock -= quantity;
        if (this.stock <= 0) {
            this.available = false;
        }
    }
}
