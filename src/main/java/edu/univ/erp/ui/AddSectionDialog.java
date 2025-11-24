package edu.univ.erp.ui;

import edu.univ.erp.service.CourseService;
import edu.univ.erp.service.SectionService;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import edu.univ.erp.data.DatabaseConnection;

/**
 * Dialog for adding or editing a section.
 * Updated: Day dropdown + Start/End time dropdowns (30-min steps).
 * Start times: 09:00 -> 16:30
 * End times:   10:00 -> 18:00
 * Stored format: "Mon 09:00-10:30"
 */
public class AddSectionDialog extends JDialog {

    private final JComboBox<String> courseBox = new JComboBox<>();
    private final JComboBox<InstructorItem> instructorBox = new JComboBox<>();

    // Day dropdown
    private final JComboBox<String> dayBox =
            new JComboBox<>(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri"});

    // Start and End time dropdowns (30-min steps)
    private final JComboBox<String> startTimeBox = new JComboBox<>();
    private final JComboBox<String> endTimeBox = new JComboBox<>();

    private final JTextField roomField = new JTextField();
    private final JTextField capacityField = new JTextField();
    private final JTextField semesterField = new JTextField();
    private final JTextField yearField = new JTextField();

    private final SectionService service;
    private final SectionService.SectionRow editingSection;

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public AddSectionDialog(Frame owner, SectionService service,
                            SectionService.SectionRow editingSection) {
        super(owner, true);
        this.service = service;
        this.editingSection = editingSection;

        setTitle(editingSection == null ? "Add Section" : "Edit Section");
        setSize(520, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // populate time dropdowns
        populateTimeBoxes();

        loadCourses();
        loadInstructors();
        initForm();
        initButtons();

        if (editingSection != null) loadData();
    }

    // Create 30-min step times and fill the combo boxes
    private void populateTimeBoxes() {
        // Start times: 09:00 -> 16:30
        LocalTime s = LocalTime.of(9, 0);
        LocalTime lastStart = LocalTime.of(16, 30);
        while (!s.isAfter(lastStart)) {
            startTimeBox.addItem(s.format(TF));
            s = s.plusMinutes(30);
        }

        // End times: 10:00 -> 18:00
        LocalTime e = LocalTime.of(10, 0);
        LocalTime lastEnd = LocalTime.of(18, 0);
        while (!e.isAfter(lastEnd)) {
            endTimeBox.addItem(e.format(TF));
            e = e.plusMinutes(30);
        }
    }

    // ===== Load Courses =====
    private void loadCourses() {
        try {
            CourseService courseService = new CourseService();
            var list = courseService.getAllCourses();
            for (var c : list) {
                courseBox.addItem(c.id + " - " + c.title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Load Instructors =====
    private void loadInstructors() {
        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT instructor_id, name FROM instructors");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                instructorBox.addItem(new InstructorItem(
                        rs.getInt("instructor_id"),
                        rs.getString("name")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Build Form Layout =====
    private void initForm() {
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        form.add(new JLabel("Course:"));
        form.add(courseBox);

        form.add(new JLabel("Instructor:"));
        form.add(instructorBox);

        form.add(new JLabel("Day:"));
        form.add(dayBox);

        // Start time
        form.add(new JLabel("Start Time:"));
        form.add(startTimeBox);

        // End time
        form.add(new JLabel("End Time:"));
        form.add(endTimeBox);

        // Hint label to show format example
        form.add(new JLabel("Time format example:"));
        form.add(new JLabel("09:00-10:30"));

        form.add(new JLabel("Room:"));
        form.add(roomField);

        form.add(new JLabel("Capacity:"));
        form.add(capacityField);

        form.add(new JLabel("Semester:"));
        form.add(semesterField);

        form.add(new JLabel("Year:"));
        form.add(yearField);

        add(form, BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnCancel = new JButton("Cancel");
        JButton btnSave = new JButton("Save");

        bottom.add(btnCancel);
        bottom.add(btnSave);

        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());
    }

    // ===== Load data into form when editing =====
    private void loadData() {

        // Select course
        for (int i = 0; i < courseBox.getItemCount(); i++) {
            if (courseBox.getItemAt(i).startsWith(editingSection.courseId)) {
                courseBox.setSelectedIndex(i);
                break;
            }
        }

        // Select instructor
        for (int i = 0; i < instructorBox.getItemCount(); i++) {
            InstructorItem ii = instructorBox.getItemAt(i);
            if (ii.id == editingSection.instructorId) {
                instructorBox.setSelectedIndex(i);
                break;
            }
        }

        // Parse "Mon 09:00-10:30" (robust to "Mon 9:00-10:30" too)
        if (editingSection.dayTime != null && editingSection.dayTime.contains(" ")) {
            String[] parts = editingSection.dayTime.split(" ", 2);
            String day = parts[0];
            String times = parts.length > 1 ? parts[1] : "";

            dayBox.setSelectedItem(day);

            if (times.contains("-")) {
                String[] tr = times.split("-", 2);
                String st = tr[0].trim();
                String et = tr[1].trim();

                // Normalize to HH:mm (if single-digit hour)
                try {
                    LocalTime start = parseTimeLenient(st);
                    LocalTime end = parseTimeLenient(et);
                    startTimeBox.setSelectedItem(start.format(TF));
                    endTimeBox.setSelectedItem(end.format(TF));
                } catch (Exception ex) {
                    // fallback: set raw if parse fails
                    startTimeBox.setSelectedItem(st);
                    endTimeBox.setSelectedItem(et);
                }
            }
        }

        roomField.setText(editingSection.room);
        capacityField.setText(String.valueOf(editingSection.capacity));
        semesterField.setText(editingSection.semester);
        yearField.setText(String.valueOf(editingSection.year));
    }

    // ===== Save Section =====
    private void onSave() {
        try {
            String courseSelect = (String) courseBox.getSelectedItem();
            String courseId = courseSelect.split(" - ")[0];

            InstructorItem instructor = (InstructorItem) instructorBox.getSelectedItem();
            int instructorId = instructor.id;

            String day = (String) dayBox.getSelectedItem();
            String start = (String) startTimeBox.getSelectedItem();
            String end = (String) endTimeBox.getSelectedItem();

            // Validate times exist
            LocalTime s = LocalTime.parse(start, TF);
            LocalTime e = LocalTime.parse(end, TF);

            if (!e.isAfter(s)) {
                JOptionPane.showMessageDialog(this,
                        "End time must be after start time.",
                        "Invalid Time Range",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Combine into final format (zero-padded)
            String dayTime = day + " " + s.format(TF) + "-" + e.format(TF);

            String room = roomField.getText().trim();
            int capacity = Integer.parseInt(capacityField.getText().trim());
            String semester = semesterField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            boolean ok;

            if (editingSection == null) {
                ok = service.addSection(courseId, instructorId, dayTime,
                        room, capacity, semester, year);
            } else {
                ok = service.updateSection(editingSection.id, courseId, instructorId,
                        dayTime, room, capacity, semester, year);
            }

            if (ok) {
                JOptionPane.showMessageDialog(this, "Section saved successfully.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save section.");
            }

        } catch (NumberFormatException nf) {
            JOptionPane.showMessageDialog(this, "Capacity and Year must be numbers.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid data: " + e.getMessage());
        }
    }

    // parse times like "9:00" or "09:00"
    private static LocalTime parseTimeLenient(String t) {
        String s = t.trim();
        DateTimeFormatter f1 = DateTimeFormatter.ofPattern("H:mm");
        DateTimeFormatter f2 = DateTimeFormatter.ofPattern("HH:mm");
        try {
            return LocalTime.parse(s, f1);
        } catch (Exception ex) {
            return LocalTime.parse(s, f2);
        }
    }

    // Helper class for instructor dropdown
    private static class InstructorItem {
        int id;
        String name;

        InstructorItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }
}
