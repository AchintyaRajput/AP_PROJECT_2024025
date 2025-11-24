package edu.univ.erp.access;

import edu.univ.erp.data.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AccessControl {

    public static boolean isMaintenanceOn() {
        try (Connection conn = DatabaseConnection.getERPConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT `value` FROM settings WHERE `key` = 'maintenance'");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean canModifyData(String role) {
        if (isMaintenanceOn()) return false; // maintenance blocks modifications
        return role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Instructor");
    }
}
