package com.ryan.property.model;

import java.math.BigDecimal;

public class ParkingUsageStats {

    private int usageYear;
    private int usageCount;
    private BigDecimal totalFee;

    public ParkingUsageStats(int usageYear, int usageCount, BigDecimal totalFee) {
        this.usageYear = usageYear;
        this.usageCount = usageCount;
        this.totalFee = totalFee;
    }

    public int getUsageYear() {
        return usageYear;
    }

    public void setUsageYear(int usageYear) {
        this.usageYear = usageYear;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }
}
