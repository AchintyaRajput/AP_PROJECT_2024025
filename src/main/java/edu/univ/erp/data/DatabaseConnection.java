package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseConnection {

    private static final String ERP_URL = "jdbc:mysql://localhost:3306/erp_db";
    private static final String AUTH_URL = "jdbc:mysql://localhost:3306/auth_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Avi@2006"; // your password

    public static Connection getERPConnection() throws Exception {
        return DriverManager.getConnection(ERP_URL, USER, PASSWORD);
    }


    public static Connection getAuthConnection() throws Exception {
        return DriverManager.getConnection(AUTH_URL, USER, PASSWORD);
    }

    public static boolean isMaintenanceOn() {
        try (Connection conn = getERPConnection()) {

            String sql = "SELECT `value` FROM settings WHERE `key` = 'maintenance'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("value").equalsIgnoreCase("true");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; // default if error
    }

    public static boolean setMaintenance(boolean enable) {
        try (Connection conn = getERPConnection()) {

            String sql = """
            UPDATE settings
            SET value = ?
            WHERE `key` = 'maintenance'
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, enable ? "true" : "false");

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void main(String[] args) {

        try (Connection conn = getERPConnection()) {
            System.out.println("✅ ERP DB connected successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = getAuthConnection()) {
            System.out.println("✅ Auth DB connected successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
