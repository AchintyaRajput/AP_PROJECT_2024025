package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentService {

    // ====== DATA MODELS ======
    public static class SectionRow {
        public int sectionId;
        public String courseId;
        public String courseTitle;
        public String instructorName;
        public String dayTime;
        public String room;
        public int availableCapacity;
        public String semester;
        public int year;
    }

    public static class EnrollmentRow {
        public int sectionId;
        public String courseId;
        public String courseTitle;
        public String instructorName;
        public String dayTime;
        public String room;
        public String semester;
        public int year;
    }

    public static class GradeRow {
        public String courseId;
        public String courseTitle;
        public String component;
        public double score;
        public double maxMarks;   // NEW FIELD
        public double weight;
    }

    public static class TranscriptRow {
        public String courseId;
        public String courseTitle;
        public String semester;
        public int year;
        public String finalGrade;
        public String status;
    }

    private final NotificationService notificationService = new NotificationService();

    // ==========================================================
    // GET REAL STUDENT NAME FROM ERP DB
    // ==========================================================
    public String getStudentName(int studentId) {
        String sql = "SELECT name FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString("name");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // ==========================================================
    // 1️⃣ GET ALL AVAILABLE SECTIONS
    // ==========================================================
    public List<SectionRow> getAvailableSections() {
        List<SectionRow> list = new ArrayList<>();

        String sql = """
                SELECT s.section_id, s.course_id, c.title,
                       i.name AS instructor_name,
                       s.day_time, s.room,
                       s.available_capacity,
                       s.semester, s.year
                FROM sections s
                JOIN courses c ON s.course_id = c.course_id
                LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
                ORDER BY c.course_id, s.section_id
                """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SectionRow r = new SectionRow();
                r.sectionId = rs.getInt("section_id");
                r.courseId = rs.getString("course_id");
                r.courseTitle = rs.getString("title");
                r.instructorName = rs.getString("instructor_name");
                r.dayTime = rs.getString("day_time");
                r.room = rs.getString("room");
                r.availableCapacity = rs.getInt("available_capacity");
                r.semester = rs.getString("semester");
                r.year = rs.getInt("year");

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    // ==========================================================
    // 2️⃣ GET STUDENT ENROLLMENTS
    // ==========================================================
    public List<EnrollmentRow> getStudentEnrollments(int studentId) {
        List<EnrollmentRow> list = new ArrayList<>();

        String sql = """
                SELECT e.section_id, s.course_id, c.title,
                       i.name AS instructor_name,
                       s.day_time, s.room,
                       s.semester, s.year
                FROM enrollments e
                JOIN sections s ON e.section_id = s.section_id
                JOIN courses c ON s.course_id = c.course_id
                LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
                WHERE e.student_id = ?
                ORDER BY s.year DESC, s.semester DESC
                """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                EnrollmentRow r = new EnrollmentRow();
                r.sectionId = rs.getInt("section_id");
                r.courseId = rs.getString("course_id");
                r.courseTitle = rs.getString("title");
                r.instructorName = rs.getString("instructor_name");
                r.dayTime = rs.getString("day_time");
                r.room = rs.getString("room");
                r.semester = rs.getString("semester");
                r.year = rs.getInt("year");

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ==========================================================
    // 3️⃣ REGISTER FOR A SECTION
    // ==========================================================
    public String registerForSection(int studentId, int sectionId) {

        try (Connection conn = DatabaseConnection.getERPConnection()) {

            // Fetch section info
            String sql = """
                SELECT s.course_id, s.available_capacity, s.instructor_id, c.title
                FROM sections s
                JOIN courses c ON s.course_id = c.course_id
                WHERE s.section_id = ?
                """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return "Section does not exist.";

            String courseId = rs.getString("course_id");
            int available = rs.getInt("available_capacity");
            int instructorId = rs.getInt("instructor_id");
            String courseTitle = rs.getString("title");

            // Fetch student name
            String studentName = "";
            PreparedStatement ps2 = conn.prepareStatement("SELECT name FROM students WHERE student_id = ?");
            ps2.setInt(1, studentId);
            ResultSet rsStu = ps2.executeQuery();
            if (rsStu.next()) studentName = rsStu.getString("name");

            // No capacity
            if (available <= 0) return "No seats available.";

            // Prevent duplicate course enrollment
            String sqlDup = """
                SELECT COUNT(*)
                FROM enrollments e
                JOIN sections s ON e.section_id = s.section_id
                WHERE e.student_id = ? AND s.course_id = ?
                """;

            PreparedStatement pd = conn.prepareStatement(sqlDup);
            pd.setInt(1, studentId);
            pd.setString(2, courseId);
            ResultSet rsDup = pd.executeQuery();
            rsDup.next();

            if (rsDup.getInt(1) > 0)
                return "You are already enrolled in another section of this course.";

            // Insert enrollment
            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'Active')");
            insert.setInt(1, studentId);
            insert.setInt(2, sectionId);
            insert.executeUpdate();

            // Reduce capacity
            PreparedStatement upd = conn.prepareStatement(
                    "UPDATE sections SET available_capacity = available_capacity - 1 WHERE section_id = ?");
            upd.setInt(1, sectionId);
            upd.executeUpdate();

            // Notifications
            notificationService.createNotification(
                    studentId, null,
                    "You enrolled in " + courseId + " - " + courseTitle + " (Section " + sectionId + ")",
                    "section:" + sectionId
            );

            if (instructorId > 0) {
                notificationService.createNotification(
                        instructorId, null,
                        "Student " + studentName + " enrolled in your section " + courseId + " (ID " + sectionId + ")",
                        "section:" + sectionId
                );
            }

            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }


    // ==========================================================
    // 4️⃣ DROP COURSE
    // ==========================================================
    public boolean dropCourse(int studentId, int sectionId) {

        try (Connection conn = DatabaseConnection.getERPConnection()) {

            // Get info for notification
            String sql = """
                SELECT s.course_id, c.title, s.instructor_id, st.name AS student_name
                FROM sections s
                JOIN courses c ON s.course_id = c.course_id
                JOIN students st ON st.student_id = ?
                WHERE s.section_id = ?
                """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            ResultSet rs = ps.executeQuery();

            String courseId = null, courseTitle = null, studentName = null;
            int instructorId = 0;

            if (rs.next()) {
                courseId = rs.getString("course_id");
                courseTitle = rs.getString("title");
                instructorId = rs.getInt("instructor_id");
                studentName = rs.getString("student_name");
            }

            // Delete enrollment
            PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM enrollments WHERE student_id = ? AND section_id = ?");
            del.setInt(1, studentId);
            del.setInt(2, sectionId);

            boolean removed = del.executeUpdate() > 0;
            if (!removed) return false;

            // Increase capacity
            PreparedStatement upd = conn.prepareStatement(
                    "UPDATE sections SET available_capacity = available_capacity + 1 WHERE section_id = ?");
            upd.setInt(1, sectionId);
            upd.executeUpdate();

            // Notifications
            notificationService.createNotification(
                    studentId, null,
                    "You dropped " + courseId + " - " + courseTitle + " (Section " + sectionId + ")",
                    "section:" + sectionId
            );

            if (instructorId > 0) {
                notificationService.createNotification(
                        instructorId, null,
                        "Student " + studentName + " dropped your section " + courseId,
                        "section:" + sectionId
                );
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // ==========================================================
    // 5️⃣ GET STUDENT GRADES (NOW RETURNS maxMarks)
    // ==========================================================
    public List<GradeRow> getGrades(int studentId) {
        List<GradeRow> list = new ArrayList<>();

        String sql = """
                SELECT c.course_id, c.title,
                       g.component, g.score, g.max_marks, g.weight
                FROM enrollments e
                JOIN sections s ON e.section_id = s.section_id
                JOIN courses c ON s.course_id = c.course_id
                JOIN grades g ON g.enrollment_id = e.enrollment_id
                WHERE e.student_id = ?
                """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GradeRow r = new GradeRow();
                r.courseId = rs.getString("course_id");
                r.courseTitle = rs.getString("title");
                r.component = rs.getString("component");
                r.score = rs.getDouble("score");
                r.maxMarks = rs.getDouble("max_marks");  // NEW
                r.weight = rs.getDouble("weight");

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ==========================================================
    // 6️⃣ GET TRANSCRIPT DATA
    // ==========================================================
    public List<TranscriptRow> getTranscriptRows(int studentId) {

        List<TranscriptRow> list = new ArrayList<>();

        String sql = """
                SELECT c.course_id, c.title,
                       s.semester, s.year,
                       e.final_grade, e.status
                FROM enrollments e
                JOIN sections s ON e.section_id = s.section_id
                JOIN courses c ON s.course_id = c.course_id
                WHERE e.student_id = ?
                ORDER BY s.year DESC, s.semester DESC
                """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TranscriptRow r = new TranscriptRow();
                r.courseId = rs.getString("course_id");
                r.courseTitle = rs.getString("title");
                r.semester = rs.getString("semester");
                r.year = rs.getInt("year");
                r.finalGrade = rs.getString("final_grade");
                r.status = rs.getString("status");

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
