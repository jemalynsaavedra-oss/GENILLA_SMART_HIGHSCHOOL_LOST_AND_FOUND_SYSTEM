package com.smartlostfound.dao;
import com.smartlostfound.model.FoundItem;
import java.sql.*;
import java.util.*;

public class FoundItemDao {
    private final ActivityLogDao logs=new ActivityLogDao();
    public int create(String name,String description,String location,String date,int finderId)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO found_items(item_name,description,location,date_found,finder_id) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,name);p.setString(2,description);p.setString(3,location);p.setString(4,date);p.setInt(5,finderId);p.executeUpdate();int id;try(ResultSet k=p.getGeneratedKeys()){if(!k.next())throw new SQLException("Could not retrieve item ID");id=k.getInt(1);}logs.logAction(finderId,"ITEM_CREATED","Created found-item record #"+id+": "+name+".");return id;
        }
    }
    public List<FoundItem> getAll()throws SQLException{return query("SELECT * FROM found_items ORDER BY id DESC",null);}
    /** All Finder reports that are still available for an Owner to claim. */
    public List<FoundItem> getAvailableItems()throws SQLException{return query("SELECT * FROM found_items WHERE status='FOUND' ORDER BY id DESC",null);}
    public List<FoundItem> getByFinder(int finderId)throws SQLException{return query("SELECT * FROM found_items WHERE finder_id=? ORDER BY id DESC",finderId);}
    public List<FoundItem> search(String term)throws SQLException{
        String q="SELECT * FROM found_items WHERE status='FOUND' AND (item_name LIKE ? OR description LIKE ? OR location LIKE ?) ORDER BY id DESC";
        List<FoundItem> a=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){String v="%"+term+"%";p.setString(1,v);p.setString(2,v);p.setString(3,v);try(ResultSet r=p.executeQuery()){while(r.next())a.add(map(r));}}return a;
    }
    public boolean update(int id,String name,String description,String location,String date,int finderId)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE found_items SET item_name=?,description=?,location=?,date_found=? WHERE id=? AND finder_id=? AND status='FOUND'")){p.setString(1,name);p.setString(2,description);p.setString(3,location);p.setString(4,date);p.setInt(5,id);p.setInt(6,finderId);if(p.executeUpdate()!=1)return false;}
        logs.logAction(finderId,"ITEM_UPDATED","Updated found-item record #"+id+" ("+name+").");return true;
    }
    public boolean delete(int id,int finderId)throws SQLException{
        String name;try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT item_name FROM found_items WHERE id=? AND finder_id=? AND status='FOUND'")){p.setInt(1,id);p.setInt(2,finderId);try(ResultSet r=p.executeQuery()){if(!r.next())return false;name=r.getString(1);}}
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM found_items WHERE id=? AND finder_id=? AND status='FOUND'")){p.setInt(1,id);p.setInt(2,finderId);if(p.executeUpdate()!=1)return false;}
        logs.logAction(finderId,"ITEM_DELETED","Deleted found-item record #"+id+" ("+name+").");return true;
    }
    public boolean adminDelete(int id,int adminId)throws SQLException{
        String name; try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT item_name FROM found_items WHERE id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return false;name=r.getString(1);}}
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM found_items WHERE id=?")){p.setInt(1,id);if(p.executeUpdate()!=1)return false;}
        logs.logAction(adminId,"ITEM_DELETED","Admin deleted found-item record #"+id+" ("+name+")."); return true;
    }
    /** Claims an available item and writes its audit event in the same database transaction. */
    public boolean claim(int id,int ownerId)throws SQLException{
        return updateAndAudit(
                "UPDATE found_items SET status='CLAIMED',owner_id=? WHERE id=? AND status='FOUND'",
                ownerId,id,"ITEM_CLAIMED","Owner claimed found-item record #"+id+".");
    }
    /** Confirms receipt and writes its audit event atomically. */
    public boolean receive(int id,int ownerId)throws SQLException{
        return updateAndAudit(
                "UPDATE found_items SET status='RETURNED' WHERE id=? AND owner_id=? AND status='CLAIMED'",
                ownerId,id,"ITEM_RECEIVED","Owner confirmed receipt of item record #"+id+".");
    }
    private boolean updateAndAudit(String updateSql,int ownerId,int itemId,String action,String description)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement(updateSql)){
                if("ITEM_CLAIMED".equals(action)){p.setInt(1,ownerId);p.setInt(2,itemId);}
                else {p.setInt(1,itemId);p.setInt(2,ownerId);}
                if(p.executeUpdate()!=1){c.rollback();return false;}
            }
            try(PreparedStatement log=c.prepareStatement(
                    "INSERT INTO activity_logs(user_id,action_type,description) VALUES(?,?,?)")){
                log.setInt(1,ownerId);log.setString(2,action);log.setString(3,description);log.executeUpdate();
            }
            c.commit();
            return true;
        }
    }
    public List<FoundItem> getClaimedByOwner(int ownerId)throws SQLException{return query("SELECT * FROM found_items WHERE owner_id=? AND status='CLAIMED' ORDER BY id DESC",ownerId);}
    private List<FoundItem> query(String sql,Integer id)throws SQLException{List<FoundItem>a=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(id!=null)p.setInt(1,id);try(ResultSet r=p.executeQuery()){while(r.next())a.add(map(r));}}return a;}
    private FoundItem map(ResultSet r)throws SQLException{int oid=r.getInt("owner_id");Integer owner=r.wasNull()?null:oid;return new FoundItem(r.getInt("id"),r.getString("item_name"),r.getString("description"),r.getString("location"),r.getString("date_found"),r.getInt("finder_id"),owner,r.getString("status"));}
}
