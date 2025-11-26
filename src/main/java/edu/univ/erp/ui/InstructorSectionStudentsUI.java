package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modernized: InstructorSectionStudentsUI (Popup)
 * - Clean white/green theme
 * - Modern heading bar
 * - Right-aligned buttons
 * - Table inside bordered scroll panel
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

        setTitle("Section Students");
        setSize(950, 570);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        initHeader();
        initTable();
        initBottomButtons();

        loadStudents();
        setVisible(true);
    }

    private void initHeader() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(220, 240, 220)); // light green
        top.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel title = new JLabel("Students in Section " + sectionId, SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        top.add(title, BorderLayout.WEST);

        // Maintenance banner
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode — Grade Editing Disabled",
                    SwingConstants.CENTER
            );
            banner.setOpaque(true);
            banner.setBackground(new Color(255, 204, 0));
            banner.setForeground(Color.BLACK);
            banner.setFont(new Font("SansSerif", Font.BOLD, 14));
            banner.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.add(top, BorderLayout.NORTH);
            wrapper.add(banner, BorderLayout.SOUTH);
            add(wrapper, BorderLayout.NORTH);
        } else {
            add(top, BorderLayout.NORTH);
        }
    }

    private void initTable() {
        model = new DefaultTableModel(new Object[]{
                "Enrollment ID", "Student ID", "Name", "Email", "Final Grade"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        add(scroll, BorderLayout.CENTER);
    }

    private void initBottomButtons() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnGrades = makeButton("Enter / Edit Grades");
        JButton btnRefresh = makeButton("Refresh");

        bottom.add(btnGrades);
        bottom.add(btnRefresh);

        add(bottom, BorderLayout.SOUTH);

        btnGrades.addActionListener(e -> openGradeEntry());
        btnRefresh.addActionListener(e -> loadStudents());
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void loadStudents() {
        model.setRowCount(0);

        List<InstructorService.StudentRow> students =
                instructorService.getStudentsForSection(sectionId);

        // load final grades
        java.util.Map<Integer, String> finalGrades = new java.util.HashMap<>();
        String sqlFinal = """
            SELECT enrollment_id, final_grade
            FROM enrollments
            WHERE section_id = ?
        """;

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
            model.addRow(new Object[]{ s.enrollmentId, s.studentId, s.studentName, s.email, fg });
        }
    }

    private void openGradeEntry() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Select a student first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int enrollmentId = (int) model.getValueAt(row, 0);
        String studentName = (String) model.getValueAt(row, 2);

        new GradeEntryUI(enrollmentId, studentName).setVisible(true);
    }
}
