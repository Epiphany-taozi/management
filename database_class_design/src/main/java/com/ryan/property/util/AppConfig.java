package com.ryan.property.util;

import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) throw new RuntimeException("Cannot find application.properties");
            PROPS.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private AppConfig() {}

    public static String get(String key) {
        String v = PROPS.getProperty(key);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Missing config: " + key);
        return v.trim();
    }
}
