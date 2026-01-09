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

import com.ryan.property.model.Complaint;
import com.ryan.property.model.ComplaintOverview;
import com.ryan.property.util.DBUtil;

public class ComplaintDao {

    /**
     * =========================
     * 完整投诉查询（用于详情/管理）
     * =========================
     */
    public List<Complaint> findByFilters(
            Long ownerId,
            String status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT c.id,
                       c.owner_id,
                       c.title,
                       c.content,
                       c.status,
                       c.created_at,
                       c.updated_at,
                       r.content     AS latest_reply_content,
                       r.created_at  AS latest_reply_time
                FROM complaints c
                LEFT JOIN complaint_replies r
                  ON r.id = (
                      SELECT cr2.id
                      FROM complaint_replies cr2
                      WHERE cr2.complaint_id = c.id
                      ORDER BY cr2.created_at DESC, cr2.id DESC
                      LIMIT 1
                  )
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (ownerId != null) {
            sql.append(" AND c.owner_id = ?");
            params.add(ownerId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND c.status = ?");
            params.add(status.trim());
        }
        if (startDate != null) {
            sql.append(" AND c.created_at >= ?");
            params.add(Timestamp.valueOf(startDate.atStartOfDay()));
        }
        if (endDate != null) {
            LocalDateTime end = endDate.atTime(23, 59, 59);
            sql.append(" AND c.created_at <= ?");
            params.add(Timestamp.valueOf(end));
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

    /**
     * =========================
     * 轻量投诉概览（列表页用）
     * =========================
     */
    public List<ComplaintOverview> findOverviewByFilters(
            String status,
            LocalDate startDate,
            LocalDate endDate
    ) {
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
                           cr.content    AS latest_reply_content
                    FROM complaint_replies cr
                    JOIN (
                        SELECT complaint_id, MAX(created_at) AS max_created_at
                        FROM complaint_replies
                        GROUP BY complaint_id
                    ) latest
                      ON latest.complaint_id = cr.complaint_id
                     AND latest.max_created_at = cr.created_at
                ) r ON r.complaint_id = c.id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND c.status = ?");
            params.add(status.trim());
        }
        if (startDate != null) {
            sql.append(" AND c.created_at >= ?");
            params.add(Timestamp.valueOf(startDate.atStartOfDay()));
        }
        if (endDate != null) {
            sql.append(" AND c.created_at <= ?");
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
                    ComplaintOverview o = new ComplaintOverview();
                    o.setId(rs.getLong("id"));
                    o.setStatus(rs.getString("status"));
                    o.setCreatedAt(rs.getTimestamp("created_at"));
                    o.setLatestReplyAt(rs.getTimestamp("latest_reply_at"));
                    o.setLatestReplyContent(rs.getString("latest_reply_content"));
                    list.add(o);
                }
            }
        } catch (Exception e) {
            System.out.println("[DAO] ComplaintDao.findOverviewByFilters failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * =========================
     * 新建投诉
     * =========================
     */
    public long insert(Complaint complaint) {
        String sql = """
                INSERT INTO complaints (owner_id, title, content, status)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     java.sql.Statement.RETURN_GENERATED_KEYS
             )) {

            ps.setLong(1, complaint.getOwnerId());
            ps.setString(2, complaint.getTitle());
            ps.setString(3, complaint.getContent());
            ps.setString(4, complaint.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return -1;
        } catch (Exception e) {
            System.out.println("[DAO] ComplaintDao.insert failed: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * =========================
     * 添加回复 + 更新状态（事务）
     * =========================
     */
    public boolean addReply(
            long complaintId,
            long staffId,
            String replyContent,
            String newStatus
    ) {
        String insertReplySql = """
                INSERT INTO complaint_replies (complaint_id, staff_id, content)
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
