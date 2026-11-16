package com.smartlostfound.dao;
import com.smartlostfound.model.ActivityLog;
import java.sql.*;
import java.util.*;

public class ActivityLogDao {
    public void logAction(int userId, String actionType, String description) throws SQLException {
        String sql = "INSERT INTO activity_logs(user_id,action_type,description) VALUES(?,?,?)";
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,userId); p.setString(2,actionType); p.setString(3,description); p.executeUpdate();
        }
    }
    public List<ActivityLog> getAllLogs() throws SQLException { return query(null); }
    public List<ActivityLog> getLogsByUserId(int userId) throws SQLException { return query(userId); }
    private List<ActivityLog> query(Integer userId) throws SQLException {
        String sql = "SELECT l.id,l.user_id,COALESCE(u.username,'[deleted user]') username,l.action_type,l.description,l.timestamp FROM activity_logs l LEFT JOIN users u ON u.id=l.user_id " + (userId==null?"":"WHERE l.user_id=? ") + "ORDER BY l.timestamp DESC,l.id DESC";
        List<ActivityLog> out=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            if(userId!=null)p.setInt(1,userId);
            try(ResultSet r=p.executeQuery()){while(r.next()){int id=r.getInt("user_id"); Integer uid=r.wasNull()?null:id; out.add(new ActivityLog(r.getInt("id"),uid,r.getString("username"),r.getString("action_type"),r.getString("description"),r.getString("timestamp")));}}
        } return out;
    }
    public int clearAllLogs() throws SQLException {
        try(Connection c=DatabaseConnection.getConnection(); Statement s=c.createStatement()){return s.executeUpdate("DELETE FROM activity_logs");}
    }
}
