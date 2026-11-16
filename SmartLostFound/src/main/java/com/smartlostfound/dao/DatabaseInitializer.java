package com.smartlostfound.dao;

import com.smartlostfound.model.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;

/** Makes sure every required table exists before the CLI starts. */
public class DatabaseInitializer {
    /** Builds ('ADMIN', 'FINDER', 'OWNER') straight from the Role enum. */
    private static final String ROLE_LIST = buildRoleList();

    private static final String CREATE_USERS_TABLE = usersTableSql("users");
    private static final String CREATE_USERS_NEW_TABLE = usersTableSql("users_new");

    private static final String SEED_ADMIN = """
            INSERT INTO users (username, password, roles, status)
            SELECT ?, ?, 'ADMIN', 'APPROVED'
            WHERE NOT EXISTS (SELECT 1 FROM users WHERE roles = 'ADMIN')
            """;

    private DatabaseInitializer() {
    }

    private static String buildRoleList() {
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        for (Role role : Role.values()) {
            joiner.add("'" + role.name() + "'");
        }
        return joiner.toString();
    }

    private static String usersTableSql(String tableName) {
        return "CREATE TABLE " + (tableName.equals("users") ? "IF NOT EXISTS " : "") + tableName + " ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + " password TEXT NOT NULL,"
                + " roles TEXT NOT NULL CHECK (roles IN " + ROLE_LIST + "),"
                + " status TEXT NOT NULL DEFAULT 'PENDING'"
                + " CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))"
                + ")";
    }

    public static void initialize() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            if (usersTableIsOutdated(stmt)) {
                migrateUsersTable(stmt);
            }
            stmt.execute(CREATE_USERS_TABLE);
            seedDefaultAdmin(conn);
        }
    }

    /** An older database may have a users table whose CHECK does not list the current roles. */
    private static boolean usersTableIsOutdated(Statement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'users'")) {
            if (!rs.next()) {
                return false;
            }
            String ddl = rs.getString(1);
            for (Role role : Role.values()) {
                if (!ddl.contains("'" + role.name() + "'")) {
                    return true;
                }
            }
            // Old role names (e.g. TEACHER) still in the CHECK also mean the table is outdated.
            return ddl.contains("'TEACHER'") || ddl.contains("'STUDENT'")
                    || ddl.contains("'STAFF'") || ddl.contains("'SECURITY'");
        }
    }

    /**
     * Rebuilds the users table with the current role list. Rows that still hold a retired
     * role (TEACHER, STUDENT, STAFF, SECURITY) are converted to OWNER so no data is lost.
     */
    private static void migrateUsersTable(Statement stmt) throws SQLException {
        stmt.execute("DROP TABLE IF EXISTS users_new");
        stmt.execute(CREATE_USERS_NEW_TABLE);
        stmt.execute("INSERT INTO users_new (id, username, password, roles, status) "
                + "SELECT id, username, password, "
                + "CASE WHEN roles IN " + ROLE_LIST + " THEN roles ELSE 'OWNER' END, "
                + "status FROM users");
        stmt.execute("DROP TABLE users");
        stmt.execute("ALTER TABLE users_new RENAME TO users");
    }

    /** Ensures at least one approved ADMIN exists so pending accounts can be approved. */
    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SEED_ADMIN)) {
            ps.setString(1, "admin");
            ps.setString(2, "admin123");
            ps.executeUpdate();
        }
    }
}
