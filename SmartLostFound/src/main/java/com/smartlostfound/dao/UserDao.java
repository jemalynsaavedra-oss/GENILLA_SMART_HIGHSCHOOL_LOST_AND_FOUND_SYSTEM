package com.smartlostfound.dao;

import com.smartlostfound.model.User;
import java.sql.*;
import java.util.*;

public class UserDao {
    private final ActivityLogDao logs = new ActivityLogDao();
    public boolean register(User user) throws SQLException {
        String sql="INSERT INTO users(username,password,roles,status) VALUES(?,?,?,?)";
        int newId;
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,user.username());p.setString(2,user.password());p.setString(3,user.roles());p.setString(4,user.status());
            if(p.executeUpdate()!=1)return false;
            try(ResultSet k=p.getGeneratedKeys()){if(!k.next())throw new SQLException("Could not retrieve new user ID");newId=k.getInt(1);}
        }
        logs.logAction(newId,"USER_REGISTERED","Registered account with role " + user.roles() + ". Account status: " + user.status());
        return true;
    }
    public User login(String username,String password)throws SQLException{
        String sql="SELECT id,username,password,roles,status FROM users WHERE username=? AND password=?";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,username);p.setString(2,password);try(ResultSet r=p.executeQuery()){if(r.next())return map(r);}}return null;
    }
    public void logLogin(User u)throws SQLException{logs.logAction(u.id(),"USER_LOGIN","User " + u.username() + " logged in.");}
    public void logLogout(User u)throws SQLException{logs.logAction(u.id(),"USER_LOGOUT","User " + u.username() + " logged out.");}
    public boolean usernameExists(String username)throws SQLException{try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT 1 FROM users WHERE username=?")){p.setString(1,username);try(ResultSet r=p.executeQuery()){return r.next();}}}
    public List<User> getAllUsers()throws SQLException{return list("SELECT id,username,password,roles,status FROM users ORDER BY id",null);}
    public List<User> getPendingUsers()throws SQLException{return list("SELECT id,username,password,roles,status FROM users WHERE status='PENDING' ORDER BY id",null);}
    public boolean approveUser(int id,int adminId)throws SQLException{
        User target=findById(id); if(target==null)return false;
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET status='APPROVED' WHERE id=? AND status='PENDING'")){p.setInt(1,id);if(p.executeUpdate()!=1)return false;}
        logs.logAction(adminId,"USER_APPROVED","Admin approved account for User ID " + id + " (" + target.username() + ").");return true;
    }
    public boolean updateUserRoleAndStatus(int id,String role,String status,int adminId)throws SQLException{
        User target=findById(id); if(target==null)return false;
        String r=role.trim().toUpperCase(Locale.ROOT), st=status.trim().toUpperCase(Locale.ROOT);
        if(!Set.of("ADMIN","FINDER","OWNER").contains(r)||!Set.of("APPROVED","PENDING","REJECTED").contains(st))throw new IllegalArgumentException("Invalid role or status.");
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET roles=?,status=? WHERE id=?")){p.setString(1,r);p.setString(2,st);p.setInt(3,id);if(p.executeUpdate()!=1)return false;}
        logs.logAction(adminId,"USER_UPDATED","Admin changed role/status of User ID " + id + " (" + target.username() + ") from " + target.roles()+"/"+target.status()+" to "+r+"/"+st+".");return true;
    }
    public User findById(int id)throws SQLException{try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,username,password,roles,status FROM users WHERE id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}}
    private List<User> list(String sql,Integer id)throws SQLException{List<User> a=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())a.add(map(r));}return a;}
    private User map(ResultSet r)throws SQLException{return new User(r.getInt("id"),r.getString("username"),r.getString("password"),r.getString("roles"),r.getString("status"));}
}
