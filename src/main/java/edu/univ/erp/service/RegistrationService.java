package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;     
import edu.univ.erp.data.DatabaseConnection;  

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegistrationService {

    
    public boolean registerStudentToSection(int studentId, int sectionId, int callerUserId, String callerRole) throws Exception {

        
        if (!AccessControl.canModifyData(callerRole)) {
            throw new IllegalStateException("Not allowed — system in maintenance or insufficient role");
        }

       
        try (Connection conn = DatabaseConnection.getERPConnection()) {

            
            PreparedStatement dup = conn.prepareStatement(
                    "SELECT 1 FROM enrollments WHERE student_id=? AND section_id=?");
            dup.setInt(1, studentId);
            dup.setInt(2, sectionId);
            ResultSet r = dup.executeQuery();
            if (r.next()) return false; 

            
            PreparedStatement cap = conn.prepareStatement(
                    "SELECT capacity, " +
                            "(SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.section_id AND e.status='Active') AS enrolled " +
                            "FROM sections s WHERE s.section_id = ?");
            cap.setInt(1, sectionId);
            ResultSet cr = cap.executeQuery();
            if (cr.next()) {
                int capacity = cr.getInt("capacity");
                int enrolled = cr.getInt("enrolled");
                if (enrolled >= capacity) return false; 
            } else {
                throw new IllegalArgumentException("Section not found");
            }

            
            PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'Active')");
            ins.setInt(1, studentId);
            ins.setInt(2, sectionId);
            ins.executeUpdate();

            return true;
        }
    }
}
