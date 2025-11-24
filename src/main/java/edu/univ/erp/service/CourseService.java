package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseService - handles CRUD operations for courses.
 */
public class CourseService {

    // ---------------- GET ALL COURSES ----------------
    public List<CourseRow> getAllCourses() {
        List<CourseRow> list = new ArrayList<>();

        String sql = "SELECT course_id, title, credits FROM courses ORDER BY course_id ASC";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CourseRow(
                        rs.getString("course_id"),
                        rs.getString("title"),
                        rs.getInt("credits")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ---------------- ADD COURSE ----------------
    public boolean addCourse(String courseId, String title, int credits) {
        String sql = "INSERT INTO courses (course_id, title, credits) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, courseId);
            ps.setString(2, title);
            ps.setInt(3, credits);

            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException dup) {
            System.err.println("Duplicate course ID");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- UPDATE COURSE ----------------
    public boolean updateCourse(String courseId, String newTitle, int newCredits) {
        String sql = "UPDATE courses SET title = ?, credits = ? WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newTitle);
            ps.setInt(2, newCredits);
            ps.setString(3, courseId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- DELETE COURSE ----------------
    public boolean deleteCourse(String courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, courseId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Cannot delete course: maybe linked to a section.");
            return false;
        }
    }

    // ---------------- Helper Class ----------------
    public static class CourseRow {
        public String id;
        public String title;
        public int credits;

        public CourseRow(String id, String title, int credits) {
            this.id = id;
            this.title = title;
            this.credits = credits;
        }
    }
}
