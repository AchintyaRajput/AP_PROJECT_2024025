package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Shows all students enrolled in a section.
 * Allows instructor to open GradeEntryUI for each student.
 * Supports maintenance banner.
 */
public class InstructorSectionStudentsUI extends JFrame {

    private final User currentInstructor;
    private final int sectionId;
    private final InstructorService instructorService = new InstructorService();

    private JTable table;
    private DefaultTableModel model;

    public InstructorSectionStudentsUI(User instructor, int sectionId) {
        this.currentInstructor = instructor;
        this.sectionId = sectionId;

        setTitle("Students in Section " + sectionId);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadStudents();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ================== TOP HEADER ==================
        JPanel top = new JPanel(new BorderLayout());

        JLabel title = new JLabel(
                "Students Enrolled in Section " + sectionId,
                SwingConstants.CENTER
        );
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        top.add(title, BorderLayout.NORTH);

        // ======= Maintenance Banner =======
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Grade Editing Disabled",
                    SwingConstants.CENTER
            );
            banner.setOpaque(true);
            banner.setBackground(new Color(255, 204, 0));
            banner.setForeground(Color.BLACK);
            banner.setFont(new Font("SansSerif", Font.BOLD, 14));

            top.add(banner, BorderLayout.SOUTH);
        }

        add(top, BorderLayout.NORTH);


        // ================== TABLE ==================
        model = new DefaultTableModel(new Object[]{
                "Enrollment ID", "Student ID", "Name", "Email", "Final Grade"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);


        // ================== BOTTOM BUTTONS ==================
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnGrades = new JButton("Enter / Edit Grades");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(btnGrades);
        bottom.add(btnRefresh);

        add(bottom, BorderLayout.SOUTH);


        // ACTIONS
        btnGrades.addActionListener(e -> openGradeEntry());
        btnRefresh.addActionListener(e -> loadStudents());
    }


    // ================== LOAD STUDENTS ==================
    private void loadStudents() {
        model.setRowCount(0);

        List<InstructorService.StudentRow> students =
                instructorService.getStudentsForSection(sectionId);

        // Also fetch final grades
        String sqlFinal = """
            SELECT enrollment_id, final_grade
            FROM enrollments
            WHERE section_id = ?
        """;

        java.util.Map<Integer, String> finalGrades = new java.util.HashMap<>();

        try (var conn = DatabaseConnection.getERPConnection();
             var ps = conn.prepareStatement(sqlFinal)) {

            ps.setInt(1, sectionId);
            var rs = ps.executeQuery();

            while (rs.next()) {
                finalGrades.put(
                        rs.getInt("enrollment_id"),
                        rs.getString("final_grade")
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (InstructorService.StudentRow s : students) {
            String fg = finalGrades.getOrDefault(s.enrollmentId, "");
            model.addRow(new Object[]{
                    s.enrollmentId,
                    s.studentId,
                    s.studentName,
                    s.email,
                    fg
            });
        }
    }


    // ================== OPEN GRADE ENTRY ==================
    private void openGradeEntry() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int enrollmentId = (int) model.getValueAt(row, 0);
        String studentName = (String) model.getValueAt(row, 2);

        new GradeEntryUI(enrollmentId, studentName).setVisible(true);
    }
}
