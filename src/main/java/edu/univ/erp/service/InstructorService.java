package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;

import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorService {

    // ===== MODELS =====
    public static class SectionRow {
        public int sectionId;
        public String courseId;
        public String courseTitle;
        public String dayTime;
        public String room;
        public String semester;
        public int year;
    }

    public static class StudentRow {
        public int enrollmentId;
        public int studentId;
        public String studentName;
        public String email;
        public String finalGrade;
    }

    public static class GradeRow {
        public int gradeId;
        public String component;
        public double score;
        public double weight;
    }

    public static class StatsRow {
        public double average;
        public double median;
        public int passCount;
        public int failCount;
    }

    public static class TranscriptRow {
        public int studentId;
        public String studentName;
        public String courseId;
        public String courseTitle;
        public String semester;
        public int year;
        public String finalGrade;
        public String status;
    }

    // NEW MODEL FOR CSV EXPORT
    public static class GradeExportRow {
        public int studentId;
        public String studentName;
        public String email;
        public String component;
        public Double score;
        public Double weight;
        public String finalGrade;
    }

    // ============================================================
    // 1️⃣ GET SECTIONS
    // ============================================================
    public List<SectionRow> getInstructorSections(int instructorId) {
        List<SectionRow> list = new ArrayList<>();

        String sql = """
            SELECT s.section_id, s.course_id, c.title,
                   s.day_time, s.room, s.semester, s.year
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            WHERE s.instructor_id = ?
        """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SectionRow r = new SectionRow();
                r.sectionId = rs.getInt("section_id");
                r.courseId = rs.getString("course_id");
                r.courseTitle = rs.getString("title");
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

    // ============================================================
    // 2️⃣ STUDENTS FOR SECTION
    // ============================================================
    public List<StudentRow> getStudentsForSection(int sectionId) {
        List<StudentRow> list = new ArrayList<>();

        String sql = """
            SELECT e.enrollment_id, st.student_id, st.name, st.email, e.final_grade
            FROM enrollments e
            JOIN students st ON e.student_id = st.student_id
            WHERE e.section_id = ?
        """;

        try (var conn = DatabaseConnection.getERPConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            var rs = ps.executeQuery();

            while (rs.next()) {
                StudentRow r = new StudentRow();
                r.enrollmentId = rs.getInt("enrollment_id");
                r.studentId = rs.getInt("student_id");
                r.studentName = rs.getString("name");
                r.email = rs.getString("email");
                r.finalGrade = rs.getString("final_grade");
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ============================================================
    // 3️⃣ GET GRADES
    // ============================================================
    public List<GradeRow> getGradesForEnrollment(int enrollmentId) {
        List<GradeRow> list = new ArrayList<>();

        String sql = "SELECT grade_id, component, score, weight FROM grades WHERE enrollment_id = ?";

        try (var conn = DatabaseConnection.getERPConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            var rs = ps.executeQuery();

            while (rs.next()) {
                GradeRow g = new GradeRow();
                g.gradeId = rs.getInt("grade_id");
                g.component = rs.getString("component");
                g.score = rs.getDouble("score");
                g.weight = rs.getDouble("weight");
                list.add(g);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ============================================================
    // 4️⃣ SAVE GRADE
    // ============================================================
    public boolean saveGrade(int enrollmentId, String component, double score, double weight) {
        try (var conn = DatabaseConnection.getERPConnection()) {

            String check = "SELECT grade_id FROM grades WHERE enrollment_id=? AND component=?";
            var ps = conn.prepareStatement(check);
            ps.setInt(1, enrollmentId);
            ps.setString(2, component);
            var rs = ps.executeQuery();

            if (rs.next()) {
                String update = "UPDATE grades SET score=?, weight=? WHERE grade_id=?";
                var up = conn.prepareStatement(update);
                up.setDouble(1, score);
                up.setDouble(2, weight);
                up.setInt(3, rs.getInt(1));
                up.executeUpdate();
            } else {
                String insert = "INSERT INTO grades (enrollment_id, component, score, weight) VALUES (?,?,?,?)";
                var in = conn.prepareStatement(insert);
                in.setInt(1, enrollmentId);
                in.setString(2, component);
                in.setDouble(3, score);
                in.setDouble(4, weight);
                in.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // 5️⃣ CLASS STATS
    // ============================================================
    public StatsRow getSectionStats(int sectionId) {
        StatsRow stats = new StatsRow();

        try (var conn = DatabaseConnection.getERPConnection()) {

            String sql = """
                SELECT AVG(final_grade),
                       SUM(CASE WHEN final_grade>=30 THEN 1 ELSE 0 END),
                       SUM(CASE WHEN final_grade<30 THEN 1 ELSE 0 END)
                FROM enrollments WHERE section_id=? AND final_grade IS NOT NULL
            """;

            var ps = conn.prepareStatement(sql);
            ps.setInt(1, sectionId);
            var rs = ps.executeQuery();

            if (rs.next()) {
                stats.average = rs.getDouble(1);
                stats.passCount = rs.getInt(2);
                stats.failCount = rs.getInt(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    // ============================================================
    // 6️⃣ FIXED TRANSCRIPT EXPORT
    // ============================================================
    public List<TranscriptRow> getSectionTranscript(int sectionId) {

        List<TranscriptRow> list = new ArrayList<>();

        String sql = """
            SELECT 
                st.student_id, st.name,
                c.course_id, c.title,
                s.semester, s.year,
                e.final_grade, e.status
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_id = c.course_id
            JOIN students st ON e.student_id = st.student_id
            WHERE e.section_id = ?
        """;

        try (var conn = DatabaseConnection.getERPConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            var rs = ps.executeQuery();

            while (rs.next()) {
                TranscriptRow r = new TranscriptRow();
                r.studentId = rs.getInt("student_id");
                r.studentName = rs.getString("name");
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

    // ============================================================
    // 7️⃣ NEW: GET CSV EXPORT DATA
    // ============================================================
    public List<GradeExportRow> getGradesForExport(int sectionId) {
        List<GradeExportRow> out = new ArrayList<>();

        String sql = """
            SELECT st.student_id, st.name AS student_name, st.email,
                   g.component, g.score, g.weight, e.final_grade
            FROM enrollments e
            JOIN students st ON e.student_id = st.student_id
            LEFT JOIN grades g ON g.enrollment_id = e.enrollment_id
            WHERE e.section_id = ? AND e.status != 'Dropped'
            ORDER BY st.student_id, g.component
        """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GradeExportRow r = new GradeExportRow();
                r.studentId = rs.getInt("student_id");
                r.studentName = rs.getString("student_name");
                r.email = rs.getString("email");
                r.component = rs.getString("component");
                r.score = (Double) rs.getObject("score");
                r.weight = (Double) rs.getObject("weight");
                r.finalGrade = rs.getString("final_grade");
                out.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    // ============================================================
    // 8️⃣ NEW: EXPORT CSV
    // ============================================================
    public boolean exportGradesToCSV(File file, List<GradeExportRow> rows) {
        try (PrintWriter pw = new PrintWriter(file)) {

            pw.println("Student ID,Student Name,Email,Component,Score,Weight,Final Grade");

            for (GradeExportRow r : rows) {
                pw.printf("%d,%s,%s,%s,%s,%s,%s%n",
                        r.studentId,
                        r.studentName,
                        r.email,
                        r.component == null ? "" : r.component,
                        r.score == null ? "" : r.score,
                        r.weight == null ? "" : r.weight,
                        r.finalGrade == null ? "" : r.finalGrade
                );
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
