package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class SectionService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] VALID_DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri"};

    private final NotificationService notificationService = new NotificationService();

    
    private String normalizeDayTime(String dayTimeRaw) throws Exception {
        if (dayTimeRaw == null || !dayTimeRaw.contains(" "))
            throw new Exception("Invalid day/time format");

        String[] parts = dayTimeRaw.split(" ", 2);
        String day = parts[0].trim();
        String time = parts[1].trim();

        boolean dayOK = false;
        for (String d : VALID_DAYS) {
            if (d.equals(day)) {
                dayOK = true;
                break;
            }
        }
        if (!dayOK) throw new Exception("Invalid day: must be Mon–Fri");

        if (!time.contains("-")) throw new Exception("Invalid time range");

        String[] tr = time.split("-", 2);
        LocalTime start = parseLenient(tr[0].trim());
        LocalTime end = parseLenient(tr[1].trim());

        if (!end.isAfter(start))
            throw new Exception("End time must be after start time");

        return day + " " + start.format(TF) + "-" + end.format(TF);
    }

   
    private LocalTime parseLenient(String s) throws Exception {
        try {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception ex) {
            return LocalTime.parse(s, TF);
        }
    }

    
    public List<SectionRow> getAllSections() {
        List<SectionRow> list = new ArrayList<>();

        String sql = """
            SELECT s.section_id, s.course_id, c.title AS course_title,
                   s.instructor_id, i.name AS instructor_name,
                   s.day_time, s.room, s.capacity, s.available_capacity,
                   s.semester, s.year
            FROM sections s
            LEFT JOIN courses c ON s.course_id = c.course_id
            LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
            ORDER BY s.section_id ASC
        """;

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new SectionRow(
                        rs.getInt("section_id"),
                        rs.getString("course_id"),
                        rs.getString("course_title"),
                        rs.getInt("instructor_id"),
                        rs.getString("instructor_name"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getInt("available_capacity"),
                        rs.getString("semester"),
                        rs.getInt("year")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    
    public boolean addSection(String courseId, int instructorId, String dayTime,
                              String room, int capacity, String semester, int year) {
        try {
            String normalized = normalizeDayTime(dayTime);

            
            String courseTitle = fetchCourseTitle(courseId);

            String sql = """
                INSERT INTO sections 
                (course_id, instructor_id, day_time, room, capacity, available_capacity, semester, year)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (Connection conn = DatabaseConnection.getERPConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, courseId);
                ps.setInt(2, instructorId);
                ps.setString(3, normalized);
                ps.setString(4, room);
                ps.setInt(5, capacity);
                ps.setInt(6, capacity);
                ps.setString(7, semester);
                ps.setInt(8, year);

                ps.executeUpdate();

                
                ResultSet keys = ps.getGeneratedKeys();
                int newSectionId = -1;
                if (keys.next()) newSectionId = keys.getInt(1);

                
                if (instructorId > 0) {
                    notificationService.createNotification(
                            instructorId,
                            null,
                            "You were assigned to teach " + courseId + " - " + courseTitle +
                                    " (Section " + newSectionId + ")",
                            "section:" + newSectionId
                    );
                }

                return true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateSection(int sectionId, String courseId, int newInstructorId, String dayTime,
                                 String room, int capacity, String semester, int year) {
        try {
            String normalized = normalizeDayTime(dayTime);

            
            String oldInstructorSql = """
                SELECT instructor_id, c.title
                FROM sections s
                JOIN courses c ON s.course_id = c.course_id
                WHERE s.section_id = ?
            """;

            int oldInstructorId = 0;
            String courseTitle = "";

            try (Connection conn = DatabaseConnection.getERPConnection();
                 PreparedStatement psOld = conn.prepareStatement(oldInstructorSql)) {

                psOld.setInt(1, sectionId);
                ResultSet rsOld = psOld.executeQuery();

                if (rsOld.next()) {
                    oldInstructorId = rsOld.getInt("instructor_id");
                    courseTitle = rsOld.getString("title");
                }
            }

          
            String sql = """
                UPDATE sections
                SET course_id=?, instructor_id=?, day_time=?, room=?, 
                    capacity=?, semester=?, year=?
                WHERE section_id=?
            """;

            try (Connection conn = DatabaseConnection.getERPConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, courseId);
                ps.setInt(2, newInstructorId);
                ps.setString(3, normalized);
                ps.setString(4, room);
                ps.setInt(5, capacity);
                ps.setString(6, semester);
                ps.setInt(7, year);
                ps.setInt(8, sectionId);

                boolean ok = ps.executeUpdate() > 0;

                if (!ok) return false;
            }

           
            if (newInstructorId > 0 && newInstructorId != oldInstructorId) {
                notificationService.createNotification(
                        newInstructorId,
                        null,
                        "You are now assigned to teach " + courseId + " - " + courseTitle +
                                " (Section " + sectionId + ")",
                        "section:" + sectionId
                );
            }

            
            if (oldInstructorId > 0 && oldInstructorId != newInstructorId) {
                notificationService.createNotification(
                        oldInstructorId,
                        null,
                        "You are no longer assigned to Section " + sectionId,
                        "section:" + sectionId
                );
            }

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    
    public boolean deleteSection(int sectionId) {
        try (Connection conn = DatabaseConnection.getERPConnection()) {

            
            String checkSql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, sectionId);
            ResultSet rs = psCheck.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                System.err.println("❌ Cannot delete section. Students enrolled.");
                return false;
            }

            String sql = "DELETE FROM sections WHERE section_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sectionId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
    private String fetchCourseTitle(String courseId) {
        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT title FROM courses WHERE course_id = ?"
             )) {

            ps.setString(1, courseId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("title");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    
    public static class SectionRow {
        public int id;
        public String courseId;
        public String courseTitle;
        public int instructorId;
        public String instructorName;
        public String dayTime;
        public String room;
        public int capacity;
        public int availableCapacity;
        public String semester;
        public int year;

        public SectionRow(int id, String courseId, String courseTitle,
                          int instructorId, String instructorName,
                          String dayTime, String room,
                          int capacity, int availableCapacity,
                          String semester, int year) {

            this.id = id;
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.instructorId = instructorId;
            this.instructorName = instructorName;
            this.dayTime = dayTime;
            this.room = room;
            this.capacity = capacity;
            this.availableCapacity = availableCapacity;
            this.semester = semester;
            this.year = year;
        }
    }
}
