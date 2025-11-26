package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.DropDeadline;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Converted from JFrame → JPanel so it can load inside StudentMainFrame.
 * Includes drop-deadline enforcement and deadline display.
 */
public class StudentEnrollmentsUI extends JPanel {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel model;
    private JLabel deadlineLabel;

    public StudentEnrollmentsUI(User student) {
        this.currentStudent = student;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initUI();
        loadEnrollments();
    }

    private void initUI() {

        // ===== HEADER PANEL =====
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        JLabel title = new JLabel("My Enrolled Courses", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        top.add(title, BorderLayout.NORTH);

        // ===== Maintenance Banner =====
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Dropping Courses is Disabled",
                    SwingConstants.CENTER
            );
            banner.setOpaque(true);
            banner.setBackground(new Color(255, 204, 0));
            banner.setForeground(Color.BLACK);
            banner.setFont(new Font("SansSerif", Font.BOLD, 14));
            banner.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);

        JScrollPane scroller = new JScrollPane(table);
        scroller.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroller, BorderLayout.CENTER);

        // ===================================================
        // BOTTOM PANEL WITH DEADLINE + BUTTONS
        // ===================================================
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // deadline label
        deadlineLabel = new JLabel();
        deadlineLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));

        updateDeadlineLabel(); // set text + color

        // buttons panel (right)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnDrop = new JButton("Drop Selected");
        JButton btnRefresh = new JButton("Refresh");

        btnPanel.add(btnDrop);
        btnPanel.add(btnRefresh);

        bottom.add(deadlineLabel, BorderLayout.WEST);
        bottom.add(btnPanel, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ACTIONS
        btnDrop.addActionListener(e -> dropSelected());
        btnRefresh.addActionListener(e -> loadEnrollments());
    }

    // ===================================================
    // DEADLINE LABEL UPDATER
    // ===================================================
    private void updateDeadlineLabel() {
        LocalDateTime ddl = DropDeadline.getDeadline();

        String text = "Drop Deadline: " +
                ddl.format(DateTimeFormatter.ofPattern("dd MMM yyyy - hh:mm a"));

        if (LocalDateTime.now().isAfter(ddl)) {
            text += "  (EXPIRED)";
            deadlineLabel.setForeground(Color.RED);
        } else {
            deadlineLabel.setForeground(new Color(0, 120, 0));
        }

        deadlineLabel.setText(text);
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

        updateDeadlineLabel(); // refresh on load
    }

    private void dropSelected() {

        // ===== BLOCK DURING MAINTENANCE =====
        if (DatabaseConnection.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Maintenance Mode is active.\nDropping courses is temporarily disabled.",
                    "Action Blocked",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // ===== GLOBAL DEADLINE ENFORCEMENT =====
        LocalDateTime ddl = DropDeadline.getDeadline();
        if (LocalDateTime.now().isAfter(ddl)) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "The drop deadline has passed.\nYou can no longer drop this course.",
                    "Deadline Passed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Please select a course to drop.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "Are you sure you want to drop this course?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = studentService.dropCourse(currentStudent.getId(), sectionId);

        if (ok) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Course dropped successfully!",
                    "Dropped",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadEnrollments();
        } else {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Failed to drop course.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
