package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;     // For checking maintenance mode
import edu.univ.erp.data.DatabaseConnection;  // For connecting to MySQL

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegistrationService {

    /**
     * Registers a student into a section (class).
     *
     * @param studentId  ID of student (e.g. 101)
     * @param sectionId  ID of section (e.g. 501)
     * @param callerUserId  Who is performing the registration (e.g. admin ID)
     * @param callerRole  The role of the caller (Admin, Instructor, Student)
     * @return true if registration succeeded, false if full or duplicate
     */
    public boolean registerStudentToSection(int studentId, int sectionId, int callerUserId, String callerRole) throws Exception {

        // 🛑 Step 1: Block if maintenance mode is ON
        if (!AccessControl.canModifyData(callerRole)) {
            throw new IllegalStateException("Not allowed — system in maintenance or insufficient role");
        }

        // 🧠 Step 2: Connect to database
        try (Connection conn = DatabaseConnection.getERPConnection()) {

            // ✅ Check if already registered
            PreparedStatement dup = conn.prepareStatement(
                    "SELECT 1 FROM enrollments WHERE student_id=? AND section_id=?");
            dup.setInt(1, studentId);
            dup.setInt(2, sectionId);
            ResultSet r = dup.executeQuery();
            if (r.next()) return false; // Already enrolled

            // ✅ Check if section has space
            PreparedStatement cap = conn.prepareStatement(
                    "SELECT capacity, " +
                            "(SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.section_id AND e.status='Active') AS enrolled " +
                            "FROM sections s WHERE s.section_id = ?");
            cap.setInt(1, sectionId);
            ResultSet cr = cap.executeQuery();
            if (cr.next()) {
                int capacity = cr.getInt("capacity");
                int enrolled = cr.getInt("enrolled");
                if (enrolled >= capacity) return false; // full class
            } else {
                throw new IllegalArgumentException("Section not found");
            }

            // ✅ If not full and not duplicate → insert new enrollment
            PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'Active')");
            ins.setInt(1, studentId);
            ins.setInt(2, sectionId);
            ins.executeUpdate();

            return true;
        }
    }
}
