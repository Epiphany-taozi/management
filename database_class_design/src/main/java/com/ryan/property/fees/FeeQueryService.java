package com.ryan.property.fees;

import com.ryan.property.db.DbConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeeQueryService {
  public List<FeeRecord> query(FeeFilter filter) {
    StringBuilder sql = new StringBuilder(
        "SELECT fee_type, status, amount, paid_date FROM fees WHERE 1=1");
    List<Object> parameters = new ArrayList<>();

    if (filter != null) {
      if (filter.hasFeeType()) {
        sql.append(" AND fee_type = ?");
        parameters.add(filter.feeType());
      }
      if (filter.hasStatus()) {
        sql.append(" AND status = ?");
        parameters.add(filter.status());
      }
      if (filter.hasStartDate()) {
        sql.append(" AND fee_date >= ?");
        parameters.add(Date.valueOf(filter.startDate()));
      }
      if (filter.hasEndDate()) {
        sql.append(" AND fee_date <= ?");
        parameters.add(Date.valueOf(filter.endDate()));
      }
    }

    try (Connection connection = DbConnection.open();
        PreparedStatement statement = prepare(connection, sql, parameters);
        ResultSet resultSet = statement.executeQuery()) {
      List<FeeRecord> records = new ArrayList<>();
      while (resultSet.next()) {
        records.add(mapRecord(resultSet));
      }
      return records;
    } catch (SQLException error) {
      throw new IllegalStateException("Failed to query fees", error);
    }
  }

  private PreparedStatement prepare(Connection connection, StringBuilder sql, List<Object> parameters)
      throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql.toString());
    for (int index = 0; index < parameters.size(); index++) {
      statement.setObject(index + 1, parameters.get(index));
    }
    return statement;
  }

  private FeeRecord mapRecord(ResultSet resultSet) throws SQLException {
    String feeType = resultSet.getString("fee_type");
    String status = resultSet.getString("status");
    return new FeeRecord(
        feeType,
        status,
        resultSet.getBigDecimal("amount"),
        toLocalDate(resultSet.getDate("paid_date")));
  }

  private LocalDate toLocalDate(Date date) {
    if (date == null) {
      return null;
    }
    return date.toLocalDate();
  }
}
