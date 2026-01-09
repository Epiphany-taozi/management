package com.ryan.property.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ryan.property.model.ComplaintOverview;
import com.ryan.property.util.DBUtil;

public class ComplaintDao {

    public List<ComplaintOverview> findByFilters(String status, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT c.id,
                       c.status,
                       c.created_at,
                       r.latest_reply_at,
                       r.latest_reply_content
                FROM complaints c
                LEFT JOIN (
                    SELECT cr.complaint_id,
                           cr.created_at AS latest_reply_at,
                           cr.content AS latest_reply_content
                    FROM complaint_replies cr
                    JOIN (
                        SELECT complaint_id, MAX(created_at) AS max_created_at
                        FROM complaint_replies
                        GROUP BY complaint_id
                    ) latest ON latest.complaint_id = cr.complaint_id
                             AND latest.max_created_at = cr.created_at
                ) r ON r.complaint_id = c.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND c.status = ?\n");
            params.add(status.trim());
        }
        if (startDate != null) {
            sql.append(" AND c.created_at >= ?\n");
            params.add(Timestamp.valueOf(startDate.atStartOfDay()));
        }
        if (endDate != null) {
            sql.append(" AND c.created_at <= ?\n");
            params.add(Timestamp.valueOf(endDate.atTime(LocalTime.MAX)));
        }

        sql.append(" ORDER BY c.created_at DESC");

        List<ComplaintOverview> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ComplaintOverview item = new ComplaintOverview();
                    item.setId(rs.getLong("id"));
                    item.setStatus(rs.getString("status"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    item.setLatestReplyAt(rs.getTimestamp("latest_reply_at"));
                    item.setLatestReplyContent(rs.getString("latest_reply_content"));
                    list.add(item);
                }
            }
        } catch (Exception e) {
            System.out.println("[DAO] ComplaintDao.findByFilters failed: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
