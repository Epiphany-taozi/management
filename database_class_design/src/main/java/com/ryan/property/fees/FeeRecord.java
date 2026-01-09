package com.ryan.property.fees;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeRecord(String feeType, String status, BigDecimal amount, LocalDate paidDate) {
  public String statusLabel() {
    if (status == null) {
      return "未知";
    }
    if ("PAID".equalsIgnoreCase(status) || "已缴".equalsIgnoreCase(status)) {
      return "已缴";
    }
    if ("UNPAID".equalsIgnoreCase(status) || "未缴".equalsIgnoreCase(status)) {
      return "未缴";
    }
    return status;
  }
}
