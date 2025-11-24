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

    // ===============================
    // 1️⃣ GET ALL AVAILABLE SECTIONS
    // ===============================
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


    // ==========================================
    // 2️⃣ GET STUDENT'S REGISTERED ENROLLMENTS
    // ==========================================
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


    // ==================================
    // 3️⃣ REGISTER FOR A SECTION
    // ==================================
    public String registerForSection(int studentId, int sectionId) {

        try (Connection conn = DatabaseConnection.getERPConnection()) {

            // A) Get course, instructor, available capacity
            String sql = """
                SELECT s.course_id, s.available_capacity, s.instructor_id,
                       c.title,
                       st.name AS student_name
                FROM sections s
                JOIN courses c ON s.course_id = c.course_id
                JOIN students st ON st.student_id = ?
                WHERE s.section_id = ?
                """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return "Section does not exist.";

            String courseId = rs.getString("course_id");
            int available = rs.getInt("available_capacity");
            int instructorId = rs.getInt("instructor_id");
            String courseTitle = rs.getString("title");
            String studentName = rs.getString("student_name");

            // B) Check capacity
            if (available <= 0) return "No seats available in this section.";

            // C) Prevent duplicate course enrollment
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

            // D) Insert enrollment
            String insert = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'Active')";
            PreparedStatement pi = conn.prepareStatement(insert);
            pi.setInt(1, studentId);
            pi.setInt(2, sectionId);
            pi.executeUpdate();

            // E) Reduce available capacity
            String updateCap = "UPDATE sections SET available_capacity = available_capacity - 1 WHERE section_id = ?";
            PreparedStatement pu = conn.prepareStatement(updateCap);
            pu.setInt(1, sectionId);
            pu.executeUpdate();

            // ==============================
            // 🔔 NOTIFICATIONS
            // ==============================
            // 1. Notify student
            notificationService.createNotification(
                    studentId,
                    null,
                    "You enrolled in " + courseId + " - " + courseTitle + " (Section " + sectionId + ")",
                    "section:" + sectionId
            );

            // 2. Notify instructor (if section has instructor)
            if (instructorId > 0) {
                notificationService.createNotification(
                        instructorId,
                        null,
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


    // ==========================
    // 4️⃣ DROP A COURSE / SECTION
    // ==========================
    public boolean dropCourse(int studentId, int sectionId) {

        try (Connection conn = DatabaseConnection.getERPConnection()) {

            // A) Get course and instructor for notification
            String sqlInfo = """
                SELECT s.course_id, c.title, s.instructor_id, st.name AS student_name
                FROM sections s
                JOIN courses c ON s.course_id = c.course_id
                JOIN students st ON st.student_id = ?
                WHERE s.section_id = ?
                """;

            PreparedStatement info = conn.prepareStatement(sqlInfo);
            info.setInt(1, studentId);
            info.setInt(2, sectionId);
            ResultSet infoRs = info.executeQuery();

            String courseId = null, courseTitle = null, studentName = null;
            int instructorId = 0;

            if (infoRs.next()) {
                courseId = infoRs.getString("course_id");
                courseTitle = infoRs.getString("title");
                instructorId = infoRs.getInt("instructor_id");
                studentName = infoRs.getString("student_name");
            }

            // B) Delete enrollment
            String sql = "DELETE FROM enrollments WHERE student_id = ? AND section_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);

            boolean removed = ps.executeUpdate() > 0;
            if (!removed) return false;

            // C) Increase seats
            String update = "UPDATE sections SET available_capacity = available_capacity + 1 WHERE section_id = ?";
            PreparedStatement pu = conn.prepareStatement(update);
            pu.setInt(1, sectionId);
            pu.executeUpdate();

            // ==============================
            // 🔔 NOTIFICATIONS
            // ==============================
            // 1. Notify student
            notificationService.createNotification(
                    studentId,
                    null,
                    "You dropped " + courseId + " - " + courseTitle + " (Section " + sectionId + ")",
                    "section:" + sectionId
            );

            // 2. Notify instructor
            if (instructorId > 0) {
                notificationService.createNotification(
                        instructorId,
                        null,
                        "Student " + studentName + " dropped your section " + courseId + " (ID " + sectionId + ")",
                        "section:" + sectionId
                );
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // ======================
    // 5️⃣ GET STUDENT GRADES
    // ======================
    public List<GradeRow> getGrades(int studentId) {
        List<GradeRow> list = new ArrayList<>();

        String sql = """
                SELECT c.course_id, c.title,
                       g.component, g.score, g.weight
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
                r.weight = rs.getDouble("weight");
                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ============================================
    // 6️⃣ GET TRANSCRIPT DATA FOR CSV EXPORT
    // ============================================
    public List<TranscriptRow> getTranscriptRows(int studentId) {

        List<TranscriptRow> list = new ArrayList<>();

        String sql = """
                SELECT c.course_id, c.title,
                       s.semester, s.year,
                       e.final_grade,
                       e.status
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
