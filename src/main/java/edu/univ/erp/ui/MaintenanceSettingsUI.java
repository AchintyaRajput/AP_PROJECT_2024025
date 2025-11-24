package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;

import javax.swing.*;
import java.awt.*;

public class MaintenanceSettingsUI extends JFrame {

    private JLabel statusLabel;

    public MaintenanceSettingsUI() {

        setTitle("Maintenance Mode Settings");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ===== TITLE =====
        JLabel title = new JLabel("Maintenance Mode Control", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // ===== STATUS LABEL =====
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(statusLabel, BorderLayout.CENTER);

        updateStatusLabel();

        // ===== BUTTONS =====
        JPanel bottom = new JPanel(new FlowLayout());

        JButton btnEnable = new JButton("Enable Maintenance Mode");
        JButton btnDisable = new JButton("Disable Maintenance Mode");

        bottom.add(btnEnable);
        bottom.add(btnDisable);

        add(bottom, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        btnEnable.addActionListener(e -> toggleMode(true));
        btnDisable.addActionListener(e -> toggleMode(false));
    }

    private void updateStatusLabel() {
        boolean on = DatabaseConnection.isMaintenanceOn();
        statusLabel.setText("Maintenance Mode is: " + (on ? "ON" : "OFF"));
        statusLabel.setForeground(on ? Color.RED : new Color(0, 128, 0));
    }

    private void toggleMode(boolean enable) {
        boolean ok = DatabaseConnection.setMaintenance(enable);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Maintenance Mode has been " + (enable ? "ENABLED" : "DISABLED"));
            updateStatusLabel();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update maintenance mode.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
