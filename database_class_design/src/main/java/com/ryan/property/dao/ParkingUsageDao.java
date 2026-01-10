package com.ryan.property.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ryan.property.model.ParkingUsageFilter;
import com.ryan.property.model.ParkingUsageRecord;
import com.ryan.property.model.ParkingUsageStats;
import com.ryan.property.util.DBUtil;

public class ParkingUsageDao {

    public List<ParkingUsageRecord> findByFilter(ParkingUsageFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT pu.id,
                       pu.slot_id,
                       ps.location AS slot_location,
                       pu.owner_id,
                       o.name AS owner_name,
                       o.phone AS owner_phone,
                       pu.start_time,
                       pu.end_time,
                       pu.fee
                FROM parking_usage pu
                JOIN parking_slots ps ON ps.id = pu.slot_id
                JOIN owners o ON o.id = pu.owner_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (filter != null) {
            if (filter.hasKeyword()) {
                sql.append(" AND (ps.location LIKE ? OR o.name LIKE ? OR o.phone LIKE ?)");
                String like = like(filter.keyword());
                params.add(like);
                params.add(like);
                params.add(like);
            }
            if (filter.hasStatus()) {
                if ("ACTIVE".equalsIgnoreCase(filter.status())) {
                    sql.append(" AND pu.end_time IS NULL");
                } else if ("ENDED".equalsIgnoreCase(filter.status())) {
                    sql.append(" AND pu.end_time IS NOT NULL");
                }
            }
            if (filter.hasStartDate()) {
                sql.append(" AND pu.start_time >= ?");
                params.add(toStartOfDay(filter.startDate()));
            }
            if (filter.hasEndDate()) {
                sql.append(" AND pu.start_time <= ?");
                params.add(toEndOfDay(filter.endDate()));
            }
        }

        sql.append(" ORDER BY pu.start_time DESC");

        List<ParkingUsageRecord> records = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRecord(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("[DAO] ParkingUsageDao query failed: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }

    public List<ParkingUsageStats> fetchYearlyStats() {
        String sql = """
                SELECT YEAR(start_time) AS usage_year,
                       COUNT(*) AS usage_count,
                       SUM(fee) AS total_fee
                FROM parking_usage
                GROUP BY YEAR(start_time)
                ORDER BY usage_year
                """;
        List<ParkingUsageStats> stats = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stats.add(new ParkingUsageStats(
                        rs.getInt("usage_year"),
                        rs.getInt("usage_count"),
                        rs.getBigDecimal("total_fee")));
            }
        } catch (Exception e) {
            System.out.println("[DAO] ParkingUsageDao stats query failed: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    private ParkingUsageRecord mapRecord(ResultSet rs) throws Exception {
        ParkingUsageRecord record = new ParkingUsageRecord();
        record.setId(rs.getLong("id"));
        record.setSlotId(rs.getLong("slot_id"));
        record.setSlotLocation(rs.getString("slot_location"));
        record.setOwnerId(rs.getLong("owner_id"));
        record.setOwnerName(rs.getString("owner_name"));
        record.setOwnerPhone(rs.getString("owner_phone"));
        record.setStartTime(toLocalDateTime(rs.getTimestamp("start_time")));
        record.setEndTime(toLocalDateTime(rs.getTimestamp("end_time")));
        record.setFee(rs.getBigDecimal("fee"));
        return record;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
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
}
