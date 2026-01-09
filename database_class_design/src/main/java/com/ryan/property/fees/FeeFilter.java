package com.ryan.property.fees;

import java.time.LocalDate;

public record FeeFilter(String ownerKeyword, String feeType, String status, LocalDate startDate, LocalDate endDate) {
  public boolean hasOwnerKeyword() {
    return ownerKeyword != null && !ownerKeyword.isBlank();
  }

  public boolean hasFeeType() {
    return feeType != null && !feeType.isBlank();
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
