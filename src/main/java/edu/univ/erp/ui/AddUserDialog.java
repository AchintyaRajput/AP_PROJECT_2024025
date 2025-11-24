package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog that allows the Admin to create a new user.
 */
public class AddUserDialog extends JDialog {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField fullNameField = new JTextField();
    private final JComboBox<String> roleCombo =
            new JComboBox<>(new String[]{"Admin", "Student", "Instructor"});

    private final JTextField emailField = new JTextField();
    private final JTextField programDeptField = new JTextField();
    private final JTextField yearField = new JTextField();

    private final AdminService adminService;

    public AddUserDialog(Frame owner, AdminService adminService) {
        super(owner, "Add New User", true);
        this.adminService = adminService;

        setSize(420, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initForm();
        initButtons();
    }

    private void initForm() {
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        // Labels + Fields
        form.add(new JLabel("Username:"));
        form.add(usernameField);

        form.add(new JLabel("Password:"));
        form.add(passwordField);

        form.add(new JLabel("Full Name:"));
        form.add(fullNameField);

        form.add(new JLabel("Role:"));
        form.add(roleCombo);

        form.add(new JLabel("Email:"));
        form.add(emailField);

        form.add(new JLabel("Program / Dept:"));
        form.add(programDeptField);

        form.add(new JLabel("Year (only students):"));
        form.add(yearField);

        // Show/hide "year" dynamically based on role
        roleCombo.addActionListener(e -> {
            String role = (String) roleCombo.getSelectedItem();
            yearField.setEnabled("Student".equalsIgnoreCase(role));
        });

        add(form, BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCreate = new JButton("Create");
        JButton btnCancel = new JButton("Cancel");

        bottom.add(btnCancel);
        bottom.add(btnCreate);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnCreate.addActionListener(e -> onCreate());
    }

    // Called when CREATE button is pressed
    private void onCreate() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String programDept = programDeptField.getText().trim();

        Integer year = null;
        if ("Student".equalsIgnoreCase(role)) {
            try {
                year = Integer.parseInt(yearField.getText().trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid year value.");
                return;
            }
        }

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields.");
            return;
        }

        int id = adminService.createUser(username, password, role, fullName, email, programDept, year);

        if (id > 0) {
            JOptionPane.showMessageDialog(this, "User created. User ID = " + id);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create user (maybe duplicate username).");
        }
    }
}
