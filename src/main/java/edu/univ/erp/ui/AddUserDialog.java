package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;

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

    public AddUserDialog(Window owner, AdminService adminService) {
        super(owner, "Add New User", ModalityType.APPLICATION_MODAL);
        this.adminService = adminService;

        setSize(600, 650);     
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {

        
        JLabel title = new JLabel("Add New User", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));  
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 1, 12, 12));
        form.setBackground(new Color(235, 245, 235));
        form.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        form.add(label("Username"));
        form.add(field(usernameField));

        form.add(label("Password"));
        form.add(field(passwordField));

        form.add(label("Full Name"));
        form.add(field(fullNameField));

        form.add(label("Role"));
        form.add(field(roleCombo));

        form.add(label("Email"));
        form.add(field(emailField));

        form.add(label("Program / Dept"));
        form.add(field(programDeptField));

        form.add(label("Year (Students Only)"));
        form.add(field(yearField));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);

        add(scroll, BorderLayout.CENTER);

        roleCombo.addActionListener(e -> {
            String role = (String) roleCombo.getSelectedItem();
            yearField.setEnabled("Student".equalsIgnoreCase(role));
        });

        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnSave = new JButton("Create");
        JButton btnCancel = new JButton("Cancel");

        styleButton(btnSave, new Color(100, 160, 60));
        styleButton(btnCancel, new Color(180, 60, 60));

        bottom.add(btnCancel);
        bottom.add(btnSave);

        add(bottom, BorderLayout.SOUTH);

       
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        return l;
    }

    private JComponent field(JComponent c) {
        c.setPreferredSize(new Dimension(300, 40));
        c.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return c;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));
    }

    private void onSave() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String program = programDeptField.getText().trim();

        Integer year = null;
        if ("Student".equalsIgnoreCase(role)) {
            try {
                year = Integer.parseInt(yearField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid year.");
                return;
            }
        }

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        int id = adminService.createUser(username, password, role, fullName, email, program, year);

        if (id > 0) {
            JOptionPane.showMessageDialog(this, "User created. User ID = " + id);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create user.");
        }
    }
}
