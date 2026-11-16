package com.smartlostfound.dao;

import com.smartlostfound.model.Role;
import java.sql.*;
import java.util.StringJoiner;

public final class DatabaseInitializer {
    private static final String ROLE_LIST = buildRoleList();
    private DatabaseInitializer() {}
    private static String buildRoleList() { StringJoiner j = new StringJoiner(", ", "(", ")"); for (Role r : Role.values()) j.add("'" + r.name() + "'"); return j.toString(); }
    private static String usersSql(String name, boolean ifNotExists) {
        return "CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + name + " (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL, roles TEXT NOT NULL CHECK(roles IN " + ROLE_LIST + "), status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','APPROVED','REJECTED')))";
    }
    public static void initialize() throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement()) {
            if (usersTableIsOutdated(s)) {
                s.execute("DROP TABLE IF EXISTS users_new"); s.execute(usersSql("users_new", false));
                s.execute("INSERT INTO users_new(id,username,password,roles,status) SELECT id,username,password,CASE WHEN roles IN " + ROLE_LIST + " THEN roles ELSE 'OWNER' END,status FROM users");
                s.execute("DROP TABLE users"); s.execute("ALTER TABLE users_new RENAME TO users");
            }
            s.execute(usersSql("users", true));
            s.execute("CREATE TABLE IF NOT EXISTS activity_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, action_type TEXT NOT NULL, description TEXT NOT NULL, timestamp TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f','now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE SET NULL)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_activity_logs_timestamp ON activity_logs(timestamp DESC)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_activity_logs_user ON activity_logs(user_id)");
            s.execute("CREATE TABLE IF NOT EXISTS found_items (id INTEGER PRIMARY KEY AUTOINCREMENT, item_name TEXT NOT NULL, description TEXT NOT NULL, location TEXT NOT NULL, date_found TEXT NOT NULL, finder_id INTEGER NOT NULL, owner_id INTEGER, status TEXT NOT NULL DEFAULT 'FOUND' CHECK(status IN ('FOUND','CLAIMED','RETURNED')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f','now','localtime')), FOREIGN KEY(finder_id) REFERENCES users(id), FOREIGN KEY(owner_id) REFERENCES users(id))");
            s.execute("CREATE INDEX IF NOT EXISTS idx_found_items_status ON found_items(status)");
            try (PreparedStatement p = c.prepareStatement("INSERT INTO users(username,password,roles,status) SELECT ?,?,'ADMIN','APPROVED' WHERE NOT EXISTS(SELECT 1 FROM users WHERE roles='ADMIN')")) { p.setString(1,"admin"); p.setString(2,"admin123"); p.executeUpdate(); }
        }
    }
    private static boolean usersTableIsOutdated(Statement s) throws SQLException {
        try (ResultSet r = s.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='users'")) {
            if (!r.next()) return false; String ddl = r.getString(1);
            for (Role role : Role.values()) if (!ddl.contains("'" + role.name() + "'")) return true;
            return ddl.contains("'TEACHER'") || ddl.contains("'STUDENT'") || ddl.contains("'STAFF'") || ddl.contains("'SECURITY'");
        }
    }
}
