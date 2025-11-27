package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class InstructorService {

  
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
    }

    public static class GradeRow {
        public int gradeId;
        public String component;
        public double score;
        public double weight;
        public double maxMarks;
    }

    public static class GradeExportRow {
        public int studentId;
        public String studentName;
        public String component;
        public double score;
        public double maxMarks;
        public double weight;
        public double weightedScore;
    }

    public static class StatsRow {
        public double average;
        public double median;
        public int passCount;
        public int failCount;

        public StatsRow(double avg, double med, int pass, int fail) {
            this.average = avg;
            this.median = med;
            this.passCount = pass;
            this.failCount = fail;
        }
    }


    
    
    public List<SectionRow> getInstructorSections(int instructorId) {
        List<SectionRow> list = new ArrayList<>();

        String sql = """
            SELECT s.section_id, s.course_id, c.title,
                   s.day_time, s.room, s.semester, s.year
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            WHERE s.instructor_id = ?
            ORDER BY s.year DESC, s.semester DESC
            """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SectionRow s = new SectionRow();
                s.sectionId = rs.getInt("section_id");
                s.courseId = rs.getString("course_id");
                s.courseTitle = rs.getString("title");
                s.dayTime = rs.getString("day_time");
                s.room = rs.getString("room");
                s.semester = rs.getString("semester");
                s.year = rs.getInt("year");
                list.add(s);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }


    public List<StudentRow> getStudentsForSection(int sectionId) {
        List<StudentRow> list = new ArrayList<>();

        String sql = """
            SELECT e.enrollment_id, st.student_id, st.name, st.email
            FROM enrollments e
            JOIN students st ON e.student_id = st.student_id
            WHERE e.section_id = ?
            """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StudentRow s = new StudentRow();
                s.enrollmentId = rs.getInt("enrollment_id");
                s.studentId = rs.getInt("student_id");
                s.studentName = rs.getString("name");
                s.email = rs.getString("email");
                list.add(s);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }


    
    public List<GradeRow> getGradesForEnrollment(int enrollmentId) {
        List<GradeRow> list = new ArrayList<>();

        String sql = """
            SELECT grade_id, component, score, weight, max_marks
            FROM grades
            WHERE enrollment_id = ?
            """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GradeRow g = new GradeRow();
                g.gradeId = rs.getInt("grade_id");
                g.component = rs.getString("component");
                g.score = rs.getDouble("score");
                g.weight = rs.getDouble("weight");
                g.maxMarks = rs.getDouble("max_marks");
                list.add(g);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }


   
    public boolean saveGrade(int enrollmentId, String component,
                             double score, double weight, double maxMarks) {

        String checkSql = """
            SELECT COUNT(*) FROM grades
            WHERE enrollment_id = ? AND component = ?
            """;

        String insertSql = """
            INSERT INTO grades (enrollment_id, component, score, weight, max_marks)
            VALUES (?, ?, ?, ?, ?)
            """;

        String updateSql = """
            UPDATE grades
            SET score = ?, weight = ?, max_marks = ?
            WHERE enrollment_id = ? AND component = ?
            """;

        try (Connection conn = DatabaseConnection.getERPConnection()) {

            PreparedStatement pc = conn.prepareStatement(checkSql);
            pc.setInt(1, enrollmentId);
            pc.setString(2, component);
            ResultSet rs = pc.executeQuery();
            rs.next();

            boolean exists = (rs.getInt(1) > 0);

            if (!exists) {
                PreparedStatement ps = conn.prepareStatement(insertSql);
                ps.setInt(1, enrollmentId);
                ps.setString(2, component);
                ps.setDouble(3, score);
                ps.setDouble(4, weight);
                ps.setDouble(5, maxMarks);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(updateSql);
                ps.setDouble(1, score);
                ps.setDouble(2, weight);
                ps.setDouble(3, maxMarks);
                ps.setInt(4, enrollmentId);
                ps.setString(5, component);
                ps.executeUpdate();
            }

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }



    public List<GradeExportRow> getGradesForExport(int sectionId) {

        List<GradeExportRow> list = new ArrayList<>();

        String sql = """
            SELECT st.student_id,
                   st.name AS student_name,
                   g.component,
                   g.score,
                   g.max_marks,
                   g.weight
            FROM enrollments e
            JOIN students st ON e.student_id = st.student_id
            JOIN grades g ON g.enrollment_id = e.enrollment_id
            WHERE e.section_id = ?
            ORDER BY st.student_id
            """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                GradeExportRow r = new GradeExportRow();
                r.studentId = rs.getInt("student_id");
                r.studentName = rs.getString("student_name");
                r.component = rs.getString("component");
                r.score = rs.getDouble("score");
                r.maxMarks = rs.getDouble("max_marks");
                r.weight = rs.getDouble("weight");

                if (r.maxMarks > 0)
                    r.weightedScore = (r.score / r.maxMarks) * r.weight;
                else
                    r.weightedScore = 0;

                list.add(r);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }


    public boolean calculateFinalGrade(int enrollmentId) {

        String sql = """
            SELECT score, weight, max_marks
            FROM grades
            WHERE enrollment_id = ?
            """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            ResultSet rs = ps.executeQuery();

            double total = 0;

            while (rs.next()) {
                double score = rs.getDouble("score");
                double weight = rs.getDouble("weight");
                double maxMarks = rs.getDouble("max_marks");

                if (maxMarks > 0)
                    total += (score / maxMarks) * weight;
            }

            PreparedStatement update = conn.prepareStatement("""
                UPDATE enrollments
                SET final_grade = ?
                WHERE enrollment_id = ?
            """);

            update.setDouble(1, total);
            update.setInt(2, enrollmentId);
            update.executeUpdate();

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


   
    public StatsRow getSectionStats(int sectionId) {

        List<Double> grades = new ArrayList<>();

        String sql = """
            SELECT final_grade
            FROM enrollments
            WHERE section_id = ?
            AND final_grade IS NOT NULL
            """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                grades.add(rs.getDouble("final_grade"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        if (grades.isEmpty())
            return new StatsRow(0, 0, 0, 0);

        // compute average
        double sum = 0;
        for (double g : grades) sum += g;
        double avg = sum / grades.size();

        // compute median
        Collections.sort(grades);
        double median;
        int n = grades.size();

        if (n % 2 == 1)
            median = grades.get(n / 2);
        else
            median = (grades.get(n / 2 - 1) + grades.get(n / 2)) / 2.0;

        int pass = 0, fail = 0;
        for (double g : grades) {
            if (g >= 40) pass++;
            else fail++;
        }

        return new StatsRow(avg, median, pass, fail);
    }
}
