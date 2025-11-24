package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Shows student's current enrollments and allows dropping a course.
 * (Dropping disabled during Maintenance Mode)
 */
public class StudentEnrollmentsUI extends JFrame {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel model;

    public StudentEnrollmentsUI(User student) {
        this.currentStudent = student;

        setTitle("My Enrollments - " + student.getUsername());
        setSize(850, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadEnrollments();

        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== HEADER PANEL =====
        JPanel top = new JPanel(new BorderLayout());

        JLabel title = new JLabel("My Enrolled Courses", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        top.add(title, BorderLayout.NORTH);

        // ===== MAINTENANCE BANNER =====
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Dropping Courses is Disabled",
                    SwingConstants.CENTER
            );
            banner.setOpaque(true);
            banner.setBackground(new Color(255, 204, 0));
            banner.setForeground(Color.BLACK);
            banner.setFont(new Font("SansSerif", Font.BOLD, 14));

            top.add(banner, BorderLayout.SOUTH);
        }

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[]{
                "Section ID", "Course ID", "Course Title",
                "Instructor", "Day/Time", "Room", "Semester", "Year"
        }, 0) {

            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // no cell editing
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnDrop = new JButton("Drop Selected");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(btnDrop);
        bottom.add(btnRefresh);

        add(bottom, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        btnDrop.addActionListener(e -> dropSelected());
        btnRefresh.addActionListener(e -> loadEnrollments());
    }

    private void loadEnrollments() {
        model.setRowCount(0);

        List<StudentService.EnrollmentRow> list =
                studentService.getStudentEnrollments(currentStudent.getId());

        for (StudentService.EnrollmentRow e : list) {
            model.addRow(new Object[]{
                    e.sectionId,
                    e.courseId,
                    e.courseTitle,
                    e.instructorName,
                    e.dayTime,
                    e.room,
                    e.semester,
                    e.year
            });
        }
    }

    private void dropSelected() {

        // ===== BLOCK DURING MAINTENANCE =====
        if (DatabaseConnection.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(this,
                    "Maintenance Mode is active.\nDropping courses is temporarily disabled.",
                    "Action Blocked",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to drop.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to drop this course?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = studentService.dropCourse(currentStudent.getId(), sectionId);

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Course dropped successfully!",
                    "Dropped",
                    JOptionPane.INFORMATION_MESSAGE);
            loadEnrollments();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to drop course.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
