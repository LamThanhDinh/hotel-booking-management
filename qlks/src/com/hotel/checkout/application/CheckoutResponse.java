package com.hotel.checkout.application;

public record CheckoutResponse(
        String invoiceId,
        String bookingId,
        String roomStatusAfter,
        String grandTotal,
        String paidAt
) {
}
