package com.ryan.property.model;

import java.time.LocalDate;

public record ParkingUsageFilter(String keyword, String status, LocalDate startDate, LocalDate endDate) {
    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

    public boolean hasStatus() {
        return status != null && !status.isBlank();
    }

    public boolean hasStartDate() {
        return startDate != null;
    }

    public boolean hasEndDate() {
        return endDate != null;
    }
}
