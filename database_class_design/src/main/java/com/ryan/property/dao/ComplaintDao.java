package com.ryan.property.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ryan.property.model.Complaint;
import com.ryan.property.util.DBUtil;

public class ComplaintDao {

    public List<Complaint> findByFilters(Long ownerId, String status, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT c.id, c.owner_id, c.title, c.content, c.status, c.created_at, c.updated_at,
                       r.reply_content AS latest_reply_content, r.reply_time AS latest_reply_time
                FROM complaints c
                LEFT JOIN complaint_replies r ON r.id = (
                    SELECT r2.id
                    FROM complaint_replies r2
                    WHERE r2.complaint_id = c.id
                    ORDER BY r2.reply_time DESC, r2.id DESC
                    LIMIT 1
                )
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();
        if (ownerId != null) {
            sql.append(" AND c.owner_id = ?");
            params.add(ownerId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND c.status = ?");
            params.add(status);
        }
        if (startDate != null) {
            sql.append(" AND c.created_at >= ?");
            params.add(Timestamp.valueOf(startDate.atStartOfDay()));
        }
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            sql.append(" AND c.created_at <= ?");
            params.add(Timestamp.valueOf(endDateTime));
        }
        sql.append(" ORDER BY c.id DESC");

        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Complaint c = new Complaint();
                    c.setId(rs.getLong("id"));
                    c.setOwnerId(rs.getLong("owner_id"));
                    c.setTitle(rs.getString("title"));
                    c.setContent(rs.getString("content"));
                    c.setStatus(rs.getString("status"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    c.setUpdatedAt(rs.getTimestamp("updated_at"));
                    c.setLatestReplyContent(rs.getString("latest_reply_content"));
                    c.setLatestReplyTime(rs.getTimestamp("latest_reply_time"));
                    list.add(c);
                }
            }
        } catch (Exception e) {
            System.out.println("[DAO] ComplaintDao.findByFilters failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public long insert(Complaint complaint) {
        String sql = """
                INSERT INTO complaints (owner_id, title, content, status)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, complaint.getOwnerId());
            ps.setString(2, complaint.getTitle());
            ps.setString(3, complaint.getContent());
            ps.setString(4, complaint.getStatus());

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        } catch (Exception e) {
            System.out.println("[DAO] ComplaintDao.insert failed: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public boolean addReply(long complaintId, long staffId, String replyContent, String newStatus) {
        String insertReplySql = """
                INSERT INTO complaint_replies (complaint_id, staff_id, reply_content)
                VALUES (?, ?, ?)
                """;
        String updateComplaintSql = """
                UPDATE complaints
                SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psReply = conn.prepareStatement(insertReplySql);
                 PreparedStatement psUpdate = conn.prepareStatement(updateComplaintSql)) {

                psReply.setLong(1, complaintId);
                psReply.setLong(2, staffId);
                psReply.setString(3, replyContent);
                psReply.executeUpdate();

                psUpdate.setString(1, newStatus);
                psUpdate.setLong(2, complaintId);
                psUpdate.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.out.println("[DAO] ComplaintDao.addReply failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
