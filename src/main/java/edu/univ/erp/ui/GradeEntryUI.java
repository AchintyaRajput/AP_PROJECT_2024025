package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Fully working Grade Entry UI
 * - Add grade
 * - Update grade
 * - Load grade on row-double-click
 * - Calculate final grade
 * - Maintenance mode blocking
 */
public class GradeEntryUI extends JFrame {

    private final int enrollmentId;
    private final String studentName;

    private final InstructorService instructorService = new InstructorService();

    private JTable table;
    private DefaultTableModel model;

    // Input fields
    private JTextField txtComponent;
    private JTextField txtScore;
    private JTextField txtWeight;

    public GradeEntryUI(int enrollmentId, String studentName) {
        this.enrollmentId = enrollmentId;
        this.studentName = studentName;

        setTitle("Grade Entry - " + studentName);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadGrades();

        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== TOP PANEL =====
        JPanel top = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Grades for " + studentName, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        top.add(title, BorderLayout.NORTH);

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

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[]{
                "Grade ID", "Component", "Score", "Weight (%)"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // double-click to load row into form
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2)
                    loadSelectedIntoForm();
            }
        });

        // ===== BOTTOM WRAPPER (form + buttons) =====
        JPanel bottomWrapper = new JPanel();
        bottomWrapper.setLayout(new BorderLayout());

        // ===== INPUT FORM =====
        JPanel form = new JPanel(new GridLayout(1, 6, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtComponent = new JTextField();
        txtScore = new JTextField();
        txtWeight = new JTextField();

        form.add(new JLabel("Component:"));
        form.add(txtComponent);
        form.add(new JLabel("Score:"));
        form.add(txtScore);
        form.add(new JLabel("Weight:"));
        form.add(txtWeight);

        bottomWrapper.add(form, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnCalc = new JButton("Calculate Final Grade");
        JButton btnSave = new JButton("Save / Update");
        JButton btnRefresh = new JButton("Refresh");

        bottomButtons.add(btnCalc);
        bottomButtons.add(btnSave);
        bottomButtons.add(btnRefresh);

        bottomWrapper.add(bottomButtons, BorderLayout.SOUTH);

        add(bottomWrapper, BorderLayout.SOUTH);

        // ACTIONS
        btnCalc.addActionListener(e -> calculateFinalGrade());
        btnSave.addActionListener(e -> saveGrade());
        btnRefresh.addActionListener(e -> loadGrades());
    }

    private void loadGrades() {
        model.setRowCount(0);

        List<InstructorService.GradeRow> list =
                instructorService.getGradesForEnrollment(enrollmentId);

        for (InstructorService.GradeRow g : list) {
            model.addRow(new Object[]{
                    g.gradeId,
                    g.component,
                    g.score,
                    g.weight
            });
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        txtComponent.setText(model.getValueAt(row, 1).toString());
        txtScore.setText(model.getValueAt(row, 2).toString());
        txtWeight.setText(model.getValueAt(row, 3).toString());
    }

    private void saveGrade() {

        if (DatabaseConnection.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(this,
                    "Maintenance Mode is active.\nGrade editing is disabled.",
                    "Action Blocked",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String component = txtComponent.getText().trim();
            double score = Double.parseDouble(txtScore.getText().trim());
            double weight = Double.parseDouble(txtWeight.getText().trim());

            if (component.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Component cannot be empty.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = instructorService.saveGrade(
                    enrollmentId, component, score, weight
            );

            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Grade saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadGrades();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save grade.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Score and weight must be numbers.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateFinalGrade() {
        try (var conn = DatabaseConnection.getERPConnection()) {

            String sql = """
                SELECT score, weight
                FROM grades
                WHERE enrollment_id = ?
            """;

            var ps = conn.prepareStatement(sql);
            ps.setInt(1, enrollmentId);
            var rs = ps.executeQuery();

            double total = 0;

            while (rs.next()) {
                double score = rs.getDouble("score");
                double weight = rs.getDouble("weight");
                total += score * (weight / 100.0);
            }

            // update enrollment final_grade
            String up = """
                UPDATE enrollments
                SET final_grade = ?
                WHERE enrollment_id = ?
            """;

            var ps2 = conn.prepareStatement(up);
            ps2.setDouble(1, total);
            ps2.setInt(2, enrollmentId);
            ps2.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Final Grade Calculated: " + total,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error calculating final grade.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
