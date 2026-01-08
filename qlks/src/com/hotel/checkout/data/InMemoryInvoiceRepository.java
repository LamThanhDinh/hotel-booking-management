package com.hotel.checkout.data;

import com.hotel.checkout.application.InvoiceRepository;
import com.hotel.checkout.domain.Invoice;
import com.hotel.checkout.domain.InvoiceStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryInvoiceRepository implements InvoiceRepository {
    private final List<Invoice> invoices = new ArrayList<>();

    public InMemoryInvoiceRepository() {
        seed();
    }

    @Override
    public Invoice save(Invoice invoice) {
        for (int i = 0; i < invoices.size(); i++) {
            if (invoices.get(i).getInvoiceId().equalsIgnoreCase(invoice.getInvoiceId())) {
                invoices.set(i, invoice);
                return invoice;
            }
        }
        invoices.add(invoice);
        return invoice;
    }

    @Override
    public List<Invoice> listPaidBetween(Instant from, Instant to) {
        Instant start = from == null ? Instant.EPOCH : from;
        Instant end = to == null ? Instant.now() : to;
        return invoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .filter(inv -> !inv.getPaidAt().isBefore(start) && !inv.getPaidAt().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findAll() {
        return Collections.unmodifiableList(invoices);
    }

    private void seed() {
        // Seed empty; invoices sẽ được tạo khi checkout. Có thể thêm mẫu nếu cần.
    }
}
