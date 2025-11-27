package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

   
    public int createUser(String username, String plainPassword, String role,
                          String fullName, String email, String programOrDept, Integer year) {

        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        int newUserId = -1;

        
        String insertUserSql =
                "INSERT INTO auth_db.users (username, password_hash, role, status) " +
                        "VALUES (?, ?, ?, 'Active')";

        try (Connection authConn = DatabaseConnection.getAuthConnection();
             PreparedStatement ps = authConn.prepareStatement(insertUserSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, role);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("❌ Failed to insert user into auth_db.users");
                return -1;
            }

            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) newUserId = rs.getInt(1);
            }

            if (newUserId == -1) {
                System.err.println("❌ Could not retrieve generated user_id");
                return -1;
            }

        } catch (SQLIntegrityConstraintViolationException dup) {
            System.err.println("❌ Duplicate username: " + dup.getMessage());
            return -1;

        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }


        try (Connection erpConn = DatabaseConnection.getERPConnection()) {

            if ("Student".equalsIgnoreCase(role)) {

                String insertStudent =
                        "INSERT INTO students (student_id, name, program, year, email) " +
                                "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement sp = erpConn.prepareStatement(insertStudent)) {
                    sp.setInt(1, newUserId);
                    sp.setString(2, fullName != null ? fullName : "");
                    sp.setString(3, programOrDept != null ? programOrDept : "");

                    if (year != null) sp.setInt(4, year);
                    else sp.setNull(4, Types.INTEGER);

                    sp.setString(5, email != null ? email : "");
                    sp.executeUpdate();
                }
            }

            else if ("Instructor".equalsIgnoreCase(role)) {

                String insertInstructor =
                        "INSERT INTO instructors (instructor_id, name, department, email) " +
                                "VALUES (?, ?, ?, ?)";

                try (PreparedStatement sp = erpConn.prepareStatement(insertInstructor)) {
                    sp.setInt(1, newUserId);
                    sp.setString(2, fullName != null ? fullName : "");
                    sp.setString(3, programOrDept != null ? programOrDept : "");
                    sp.setString(4, email != null ? email : "");
                    sp.executeUpdate();
                }
            }

        } catch (Exception e) {
            System.err.println("⚠ ERP insert failed (but auth user created): " + e.getMessage());
        }

        return newUserId; /
    }


    public List<UserRow> getAllUsers() {
        List<UserRow> users = new ArrayList<>();

        String sql = "SELECT user_id, username, role, status FROM auth_db.users ORDER BY user_id ASC";

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new UserRow(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }


   
    public boolean deleteUser(int userId, String role) {

        try {
            try (Connection erp = DatabaseConnection.getERPConnection()) {

                if ("Student".equalsIgnoreCase(role)) {
                    PreparedStatement ps = erp.prepareStatement(
                            "DELETE FROM students WHERE student_id=?"
                    );
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                else if ("Instructor".equalsIgnoreCase(role)) {
                    PreparedStatement ps = erp.prepareStatement(
                            "DELETE FROM instructors WHERE instructor_id=?"
                    );
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            }

            try (Connection auth = DatabaseConnection.getAuthConnection()) {
                PreparedStatement ps = auth.prepareStatement(
                        "DELETE FROM users WHERE user_id=?"
                );
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public static class UserRow {
        public int id;
        public String username;
        public String role;
        public String status;

        public UserRow(int id, String username, String role, String status) {
            this.id = id;
            this.username = username;
            this.role = role;
            this.status = status;
        }
    }
}
