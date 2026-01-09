package com.ryan.property.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.ryan.property.model.Owner;
import com.ryan.property.util.DBUtil;

public class OwnerDao {

    public List<Owner> findAll() {
        String sql = """
                SELECT id, name, phone, building, room, id_card, created_at
                FROM owner
                ORDER BY id ASC
                """;
        List<Owner> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Owner o = new Owner();
                o.setId(rs.getLong("id"));
                o.setName(rs.getString("name"));
                o.setPhone(rs.getString("phone"));
                o.setBuilding(rs.getString("building"));
                o.setRoom(rs.getString("room"));
                o.setIdCard(rs.getString("id_card"));
                o.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(o);
            }
        } catch (Exception e) {
            System.out.println("[DAO] OwnerDao.findAll failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public long insert(Owner o) {
    String sql = """
            INSERT INTO owner(name, phone, building, room, id_card)
            VALUES (?, ?, ?, ?, ?)
            """;
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

        ps.setString(1, o.getName());
        ps.setString(2, o.getPhone());
        ps.setString(3, o.getBuilding());
        ps.setString(4, o.getRoom());
        ps.setString(5, o.getIdCard());

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

public boolean update(Owner o) {
    String sql = """
            UPDATE owner
            SET name=?, phone=?, building=?, room=?, id_card=?
            WHERE id=?
            """;
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, o.getName());
        ps.setString(2, o.getPhone());
        ps.setString(3, o.getBuilding());
        ps.setString(4, o.getRoom());
        ps.setString(5, o.getIdCard());
        ps.setLong(6, o.getId());

        return ps.executeUpdate() == 1;
    } catch (Exception e) {
        System.out.println("[DAO] update failed: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

public boolean deleteById(long id) {
    String sql = "DELETE FROM owner WHERE id=?";
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

}
