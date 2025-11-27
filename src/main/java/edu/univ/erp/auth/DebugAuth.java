package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class DebugAuth {
    
    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "admin123";

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getAuthConnection()) {
            System.out.println("Connected to auth_db OK");
            String sql = "SELECT user_id, username, password_hash, role, status FROM users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, TEST_USERNAME);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("-> No such user: " + TEST_USERNAME);
                return;
            }
            int id = rs.getInt("user_id");
            String uname = rs.getString("username");
            String hash = rs.getString("password_hash");
            String role = rs.getString("role");
            String status = rs.getString("status");

            System.out.println("Found user_id=" + id + ", username=" + uname + ", role=" + role + ", status=" + status);
            System.out.println("Stored hash: " + hash);

            boolean matches = false;
            try {
                matches = BCrypt.checkpw(TEST_PASSWORD, hash);
            } catch (Exception e) {
                System.out.println("BCrypt.checkpw threw: " + e);
                e.printStackTrace();
            }
            System.out.println("BCrypt.checkpw(\"" + TEST_PASSWORD + "\", storedHash) => " + matches);

        } catch (Exception e) {
            System.out.println("Exception connecting/querying auth_db:");
            e.printStackTrace();
        }
    }
}
