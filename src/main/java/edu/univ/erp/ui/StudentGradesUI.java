package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Shows all grades for the logged-in student.
 */
public class StudentGradesUI extends JFrame {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel model;

    public StudentGradesUI(User student) {
        this.currentStudent = student;

        setTitle("My Grades - " + student.getUsername());
        setSize(850, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadGrades();

        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("My Grades", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "Course ID", "Course Title",
                "Component", "Score", "Weight", "Weighted Score"
        }, 0) {

            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // read-only table
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom refresh button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadGrades());
        bottom.add(btnRefresh);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadGrades() {
        model.setRowCount(0);

        List<StudentService.GradeRow> list =
                studentService.getGrades(currentStudent.getId());

        for (StudentService.GradeRow g : list) {
            double weighted = g.score * (g.weight / 100.0);

            model.addRow(new Object[]{
                    g.courseId,
                    g.courseTitle,
                    g.component,
                    g.score,
                    g.weight + " %",
                    String.format("%.2f", weighted)
            });
        }
    }
}
