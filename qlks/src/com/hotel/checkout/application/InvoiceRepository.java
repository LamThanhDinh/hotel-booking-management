package com.hotel.checkout.application;

import com.hotel.checkout.domain.Invoice;
import com.hotel.checkout.domain.InvoiceStatus;

import java.time.Instant;
import java.util.List;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);

    List<Invoice> listPaidBetween(Instant from, Instant to);

    List<Invoice> findAll();
}
