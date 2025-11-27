package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class StudentAvailableSectionsUI extends JPanel {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel model;

    public StudentAvailableSectionsUI(User student) {
        this.currentStudent = student;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initUI();
        loadSections();
    }

    private void initUI() {

        
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        JLabel title = new JLabel("Available Sections", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        top.add(title, BorderLayout.NORTH);

        
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Enrollment is Disabled",
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

        
        model = new DefaultTableModel(new Object[]{
                "Section ID", "Course ID", "Title", "Instructor",
                "Day/Time", "Room", "Available Seats",
                "Semester", "Year"
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

        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);

        JButton btnEnroll = new JButton("Enroll Selected");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(btnEnroll);
        bottom.add(btnRefresh);

        add(bottom, BorderLayout.SOUTH);

        
        btnEnroll.addActionListener(e -> enrollSelected());
        btnRefresh.addActionListener(e -> loadSections());
    }

    private void loadSections() {
        model.setRowCount(0); 

        List<StudentService.SectionRow> list = studentService.getAvailableSections();

        for (StudentService.SectionRow s : list) {
            model.addRow(new Object[]{
                    s.sectionId,
                    s.courseId,
                    s.courseTitle,
                    s.instructorName,
                    s.dayTime,
                    s.room,
                    s.availableCapacity,
                    s.semester,
                    s.year
            });
        }
    }

    private void enrollSelected() {

        
        if (DatabaseConnection.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Maintenance Mode is active.\nEnrollment is temporarily disabled.",
                    "Action Blocked",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Please select a section to enroll.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        String result = studentService.registerForSection(currentStudent.getId(), sectionId);

        if (result.equals("SUCCESS")) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Enrolled successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadSections();
        } else {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    result,
                    "Enrollment Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
