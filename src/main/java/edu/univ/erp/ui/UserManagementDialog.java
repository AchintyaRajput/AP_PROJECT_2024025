package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;


public class UserManagementDialog extends JDialog {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
    private final JTextField nameField = new JTextField();
    private final JTextField programDeptField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField yearField = new JTextField();

    private final AdminService adminService = new AdminService();

    public UserManagementDialog(Frame owner, User currentAdmin) {
        super(owner, "Add User", true);
        setSize(480, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        JPanel main = new JPanel();
        main.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        main.setLayout(new GridLayout(0,2,8,8));

        main.add(new JLabel("Username:")); main.add(usernameField);
        main.add(new JLabel("Password:")); main.add(passwordField);
        main.add(new JLabel("Role:")); main.add(roleCombo);
        main.add(new JLabel("Full name:")); main.add(nameField);
        main.add(new JLabel("Program / Department:")); main.add(programDeptField);
        main.add(new JLabel("Email:")); main.add(emailField);
        main.add(new JLabel("Year (students only):")); main.add(yearField);

        add(main, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton create = new JButton("Create");
        JButton cancel = new JButton("Cancel");
        south.add(cancel); south.add(create);
        add(south, BorderLayout.SOUTH);

        create.addActionListener(e -> onCreate(currentAdmin));
        cancel.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(create);
    }

    private void onCreate(User currentAdmin) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();
        String fullname = nameField.getText().trim();
        String programDept = programDeptField.getText().trim();
        String email = emailField.getText().trim();
        Integer year = null;
        try {
            if (!yearField.getText().trim().isEmpty()) year = Integer.parseInt(yearField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a number", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.isEmpty() || password.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this, "Please fill username, password and role", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = adminService.createUser(username, password, role, fullname, email, programDept, year);
        if (id > 0) {
            JOptionPane.showMessageDialog(this, "User created with user_id = " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create user (maybe duplicate username).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
