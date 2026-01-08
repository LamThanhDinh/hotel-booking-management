package com.hotel.checkout.domain;

import com.hotel.rooms.domain.Money;

import java.time.Instant;
import java.util.Objects;

public class Invoice {
    private final String invoiceId;
    private final String bookingId;
    private final Money roomTotal;
    private final Money servicesTotal;
    private final Money grandTotal;
    private final InvoiceStatus status;
    private final Instant paidAt;

    public Invoice(String invoiceId,
                   String bookingId,
                   Money roomTotal,
                   Money servicesTotal,
                   Money grandTotal,
                   InvoiceStatus status,
                   Instant paidAt) {
        this.invoiceId = Objects.requireNonNull(invoiceId, "invoiceId");
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId");
        this.roomTotal = Objects.requireNonNull(roomTotal, "roomTotal");
        this.servicesTotal = Objects.requireNonNull(servicesTotal, "servicesTotal");
        this.grandTotal = Objects.requireNonNull(grandTotal, "grandTotal");
        this.status = Objects.requireNonNull(status, "status");
        this.paidAt = Objects.requireNonNull(paidAt, "paidAt");
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Money getRoomTotal() {
        return roomTotal;
    }

    public Money getServicesTotal() {
        return servicesTotal;
    }

    public Money getGrandTotal() {
        return grandTotal;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}
