package com.ryan.property.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DbConfig {
  private static final String CONFIG_PATH = "/application.properties";

  private final String url;
  private final String user;
  private final String password;

  private DbConfig(String url, String user, String password) {
    this.url = url;
    this.user = user;
    this.password = password;
  }

  public static DbConfig load() {
    Properties properties = new Properties();
    try (InputStream input = DbConfig.class.getResourceAsStream(CONFIG_PATH)) {
      if (input == null) {
        throw new IllegalStateException("Missing config file: " + CONFIG_PATH);
      }
      properties.load(input);
    } catch (IOException error) {
      throw new IllegalStateException("Failed to read config file: " + CONFIG_PATH, error);
    }
    String url = require(properties, "db.url");
    String user = require(properties, "db.user");
    String password = require(properties, "db.password");
    return new DbConfig(url, user, password);
  }

  private static String require(Properties properties, String key) {
    String value = properties.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing config value: " + key);
    }
    return value.trim();
  }

  public String url() {
    return url;
  }

  public String user() {
    return user;
  }

  public String password() {
    return password;
  }
}
