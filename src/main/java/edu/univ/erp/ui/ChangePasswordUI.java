package edu.univ.erp.ui;

import edu.univ.erp.auth.LoginService;
import edu.univ.erp.domain.User;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ChangePasswordUI extends JDialog {

    private final User currentUser;

    private final JPasswordField oldPassField = new JPasswordField();
    private final JPasswordField newPassField = new JPasswordField();
    private final JPasswordField confirmPassField = new JPasswordField();
    private final JLabel messageLabel = new JLabel("", SwingConstants.CENTER);

    public ChangePasswordUI(User user) {

        super((Frame) null, "Change Password", true);
        this.currentUser = user;

        
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);

        setSize(650, 470);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 25, 20, 25));
        add(root);

        
        JLabel title = new JLabel("Change Password");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(new Color(40, 40, 40));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        root.add(title, BorderLayout.NORTH);

        
        JPanel card = new JPanel();
        card.setBackground(new Color(238, 247, 233)); 
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        root.add(card, BorderLayout.CENTER);

        
        addField(card, "Old Password", oldPassField);
        addField(card, "New Password", newPassField);
        addField(card, "Confirm Password", confirmPassField);

        
        messageLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        messageLabel.setForeground(Color.RED);
        messageLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
        card.add(messageLabel);

        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        btnPanel.setBackground(new Color(238, 247, 233));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(120, 38));
        btnCancel.setFont(new Font("Inter", Font.BOLD, 14));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(Color.BLACK);
        btnCancel.setBorder(new LineBorder(new Color(180, 180, 180)));
        btnCancel.addActionListener(e -> dispose());

        JButton btnUpdate = new JButton("Update Password");
        btnUpdate.setPreferredSize(new Dimension(160, 38));
        btnUpdate.setFont(new Font("Inter", Font.BOLD, 14));
        btnUpdate.setBackground(new Color(139, 195, 74));  
        btnUpdate.setForeground(Color.BLACK);
        btnUpdate.setFocusPainted(false);
        btnUpdate.addActionListener(e -> changePassword());

        btnPanel.add(btnCancel);
        btnPanel.add(btnUpdate);

        card.add(btnPanel);
    }

    
    private void addField(JPanel container, String label, JPasswordField field) {

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Inter", Font.PLAIN, 15));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setPreferredSize(new Dimension(320, 38));
        field.setMaximumSize(new Dimension(320, 38));
        field.setFont(new Font("Inter", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(170, 170, 170)),
                new EmptyBorder(7, 10, 7, 10)
        ));

        container.add(lbl);
        container.add(field);
        container.add(Box.createVerticalStrut(12));
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

        if (!service.verifyPassword(currentUser.getUserId(), oldPass)) {
            messageLabel.setText("Old password is incorrect.");
            return;
        }

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
