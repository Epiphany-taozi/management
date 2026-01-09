package com.ryan.property.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.ryan.property.model.Staff;
import com.ryan.property.util.DBUtil;

public class StaffDao {

    public List<Staff> findAll() {
        String sql = """
                SELECT id, name, role, phone
                FROM staff
                ORDER BY id ASC
                """;
        return queryList(sql, null);
    }

    public List<Staff> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String sql = """
                SELECT id, name, role, phone
                FROM staff
                WHERE name LIKE ?
                   OR phone LIKE ?
                   OR role LIKE ?
                ORDER BY id ASC
                """;
        String like = like(keyword);
        return queryList(sql, new String[] { like, like, like });
    }

    private List<Staff> queryList(String sql, String[] params) {
        List<Staff> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Staff s = new Staff();
                    s.setId(rs.getLong("id"));
                    s.setName(rs.getString("name"));
                    s.setRole(rs.getString("role"));
                    s.setPhone(rs.getString("phone"));
                    list.add(s);
                }
            }
        } catch (Exception e) {
            System.out.println("[DAO] StaffDao query failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public long insert(Staff s) {
        String sql = """
                INSERT INTO staff(name, role, phone)
                VALUES (?, ?, ?)
                """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getName());
            ps.setString(2, s.getRole());
            ps.setString(3, s.getPhone());

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        } catch (Exception e) {
            System.out.println("[DAO] insert failed: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public boolean update(Staff s) {
        String sql = """
                UPDATE staff
                SET name=?, role=?, phone=?
                WHERE id=?
                """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getName());
            ps.setString(2, s.getRole());
            ps.setString(3, s.getPhone());
            ps.setLong(4, s.getId());

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.out.println("[DAO] update failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteById(long id) {
        String sql = "DELETE FROM staff WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.out.println("[DAO] delete failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String like(String keyword) {
        return "%" + keyword.trim() + "%";
    }
}
