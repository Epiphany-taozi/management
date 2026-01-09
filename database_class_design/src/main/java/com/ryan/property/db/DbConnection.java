package com.ryan.property.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConnection {
  private DbConnection() {
  }

  public static Connection open() {
    DbConfig config = DbConfig.load();
    try {
      return DriverManager.getConnection(config.url(), config.user(), config.password());
    } catch (SQLException error) {
      throw new IllegalStateException("Unable to open database connection", error);
    }
  }
}
