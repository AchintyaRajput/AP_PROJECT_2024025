package edu.univ.erp.ui;

import edu.univ.erp.auth.LoginService;
import edu.univ.erp.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Improved Login UI: centered layout + non-flicker purple button
 */
public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    // --- ONLY CHANGED PARTS — KEEP ALL YOUR OLD CODE ABOVE THIS CLASS ---
// Replace your constructor with THIS improved version:

    public LoginUI() {

        setTitle("ERP Login");
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        add(root);

        // =============================
        // LEFT IMAGE
        // =============================
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image img = new ImageIcon(
                        getClass().getResource("/images/college.jpg")
                ).getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };

        leftPanel.setPreferredSize(new Dimension(520, 650));
        root.add(leftPanel, BorderLayout.WEST);

        // =============================
        // RIGHT LOGIN PANEL
        // =============================
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        root.add(wrapper, BorderLayout.CENTER);

        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(new EmptyBorder(20, 100, 20, 100));

        wrapper.add(loginPanel);

        // =============================
        // TITLE
        // =============================
        JLabel title = new JLabel("Sign In", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 42));   // Bigger
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(title);
        loginPanel.add(Box.createVerticalStrut(40));

        // =============================
        // USERNAME LABEL + FIELD
        // =============================
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 18)); // bigger
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(420, 45));
        usernameField.setMaximumSize(new Dimension(420, 45));
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        loginPanel.add(usernameField);

        loginPanel.add(Box.createVerticalStrut(25));

        // =============================
        // PASSWORD LABEL + FIELD
        // =============================
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(420, 45));
        passwordField.setMaximumSize(new Dimension(420, 45));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        loginPanel.add(passwordField);

        loginPanel.add(Box.createVerticalStrut(10));

        // =============================
        // FORGOT PASSWORD (one line, left)
        // =============================
        JLabel forgot = new JLabel("<HTML><U>Forgot Password?</U></HTML>");
        forgot.setFont(new Font("SansSerif", Font.PLAIN, 16));
        forgot.setForeground(new Color(120, 60, 230)); // purple
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(forgot);

        loginPanel.add(Box.createVerticalStrut(25));

        // =============================
        // ERROR MESSAGE LABEL
        // =============================
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(messageLabel);

        loginPanel.add(Box.createVerticalStrut(20));

        // =============================
        // LOGIN BUTTON (BIG PURPLE)
        // =============================
        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(420, 50));
        loginBtn.setMaximumSize(new Dimension(420, 50));
        loginBtn.setBackground(new Color(120, 60, 230));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(loginBtn);

        loginBtn.addActionListener(e -> doLogin());

        loginPanel.add(Box.createVerticalStrut(30));

        // =============================
        // SIGN UP (big, purple, centered)
        // =============================
        JLabel signup = new JLabel("<HTML><U>Create an Account</U></HTML>");
        signup.setFont(new Font("SansSerif", Font.BOLD, 18));
        signup.setForeground(new Color(120, 60, 230));
        signup.setAlignmentX(Component.CENTER_ALIGNMENT);
        signup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginPanel.add(signup);

        getRootPane().setDefaultButton(loginBtn);
    }

    // ================================
    // LOGIN LOGIC (WITH LOCKOUT)
    // ================================
    private void doLogin() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        if (LoginService.isBlocked(username)) {
            messageLabel.setText("Too many failed attempts. Try again later.");
            return;
        }

        LoginService service = new LoginService();
        User user = service.login(username, password);

        if (user != null) {
            messageLabel.setForeground(new Color(0, 140, 0));
            messageLabel.setText("Login successful!");

            SwingUtilities.invokeLater(() -> {
                switch (user.getRole().toLowerCase()) {
                    case "admin" -> new AdminDashboard(user).setVisible(true);
                    case "student" -> new StudentDashboard(user).setVisible(true);
                    case "instructor" -> new InstructorDashboard(user).setVisible(true);
                }
                dispose();
            });

        } else {
            int left = 5 - LoginService.getFailCount(username);
            messageLabel.setText("Invalid login. Attempts left: " + left);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
