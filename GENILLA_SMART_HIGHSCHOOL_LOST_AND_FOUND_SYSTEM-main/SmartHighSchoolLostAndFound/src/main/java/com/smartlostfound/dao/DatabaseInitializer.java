package com.smartlostfound.dao;

import com.smartlostfound.model.Role;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Makes sure every required table exists before the CLI starts. */
public class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            if (usersTableIsOutdated(conn)) {
                migrateUsersTable(stmt);
            }
            stmt.execute(usersTableSql("users"));
        }
    }

    private static String usersTableSql(String tableName) {
        String roles = Arrays.stream(Role.values())
                .map(r -> "'" + r.name() + "'")
                .collect(Collectors.joining(", "));
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL CHECK (roles IN (%s)),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
                )
                """.formatted(tableName, roles);
    }

    /** An older database may have a users table whose CHECK only allows TEACHER and STUDENT. */
    private static boolean usersTableIsOutdated(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'users'")) {
            if (!rs.next()) {
                return false;
            }
            String ddl = rs.getString(1);
            return Arrays.stream(Role.values()).anyMatch(r -> !ddl.contains("'" + r.name() + "'"));
        }
    }

    private static void migrateUsersTable(Statement stmt) throws SQLException {
        stmt.execute("DROP TABLE IF EXISTS users_new");
        stmt.execute(usersTableSql("users_new"));
        stmt.execute("INSERT INTO users_new (id, username, password, roles, status) "
                + "SELECT id, username, password, roles, status FROM users");
        stmt.execute("DROP TABLE users");
        stmt.execute("ALTER TABLE users_new RENAME TO users");
    }
}
