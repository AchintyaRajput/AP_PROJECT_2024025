package edu.univ.erp.ui;

import edu.univ.erp.auth.LoginService;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordUI extends JFrame {

    private final User currentUser;

    private final JPasswordField oldPassField = new JPasswordField();
    private final JPasswordField newPassField = new JPasswordField();
    private final JPasswordField confirmPassField = new JPasswordField();
    private final JLabel messageLabel = new JLabel("", SwingConstants.CENTER);

    public ChangePasswordUI(User user) {
        this.currentUser = user;

        setTitle("Change Password");
        setSize(420, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Change Password", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(60, 60, 60));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridy++;
        panel.add(new JLabel("Old Password:"), gbc);
        gbc.gridx = 1;
        panel.add(oldPassField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        panel.add(newPassField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        panel.add(confirmPassField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        messageLabel.setForeground(Color.RED);
        panel.add(messageLabel, gbc);

        JButton btnChange = new JButton("Update Password");
        btnChange.setBackground(new Color(120, 50, 220));
        btnChange.setForeground(Color.WHITE);
        btnChange.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnChange.addActionListener(e -> changePassword());

        gbc.gridy++;
        panel.add(btnChange, gbc);

        add(panel);
    }

    private void changePassword() {
        String oldPass = new String(oldPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        messageLabel.setForeground(Color.RED);

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        if (!newPass.equals(confirm)) {
            messageLabel.setText("New passwords do not match.");
            return;
        }

        LoginService service = new LoginService();

        // Verify old password
        if (!service.verifyPassword(currentUser.getUserId(), oldPass)) {
            messageLabel.setText("Old password is incorrect.");
            return;
        }

        // Update password
        boolean updated = service.updatePassword(currentUser.getUserId(), newPass);

        if (updated) {
            JOptionPane.showMessageDialog(this,
                    "Password updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            messageLabel.setText("Failed to update password.");
        }
    }
}
