package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InstructorSectionsUI extends JFrame {

    private final User currentInstructor;
    private final InstructorService instructorService = new InstructorService();

    private JTable table;
    private DefaultTableModel model;

    public InstructorSectionsUI(User instructor) {
        this.currentInstructor = instructor;

        setTitle("My Sections - " + instructor.getUsername());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadSections();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("My Assigned Sections", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "Section ID", "Course ID", "Course Title",
                "Day/Time", "Room", "Semester", "Year"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnViewStudents = new JButton("View Students");
        JButton btnStats = new JButton("Class Stats");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(btnViewStudents);
        bottom.add(btnStats);
        bottom.add(btnRefresh);
        add(bottom, BorderLayout.SOUTH);

        btnViewStudents.addActionListener(e -> viewStudents());
        btnStats.addActionListener(e -> showStats());
        btnRefresh.addActionListener(e -> loadSections());
    }

    private void loadSections() {
        model.setRowCount(0);

        List<InstructorService.SectionRow> list =
                instructorService.getInstructorSections(currentInstructor.getId());

        for (InstructorService.SectionRow s : list) {
            model.addRow(new Object[]{
                    s.sectionId,
                    s.courseId,
                    s.courseTitle,
                    s.dayTime,
                    s.room,
                    s.semester,
                    s.year
            });
        }
    }

    private void viewStudents() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a section first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        new InstructorSectionStudentsUI(currentInstructor, sectionId).setVisible(true);
    }

    // ===============================
    // CLASS STATS POPUP
    // ===============================
    private void showStats() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a section first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        InstructorService.StatsRow stats =
                instructorService.getSectionStats(sectionId);

        if (stats == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No data available.",
                    "Class Stats",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String message = ""
                + "Class Average: " + String.format("%.2f", stats.average) + "\n"
                + "Median: " + String.format("%.2f", stats.median) + "\n"
                + "Pass Count: " + stats.passCount + "\n"
                + "Fail Count: " + stats.failCount;

        JOptionPane.showMessageDialog(
                this,
                message,
                "Class Statistics",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
