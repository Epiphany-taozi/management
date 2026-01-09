package com.ryan.property.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class DBUtil {
    private DBUtil() {}

    public static Connection getConnection() throws Exception {
        String url = AppConfig.get("db.url");
        String user = AppConfig.get("db.user");
        String password = AppConfig.get("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    public static void testConnection() {
        System.out.println("[DB] Testing MySQL connection...");
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("[DB] Connected OK. SELECT 1 = " + rs.getInt(1));
            } else {
                System.out.println("[DB] Connected but SELECT 1 returned no rows (unexpected).");
            }
        } catch (Exception e) {
            System.out.println("[DB] Connection FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
