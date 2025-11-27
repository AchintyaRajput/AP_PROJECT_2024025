package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MaintenanceSettingsUI extends JDialog {

    private JLabel statusLabel;

    public MaintenanceSettingsUI() {

        super((Frame) null, "Maintenance Mode Settings", true);

        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);

        setSize(650, 380);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 25, 20, 25));
        add(root);

        
        JLabel title = new JLabel("Maintenance Mode Control");
        title.setFont(new Font("Inter", Font.BOLD, 22));
        title.setForeground(new Color(40, 40, 40));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        root.add(title, BorderLayout.NORTH);

        
        JPanel card = new JPanel();
        card.setBackground(new Color(238, 247, 233)); 
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        root.add(card, BorderLayout.CENTER);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Inter", Font.BOLD, 18));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(20));

        updateStatusLabel();

        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        btnPanel.setBackground(new Color(238, 247, 233));

        JButton btnEnable = new JButton("Enable");
        JButton btnDisable = new JButton("Disable");

        styleGreenButton(btnEnable);
        styleWhiteButton(btnDisable);

        btnEnable.addActionListener(e -> toggleMode(true));
        btnDisable.addActionListener(e -> toggleMode(false));

        btnPanel.add(btnEnable);
        btnPanel.add(btnDisable);

        card.add(btnPanel);
    }

    
    private void styleGreenButton(JButton btn) {
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setFont(new Font("Inter", Font.BOLD, 15));
        btn.setBackground(new Color(139, 195, 74));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
    }

    private void styleWhiteButton(JButton btn) {
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setFont(new Font("Inter", Font.BOLD, 15));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(new LineBorder(new Color(180, 180, 180)));
        btn.setFocusPainted(false);
    }

    
    private void updateStatusLabel() {
        boolean on = DatabaseConnection.isMaintenanceOn();

        statusLabel.setText(
                "Maintenance Mode is: " + (on ? "ON" : "OFF")
        );

        statusLabel.setForeground(on ? new Color(200, 0, 0) : new Color(0, 128, 0));
    }

    private void toggleMode(boolean enable) {
        boolean ok = DatabaseConnection.setMaintenance(enable);

        if (ok) {
            JOptionPane.showMessageDialog(
                    this,
                    "Maintenance mode has been " + (enable ? "ENABLED" : "DISABLED"),
                    "Maintenance Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
            updateStatusLabel();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to update maintenance mode.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
