package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class NotificationService {

    public static class NotificationRow {
        public int id;
        public Integer userId;   
        public String role;      
        public String message;
        public String link;
        public boolean isRead;
        public Timestamp createdAt;
    }

  
    public boolean createNotification(Integer userId, String role, String message, String link) {
        String sql = "INSERT INTO notifications (user_id, role, message, link) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, userId);
            if (role == null) ps.setNull(2, Types.VARCHAR); else ps.setString(2, role);
            ps.setString(3, message);
            if (link == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, link);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

   
    public List<NotificationRow> getNotificationsForUser(int userId, String role) {
        List<NotificationRow> out = new ArrayList<>();

        String sql = """
            SELECT notification_id, user_id, role, message, link, is_read, created_at
            FROM notifications
            WHERE (user_id = ? OR role = ?)
            ORDER BY is_read ASC, created_at DESC
        """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NotificationRow r = new NotificationRow();
                r.id = rs.getInt("notification_id");
                int uid = rs.getInt("user_id");
                r.userId = rs.wasNull() ? null : uid;
                r.role = rs.getString("role");
                r.message = rs.getString("message");
                r.link = rs.getString("link");
                r.isRead = rs.getInt("is_read") == 1;
                r.createdAt = rs.getTimestamp("created_at");
                out.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

   
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

   
    public boolean markAllAsRead(int userId, String role) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE (user_id = ? OR role = ?)";
        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, role);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
