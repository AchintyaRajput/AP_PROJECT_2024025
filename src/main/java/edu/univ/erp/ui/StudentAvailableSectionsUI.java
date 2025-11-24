package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Shows all available sections and allows the student to enroll.
 * Displays ONLY available capacity.
 * Enrollment is disabled during Maintenance Mode.
 */
public class StudentAvailableSectionsUI extends JFrame {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel model;

    public StudentAvailableSectionsUI(User student) {
        this.currentStudent = student;

        setTitle("Available Sections - " + student.getUsername());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadSections();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== HEADER PANEL =====
        JPanel top = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Available Sections", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        top.add(title, BorderLayout.NORTH);

        // ===== MAINTENANCE BANNER =====
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Enrollment is Disabled",
                    SwingConstants.CENTER
            );
            banner.setOpaque(true);
            banner.setBackground(new Color(255, 204, 0));
            banner.setForeground(Color.BLACK);
            banner.setFont(new Font("SansSerif", Font.BOLD, 14));

            top.add(banner, BorderLayout.SOUTH);
        }

        add(top, BorderLayout.NORTH);

        // ===== TABLE MODEL =====
        model = new DefaultTableModel(new Object[]{
                "Section ID", "Course ID", "Title", "Instructor",
                "Day/Time", "Room", "Available Seats",
                "Semester", "Year"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // prevent editing
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnEnroll = new JButton("Enroll Selected");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(btnEnroll);
        bottom.add(btnRefresh);

        add(bottom, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        btnEnroll.addActionListener(e -> enrollSelected());
        btnRefresh.addActionListener(e -> loadSections());
    }

    private void loadSections() {
        model.setRowCount(0); // clear

        List<StudentService.SectionRow> list = studentService.getAvailableSections();

        for (StudentService.SectionRow s : list) {
            model.addRow(new Object[]{
                    s.sectionId,
                    s.courseId,
                    s.courseTitle,
                    s.instructorName,
                    s.dayTime,
                    s.room,
                    s.availableCapacity, // ONLY AVAILABLE SEATS
                    s.semester,
                    s.year
            });
        }
    }

    private void enrollSelected() {

        // ===== BLOCK DURING MAINTENANCE =====
        if (DatabaseConnection.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(this,
                    "Maintenance Mode is active.\nEnrollment is temporarily disabled.",
                    "Action Blocked",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section to enroll.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        // Backend call
        String result = studentService.registerForSection(currentStudent.getId(), sectionId);

        if (result.equals("SUCCESS")) {
            JOptionPane.showMessageDialog(this,
                    "Enrolled successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadSections();
        } else {
            JOptionPane.showMessageDialog(this,
                    result,
                    "Enrollment Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
