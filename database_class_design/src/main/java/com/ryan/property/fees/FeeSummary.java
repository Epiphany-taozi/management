package com.ryan.property.fees;

import java.math.BigDecimal;

public record FeeSummary(int paidYear, String feeType, BigDecimal totalAmount, int totalCount) {
}
