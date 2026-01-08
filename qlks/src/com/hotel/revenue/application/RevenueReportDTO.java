package com.hotel.revenue.application;

import java.util.List;

public record RevenueReportDTO(
        String from,
        String to,
        String totalRevenue,
        int invoiceCount,
        List<RevenueDailyLineDTO> dailyLines
) {
}
