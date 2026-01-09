package com.ryan.property.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.ryan.property.model.Asset;
import com.ryan.property.util.DBUtil;

public class AssetDao {

    public List<Asset> findAll() {
        String sql = """
                SELECT id, name, location, status
                FROM assets
                ORDER BY id ASC
                """;
        return queryList(sql, null);
    }

    public List<Asset> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String sql = """
                SELECT id, name, location, status
                FROM assets
                WHERE name LIKE ?
                   OR location LIKE ?
                   OR status LIKE ?
                ORDER BY id ASC
                """;
        String like = like(keyword);
        return queryList(sql, new String[] { like, like, like });
    }

    private List<Asset> queryList(String sql, String[] params) {
        List<Asset> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asset a = new Asset();
                    a.setId(rs.getLong("id"));
                    a.setName(rs.getString("name"));
                    a.setLocation(rs.getString("location"));
                    a.setStatus(rs.getString("status"));
                    list.add(a);
                }
            }
        } catch (Exception e) {
            System.out.println("[DAO] AssetDao query failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public long insert(Asset a) {
        String sql = """
                INSERT INTO assets(name, location, status)
                VALUES (?, ?, ?)
                """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getName());
            ps.setString(2, a.getLocation());
            ps.setString(3, a.getStatus());

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

    public boolean update(Asset a) {
        String sql = """
                UPDATE assets
                SET name=?, location=?, status=?
                WHERE id=?
                """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getName());
            ps.setString(2, a.getLocation());
            ps.setString(3, a.getStatus());
            ps.setLong(4, a.getId());

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.out.println("[DAO] update failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteById(long id) {
        String sql = "DELETE FROM assets WHERE id=?";
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
