package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Converted from JFrame → JPanel for embedding inside InstructorDashboard.
 * All functionality preserved.
 */
public class InstructorSectionsUI extends JPanel {

    private final User currentInstructor;
    private final InstructorService instructorService = new InstructorService();

    private JTable table;
    private DefaultTableModel model;

    public InstructorSectionsUI(User instructor) {
        this.currentInstructor = instructor;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initUI();
        loadSections();
    }

    private void initUI() {

        JLabel title = new JLabel("My Assigned Sections", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // ===== TABLE MODEL =====
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

        JScrollPane scroller = new JScrollPane(table);
        scroller.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroller, BorderLayout.CENTER);

        // ===== BOTTOM BUTTONS =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);

        JButton btnViewStudents = new JButton("View Students");
        JButton btnStats = new JButton("Class Stats");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(btnViewStudents);
        bottom.add(btnStats);
        bottom.add(btnRefresh);

        add(bottom, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        btnViewStudents.addActionListener(e -> viewStudents());
        btnStats.addActionListener(e -> showStats());
        btnRefresh.addActionListener(e -> loadSections());
    }

    public void loadSections() {
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
                    SwingUtilities.getWindowAncestor(this),
                    "Please select a section first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        new InstructorSectionStudentsUI(currentInstructor, sectionId)
                .setVisible(true);
    }

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

        String msg = """
            Class Statistics:
            
            Average Final Grade: %.2f
            Median Final Grade: %.2f
            Pass Count: %d
            Fail Count: %d
            """.formatted(
                stats.average,
                stats.median,
                stats.passCount,
                stats.failCount
        );

        JOptionPane.showMessageDialog(
                this,
                msg,
                "Class Statistics",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
