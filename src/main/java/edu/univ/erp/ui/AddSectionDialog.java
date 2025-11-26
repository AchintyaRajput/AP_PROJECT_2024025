package edu.univ.erp.ui;

import edu.univ.erp.service.SectionService;
import edu.univ.erp.service.CourseService;
import edu.univ.erp.data.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AddSectionDialog extends JDialog {

    private final JComboBox<String> courseBox = new JComboBox<>();
    private final JComboBox<InstructorItem> instructorBox = new JComboBox<>();
    private final JComboBox<String> dayBox =
            new JComboBox<>(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri"});
    private final JComboBox<String> startTimeBox = new JComboBox<>();
    private final JComboBox<String> endTimeBox = new JComboBox<>();

    private final JTextField roomField = new JTextField();
    private final JTextField capacityField = new JTextField();
    private final JTextField semesterField = new JTextField();
    private final JTextField yearField = new JTextField();

    private final SectionService service;
    private final SectionService.SectionRow editingSection;

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public AddSectionDialog(Window parent, SectionService service,
                            SectionService.SectionRow editingSection) {

        super(parent instanceof Frame ? (Frame) parent : null, true);

        this.service = service;
        this.editingSection = editingSection;

        setTitle(editingSection == null ? "Add Section" : "Edit Section");
        setSize(650, 720);
        setResizable(false);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        populateTimeBoxes();
        loadCourses();
        loadInstructors();

        initUI();

        if (editingSection != null) loadData();
    }

    // =============================
    // Time interval combos
    // =============================
    private void populateTimeBoxes() {
        LocalTime s = LocalTime.of(9, 0);
        LocalTime lastStart = LocalTime.of(16, 30);
        while (!s.isAfter(lastStart)) {
            startTimeBox.addItem(s.format(TF));
            s = s.plusMinutes(30);
        }

        LocalTime e = LocalTime.of(10, 0);
        LocalTime lastEnd = LocalTime.of(18, 0);
        while (!e.isAfter(lastEnd)) {
            endTimeBox.addItem(e.format(TF));
            e = e.plusMinutes(30);
        }
    }

    private void loadCourses() {
        try {
            CourseService cs = new CourseService();
            var list = cs.getAllCourses();
            for (var c : list) {
                courseBox.addItem(c.id + " - " + c.title);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ========================
    // BUILD UI
    // ========================
    private void initUI() {

        JPanel form = new JPanel(new GridLayout(0, 1, 12, 16));
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        form.setBackground(new Color(238, 247, 238));

        addLabeledField(form, "Course", courseBox);
        addLabeledField(form, "Instructor", instructorBox);
        addLabeledField(form, "Day", dayBox);
        addLabeledField(form, "Start Time", startTimeBox);
        addLabeledField(form, "End Time", endTimeBox);
        addLabeledField(form, "Room", roomField);
        addLabeledField(form, "Capacity", capacityField);
        addLabeledField(form, "Semester", semesterField);
        addLabeledField(form, "Year", yearField);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Cancel");
        JButton btnSave = new JButton("Save");

        styleButton(btnSave);
        styleButton(btnCancel);

        bottom.add(btnCancel);
        bottom.add(btnSave);
        bottom.setBackground(Color.WHITE);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        add(bottom, BorderLayout.SOUTH);
    }

    private void addLabeledField(JPanel panel, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Inter", Font.BOLD, 15));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 36));

        panel.add(lbl);
        panel.add(field);
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("Inter", Font.BOLD, 15));
        b.setBackground(new Color(205, 235, 205));
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 38));
    }

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

        // Parse times
        try {
            String[] parts = editingSection.dayTime.split(" ");
            dayBox.setSelectedItem(parts[0]);

            String[] tr = parts[1].split("-");
            LocalTime start = LocalTime.parse(tr[0], TF);
            LocalTime end = LocalTime.parse(tr[1], TF);

            startTimeBox.setSelectedItem(start.format(TF));
            endTimeBox.setSelectedItem(end.format(TF));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        roomField.setText(editingSection.room);
        capacityField.setText(String.valueOf(editingSection.capacity));
        semesterField.setText(editingSection.semester);
        yearField.setText(String.valueOf(editingSection.year));
    }

    // ========================
    // VALIDATION + SAVE
    // ========================
    private boolean validateFields() {

        if (courseBox.getSelectedItem() == null) {
            error("Please select a course.");
            return false;
        }

        if (instructorBox.getSelectedItem() == null) {
            error("Please select an instructor.");
            return false;
        }

        if (dayBox.getSelectedItem() == null) {
            error("Please select a day.");
            return false;
        }

        if (startTimeBox.getSelectedItem() == null || endTimeBox.getSelectedItem() == null) {
            error("Start and end times must be selected.");
            return false;
        }

        if (roomField.getText().trim().isEmpty()) {
            error("Room cannot be empty.");
            return false;
        }

        // Capacity validation
        try {
            int cap = Integer.parseInt(capacityField.getText().trim());
            if (cap < 0) {
                error("Capacity cannot be negative.");
                return false;
            }
        } catch (NumberFormatException ex) {
            error("Capacity must be a valid number.");
            return false;
        }

        // Semester
        if (semesterField.getText().trim().isEmpty()) {
            error("Semester cannot be empty.");
            return false;
        }

        // Year validation
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            if (year < 2000 || year > 2100) {
                error("Enter a valid year (2000 - 2100).");
                return false;
            }
        } catch (NumberFormatException ex) {
            error("Year must be a valid number.");
            return false;
        }

        // Time logic
        LocalTime start = LocalTime.parse(startTimeBox.getSelectedItem().toString(), TF);
        LocalTime end = LocalTime.parse(endTimeBox.getSelectedItem().toString(), TF);

        if (!end.isAfter(start)) {
            error("End time must be after start time.");
            return false;
        }

        return true;
    }

    private void onSave() {

        if (!validateFields()) return;

        try {
            String courseId = courseBox.getSelectedItem().toString().split(" - ")[0];

            InstructorItem instructor = (InstructorItem) instructorBox.getSelectedItem();
            int instructorId = instructor.id;

            String day = dayBox.getSelectedItem().toString();
            String start = startTimeBox.getSelectedItem().toString();
            String end = endTimeBox.getSelectedItem().toString();

            String dayTime = day + " " + start + "-" + end;

            String room = roomField.getText().trim();
            int capacity = Integer.parseInt(capacityField.getText().trim());
            String semester = semesterField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            boolean ok;

            if (editingSection == null) {
                ok = service.addSection(courseId, instructorId, dayTime, room,
                        capacity, semester, year);
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

        } catch (Exception ex) {
            error("Invalid input: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // Helper class
    private static class InstructorItem {
        int id;
        String name;

        InstructorItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }
}
