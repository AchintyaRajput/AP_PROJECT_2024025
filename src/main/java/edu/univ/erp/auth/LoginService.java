package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class LoginService {

   
    private static final Map<String, Integer> failedAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    
    public User login(String username, String password) {

        if (failedAttempts.getOrDefault(username, 0) >= MAX_ATTEMPTS) {
            System.out.println("❌ Account locked due to too many failed attempts.");
            return null;
        }

        try (Connection conn = DatabaseConnection.getAuthConnection()) {

            String query = "SELECT user_id, username, password_hash, role, status FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                if (BCrypt.checkpw(password, storedHash)) {
                    System.out.println("✅ Login successful for " + username);

                  
                    failedAttempts.remove(username);

                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                } else {
                    System.out.println("❌ Incorrect password");
                    increaseFail(username);
                    return null;
                }
            } else {
                System.out.println("❌ User not found");
                increaseFail(username);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    
    private void increaseFail(String username) {
        int count = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, count);
        System.out.println("⚠ Failed attempts for " + username + ": " + count);
    }

    public static boolean isBlocked(String username) {
        return failedAttempts.getOrDefault(username, 0) >= MAX_ATTEMPTS;
    }

    public static int getFailCount(String username) {
        return failedAttempts.getOrDefault(username, 0);
    }


    
    public boolean verifyPassword(int userId, String oldPassword) {
        try (Connection conn = DatabaseConnection.getAuthConnection()) {

            String sql = "SELECT password_hash FROM users WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return BCrypt.checkpw(oldPassword, storedHash);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updatePassword(int userId, String newPassword) {
        try (Connection conn = DatabaseConnection.getAuthConnection()) {

            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newHash);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

 
    public String getHashByUserId(int userId) {
        try (Connection conn = DatabaseConnection.getAuthConnection()) {

            String sql = "SELECT password_hash FROM users WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString("password_hash");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    
    public void registerUser(String username, String password, String role) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection conn = DatabaseConnection.getAuthConnection()) {
            String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, role);
            ps.executeUpdate();
            System.out.println("✅ User registered: " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
