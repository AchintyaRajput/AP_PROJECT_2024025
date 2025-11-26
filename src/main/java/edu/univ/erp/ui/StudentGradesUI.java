package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Updated StudentGradesUI:
 * - Displays Score / Max Marks
 * - Displays Weighted Score using (score/maxMarks)*weight
 * - Matches new grading system
 */
public class StudentGradesUI extends JPanel {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel model;

    public StudentGradesUI(User student) {
        this.currentStudent = student;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initUI();
        loadGrades();
    }

    private void initUI() {

        JLabel title = new JLabel("My Grades", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        add(title, BorderLayout.NORTH);

        // ===== TABLE MODEL =====
        model = new DefaultTableModel(new Object[]{
                "Course ID",
                "Course Title",
                "Component",
                "Score",
                "Max Marks",
                "Weight (%)",
                "Weighted Score"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(scroll, BorderLayout.CENTER);

        // ===== REFRESH BUTTON =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadGrades());

        bottom.add(btnRefresh);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadGrades() {
        model.setRowCount(0);

        List<StudentService.GradeRow> list =
                studentService.getGrades(currentStudent.getId());

        for (StudentService.GradeRow g : list) {

            double weighted = 0;
            if (g.maxMarks > 0)
                weighted = (g.score / g.maxMarks) * g.weight;

            model.addRow(new Object[]{
                    g.courseId,
                    g.courseTitle,
                    g.component,
                    g.score,
                    g.maxMarks,
                    g.weight,
                    String.format("%.2f", weighted)
            });
        }
    }
}
