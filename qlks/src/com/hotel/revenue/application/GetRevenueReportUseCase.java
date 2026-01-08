package com.hotel.revenue.application;

import com.hotel.checkout.application.InvoiceRepository;
import com.hotel.checkout.domain.Invoice;
import com.hotel.checkout.domain.InvoiceStatus;
import com.hotel.rooms.domain.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRevenueReportUseCase {
    private final InvoiceRepository invoiceRepository;

    public GetRevenueReportUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public RevenueReportDTO execute(LocalDate from, LocalDate to) {
        LocalDate normalizedFrom = from == null ? LocalDate.now().minusDays(30) : from;
        LocalDate normalizedTo = to == null ? LocalDate.now() : to;

        Instant fromInstant = normalizedFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = normalizedTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusSeconds(1);

        List<Invoice> invoices = invoiceRepository.listPaidBetween(fromInstant, toInstant);
        BigDecimal total = BigDecimal.ZERO;
        Map<LocalDate, BigDecimal> daily = new HashMap<>();
        String currency = invoices.isEmpty() ? "VND" : invoices.get(0).getGrandTotal().getCurrency();

        for (Invoice inv : invoices) {
            if (inv.getStatus() != InvoiceStatus.PAID) continue;
            BigDecimal amt = inv.getGrandTotal().getAmount();
            total = total.add(amt);
            LocalDate day = LocalDate.ofInstant(inv.getPaidAt(), ZoneOffset.UTC);
            daily.put(day, daily.getOrDefault(day, BigDecimal.ZERO).add(amt));
        }

        Money totalMoney = new Money(total, currency);
        List<RevenueDailyLineDTO> dailyLines = new ArrayList<>();
        daily.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> dailyLines.add(new RevenueDailyLineDTO(e.getKey().toString(), new Money(e.getValue(), currency).toString())));

        return new RevenueReportDTO(
                normalizedFrom.toString(),
                normalizedTo.toString(),
                totalMoney.toString(),
                invoices.size(),
                dailyLines
        );
    }
}
