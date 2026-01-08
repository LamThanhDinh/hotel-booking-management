package com.hotel.revenue.domain;

import com.hotel.rooms.domain.Money;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class RevenueReport {
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final Money totalRevenue;
    private final int invoiceCount;
    private final Map<LocalDate, Money> dailyBreakdown;

    public RevenueReport(LocalDate fromDate, LocalDate toDate, Money totalRevenue, int invoiceCount, Map<LocalDate, Money> dailyBreakdown) {
        this.fromDate = Objects.requireNonNull(fromDate, "fromDate");
        this.toDate = Objects.requireNonNull(toDate, "toDate");
        this.totalRevenue = Objects.requireNonNull(totalRevenue, "totalRevenue");
        this.invoiceCount = invoiceCount;
        this.dailyBreakdown = dailyBreakdown == null ? Map.of() : dailyBreakdown;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public Money getTotalRevenue() {
        return totalRevenue;
    }

    public int getInvoiceCount() {
        return invoiceCount;
    }

    public Map<LocalDate, Money> getDailyBreakdown() {
        return Collections.unmodifiableMap(dailyBreakdown);
    }
}
