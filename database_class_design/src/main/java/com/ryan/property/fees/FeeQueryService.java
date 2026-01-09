package com.ryan.property.fees;

import com.ryan.property.db.DbConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FeeQueryService {
  public List<FeeRecord> query(FeeFilter filter) {
    StringBuilder sql = new StringBuilder(
        """
            SELECT f.id,
                   f.owner_id,
                   o.name AS owner_name,
                   f.fee_type,
                   f.status,
                   f.amount,
                   f.paid_at
            FROM fees f
            JOIN owners o ON o.id = f.owner_id
            WHERE 1=1
            """);
    List<Object> parameters = new ArrayList<>();

    if (filter != null) {
      if (filter.hasOwnerKeyword()) {
        sql.append(" AND (o.name LIKE ? OR o.phone LIKE ?)");
        String like = like(filter.ownerKeyword());
        parameters.add(like);
        parameters.add(like);
      }
      if (filter.hasFeeType()) {
        sql.append(" AND f.fee_type = ?");
        parameters.add(filter.feeType());
      }
      if (filter.hasStatus()) {
        sql.append(" AND f.status = ?");
        parameters.add(filter.status());
      }
      if (filter.hasStartDate()) {
        sql.append(" AND f.paid_at >= ?");
        parameters.add(toStartOfDay(filter.startDate()));
      }
      if (filter.hasEndDate()) {
        sql.append(" AND f.paid_at <= ?");
        parameters.add(toEndOfDay(filter.endDate()));
      }
    }

    sql.append(" ORDER BY f.id DESC");

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
    return new FeeRecord(
        resultSet.getLong("id"),
        resultSet.getLong("owner_id"),
        resultSet.getString("owner_name"),
        resultSet.getString("fee_type"),
        resultSet.getString("status"),
        resultSet.getBigDecimal("amount"),
        toLocalDate(resultSet.getDate("paid_at")));
  }

  private LocalDate toLocalDate(Date date) {
    if (date == null) {
      return null;
    }
    return date.toLocalDate();
  }

  private Timestamp toStartOfDay(LocalDate date) {
    return Timestamp.valueOf(date.atStartOfDay());
  }

  private Timestamp toEndOfDay(LocalDate date) {
    return Timestamp.valueOf(date.atTime(LocalTime.MAX));
  }

  private String like(String keyword) {
    return "%" + keyword.trim() + "%";
  }

  public List<FeeSummary> querySummary() {
    String sql = """
        SELECT YEAR(paid_at) AS paid_year,
               fee_type,
               SUM(amount) AS total_amount,
               COUNT(*) AS total_count
        FROM fees
        WHERE paid_at IS NOT NULL
        GROUP BY YEAR(paid_at), fee_type
        ORDER BY paid_year, fee_type
        """;

    try (Connection connection = DbConnection.open();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
      List<FeeSummary> summaries = new ArrayList<>();
      while (resultSet.next()) {
        summaries.add(new FeeSummary(
            resultSet.getInt("paid_year"),
            resultSet.getString("fee_type"),
            resultSet.getBigDecimal("total_amount"),
            resultSet.getInt("total_count")));
      }
      return summaries;
    } catch (SQLException error) {
      throw new IllegalStateException("Failed to query fee summary", error);
    }
  }
}
