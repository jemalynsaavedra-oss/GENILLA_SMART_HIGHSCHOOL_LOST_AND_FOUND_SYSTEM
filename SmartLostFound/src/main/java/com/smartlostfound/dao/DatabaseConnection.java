package com.smartlostfound.dao;
import java.sql.*;
public final class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:smart_lost_found.db";
    private DatabaseConnection() {}
    public static Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement s = c.createStatement()) { s.execute("PRAGMA foreign_keys=ON"); s.execute("PRAGMA busy_timeout=5000"); }
        return c;
    }
}
