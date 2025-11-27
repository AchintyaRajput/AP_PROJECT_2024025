package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class GradeEntryUI extends JFrame {

    private final int enrollmentId;
    private final String studentName;

    private final InstructorService instructorService = new InstructorService();

    private JTable table;
    private DefaultTableModel model;

    
    private JTextField txtComponent;
    private JTextField txtScore;
    private JTextField txtMaxMarks;
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

        
        model = new DefaultTableModel(new Object[]{
                "Grade ID", "Component", "Score", "Max Marks", "Weight (%)", "Weighted Score"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) loadSelectedIntoForm();
            }
        });

        
        JPanel bottomWrapper = new JPanel();
        bottomWrapper.setLayout(new BorderLayout());

        
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtComponent = new JTextField();
        txtScore = new JTextField();
        txtMaxMarks = new JTextField();
        txtWeight = new JTextField();

        int col = 0;

        gbc.gridx = col; gbc.gridy = 0; form.add(new JLabel("Component:"), gbc);
        gbc.gridx = col + 1; gbc.weightx = 0.3; form.add(txtComponent, gbc);

        gbc.gridx = col + 2; gbc.weightx = 0; form.add(new JLabel("Score:"), gbc);
        gbc.gridx = col + 3; gbc.weightx = 0.15; form.add(txtScore, gbc);

        gbc.gridx = col + 4; gbc.weightx = 0; form.add(new JLabel("Max Marks:"), gbc);
        gbc.gridx = col + 5; gbc.weightx = 0.15; form.add(txtMaxMarks, gbc);

        
        gbc.gridx = col; gbc.gridy = 1; gbc.weightx = 0; form.add(new JLabel("Weight (%):"), gbc);
        gbc.gridx = col + 1; gbc.gridwidth = 1; gbc.weightx = 0.15; form.add(txtWeight, gbc);

        bottomWrapper.add(form, BorderLayout.CENTER);

        
        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRecalc = new JButton("Recalculate Final Grade");
        JButton btnSave = new JButton("Save / Update");
        JButton btnRefresh = new JButton("Refresh");

        bottomButtons.add(btnRecalc);
        bottomButtons.add(btnSave);
        bottomButtons.add(btnRefresh);

        bottomWrapper.add(bottomButtons, BorderLayout.SOUTH);

        add(bottomWrapper, BorderLayout.SOUTH);

        
        btnRecalc.addActionListener(e -> recalculateFinalGrade());
        btnSave.addActionListener(e -> saveGrade());
        btnRefresh.addActionListener(e -> loadGrades());
    }

    private void loadGrades() {
        model.setRowCount(0);

        List<InstructorService.GradeRow> list =
                instructorService.getGradesForEnrollment(enrollmentId);

        for (InstructorService.GradeRow g : list) {
            double weighted = 0.0;
            if (g.maxMarks > 0) {
                weighted = (g.score / g.maxMarks) * g.weight;
            }
            model.addRow(new Object[]{
                    g.gradeId,
                    g.component,
                    g.score,
                    g.maxMarks,
                    g.weight,
                    String.format("%.2f", weighted)
            });
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        
        txtComponent.setText(model.getValueAt(row, 1).toString());
        txtScore.setText(model.getValueAt(row, 2).toString());
        txtMaxMarks.setText(model.getValueAt(row, 3).toString());
        txtWeight.setText(model.getValueAt(row, 4).toString());
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
            double maxMarks = Double.parseDouble(txtMaxMarks.getText().trim());
            double weight = Double.parseDouble(txtWeight.getText().trim());

            if (component.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Component cannot be empty.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            
            boolean ok = instructorService.saveGrade(enrollmentId, component, score, weight, maxMarks);

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
                    "Score, Max Marks and Weight must be numbers.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recalculateFinalGrade() {
        
        boolean ok = instructorService.calculateFinalGrade(enrollmentId);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Final grade recalculated and saved.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadGrades();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to calculate final grade.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
