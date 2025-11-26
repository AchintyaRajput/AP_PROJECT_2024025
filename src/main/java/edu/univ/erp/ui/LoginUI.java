package edu.univ.erp.ui;

import edu.univ.erp.auth.LoginService;
import edu.univ.erp.domain.User;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginUI() {

        // ==========================================
        // THEME SETUP
        // ==========================================
        FlatLightLaf.setup();

        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);

        setTitle("ERP Login");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        add(root);

        // ==========================================
        // LEFT SIDE IMAGE
        // ==========================================
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    Image img = new ImageIcon(
                            getClass().getResource("/images/college.jpg")
                    ).getImage();
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception ignored) {}
            }
        };
        leftPanel.setPreferredSize(new Dimension(480, 650));
        root.add(leftPanel, BorderLayout.WEST);

        // ==========================================
        // RIGHT SIDE PANEL (Left aligned form)
        // ==========================================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        root.add(rightPanel, BorderLayout.CENTER);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(40, 80, 40, 40));

        rightPanel.add(content, BorderLayout.NORTH);

        // ==========================================
        // TITLE (outside the green box)
        // ==========================================
        JLabel title = new JLabel("University ERP");
        title.setFont(new Font("Inter", Font.BOLD, 40));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Login to continue");
        subtitle.setFont(new Font("Inter", Font.PLAIN, 16));
        subtitle.setForeground(new Color(100, 100, 100));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(5));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(25));

        // ==========================================
        // GREEN LOGIN CARD BOX
        // ==========================================
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(238, 247, 233));          // light green shade
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(430, Integer.MAX_VALUE));
        card.setBorder(new LineBorder(new Color(210, 230, 210), 2, true));

        content.add(card);
        content.add(Box.createVerticalStrut(15));

        // ==========================================
        // USERNAME
        // ==========================================
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Inter", Font.PLAIN, 15));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(350, 42));
        usernameField.setMaximumSize(new Dimension(350, 42));
        usernameField.setFont(new Font("Inter", Font.PLAIN, 16));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        card.add(userLabel);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(20));

        // ==========================================
        // PASSWORD
        // ==========================================
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Inter", Font.PLAIN, 15));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(350, 42));
        passwordField.setMaximumSize(new Dimension(350, 42));
        passwordField.setFont(new Font("Inter", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        card.add(passLabel);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));

        // ==========================================
        // FORGOT PASSWORD
        // ==========================================
        JLabel forgot = new JLabel("<html><u>Forgot password?</u></html>");
        forgot.setFont(new Font("Inter", Font.PLAIN, 14));
        forgot.setForeground(new Color(20, 130, 80));
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(forgot);
        card.add(Box.createVerticalStrut(18));

        // ==========================================
        // MESSAGE LABEL (errors)
        // ==========================================
        messageLabel = new JLabel("", SwingConstants.LEFT);
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(messageLabel);
        card.add(Box.createVerticalStrut(20));

        // ==========================================
        // SIGN IN BUTTON
        // ==========================================
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setPreferredSize(new Dimension(350, 45));
        loginBtn.setMaximumSize(new Dimension(350, 45));
        loginBtn.setFont(new Font("Inter", Font.BOLD, 18));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setBackground(new Color(139, 195, 74));
        loginBtn.setFocusPainted(false);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> doLogin());

        card.add(loginBtn);
        card.add(Box.createVerticalStrut(22));

        // ==========================================
        // CREATE ACCOUNT LINK
        // ==========================================
        JLabel signup = new JLabel("<html><u>Create an account</u></html>");
        signup.setFont(new Font("Inter", Font.BOLD, 14));
        signup.setForeground(new Color(20, 130, 80));
        signup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signup.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(signup);

        getRootPane().setDefaultButton(loginBtn);
    }

    // =====================================================
    // LOGIN LOGIC
    // =====================================================
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
            messageLabel.setForeground(new Color(0, 120, 0));
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
