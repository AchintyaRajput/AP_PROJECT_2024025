package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.BackupService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * AdminDashboard with Backup / Restore buttons using BackupService (mysqldump).
 *
 * NOTE: This version uses the MySQL settings you provided:
 * MYSQL_BIN = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin"
 * DB_USER = "root"
 * DB_PASS = "avi@2006"
 * DB_NAME = "erp_db"
 *
 * If you prefer to read these from a config or env vars, I can change it.
 */
public class AdminDashboard extends JFrame {

    private final User currentAdmin;

    // ---- Replace these with your config or env if desired ----
    private static final String MYSQL_BIN = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Avi@2006";
    private static final String DB_NAME = "erp_db";
    // ----------------------------------------------------------

    public AdminDashboard(User admin) {
        this.currentAdmin = admin;

        setTitle("Admin Dashboard - " + admin.getUsername());
        setSize(950, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== HEADER =====
        JLabel heading = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        // ===== LEFT: Notification Panel (keep if exists) =====
        NotificationPanel notifications = new NotificationPanel(admin);
        add(notifications, BorderLayout.WEST);

        // ===== CENTER BUTTON PANEL =====
        JPanel center = new JPanel(new GridLayout(8, 1, 20, 20));
        center.setBorder(BorderFactory.createEmptyBorder(30, 120, 30, 120));

        JButton btnUsers = new JButton("Manage Users");
        JButton btnCourses = new JButton("Manage Courses");
        JButton btnSections = new JButton("Manage Sections");
        JButton btnMaintenance = new JButton("Maintenance Mode Settings");
        JButton btnBackup = new JButton("Backup ERP DB");
        JButton btnRestore = new JButton("Restore ERP DB");
        JButton btnChangePass = new JButton("Change Password");
        JButton btnLogout = new JButton("Logout");

        center.add(btnUsers);
        center.add(btnCourses);
        center.add(btnSections);
        center.add(btnMaintenance);
        center.add(btnBackup);
        center.add(btnRestore);
        center.add(btnChangePass);
        center.add(btnLogout);

        add(center, BorderLayout.CENTER);

        // ACTIONS
        btnUsers.addActionListener(e -> new ManageUsersUI(currentAdmin).setVisible(true));
        btnCourses.addActionListener(e -> new ManageCoursesUI(currentAdmin).setVisible(true));
        btnSections.addActionListener(e -> new ManageSectionsUI(currentAdmin).setVisible(true));

        btnMaintenance.addActionListener(e -> new MaintenanceSettingsUI().setVisible(true));

        btnChangePass.addActionListener(e -> new ChangePasswordUI(currentAdmin).setVisible(true));

        btnLogout.addActionListener(e -> {
            dispose();
            JOptionPane.showMessageDialog(null, "Logged out successfully.");
        });

        // ===== BACKUP & RESTORE ACTIONS =====
        btnBackup.addActionListener(e -> doBackup());
        btnRestore.addActionListener(e -> doRestore());

        setVisible(true);
    }

    // --------------------------
    // BACKUP FLOW (SwingWorker)
    // --------------------------
    private void doBackup() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save ERP DB backup (.sql)");
        chooser.setSelectedFile(new File("erp_db_backup.sql"));

        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File outFile = chooser.getSelectedFile();

        BackupService service = new BackupService(MYSQL_BIN, DB_USER, DB_PASS, DB_NAME);

        // Progress dialog
        JDialog dlg = createProgressDialog("Backing up database...");

        SwingWorker<BackupService.Result, Void> worker = new SwingWorker<>() {
            @Override
            protected BackupService.Result doInBackground() {
                try {
                    return service.backup(outFile);
                } catch (Exception ex) {
                    return new BackupService.Result(false, "Exception: " + ex.getMessage());
                }
            }

            @Override
            protected void done() {
                dlg.dispose();
                try {
                    BackupService.Result r = get();
                    if (r.success) {
                        JOptionPane.showMessageDialog(AdminDashboard.this, r.message, "Backup Completed", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AdminDashboard.this, r.message, "Backup Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Backup failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        dlg.setVisible(true);
    }

    // --------------------------
    // RESTORE FLOW (SwingWorker)
    // --------------------------
    private void doRestore() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose backup SQL file to restore from");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File sqlFile = chooser.getSelectedFile();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Restore will overwrite data in database '" + DB_NAME + "'.\nMake sure you have a backup. Continue?",
                "Confirm Restore",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        BackupService service = new BackupService(MYSQL_BIN, DB_USER, DB_PASS, DB_NAME);

        JDialog dlg = createProgressDialog("Restoring database...");

        SwingWorker<BackupService.Result, Void> worker = new SwingWorker<>() {
            @Override
            protected BackupService.Result doInBackground() {
                try {
                    return service.restore(sqlFile);
                } catch (Exception ex) {
                    return new BackupService.Result(false, "Exception: " + ex.getMessage());
                }
            }

            @Override
            protected void done() {
                dlg.dispose();
                try {
                    BackupService.Result r = get();
                    if (r.success) {
                        JOptionPane.showMessageDialog(AdminDashboard.this, r.message, "Restore Completed", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AdminDashboard.this, r.message, "Restore Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Restore failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        dlg.setVisible(true);
    }

    private JDialog createProgressDialog(String message) {
        JDialog dlg = new JDialog(this, true);
        dlg.setUndecorated(true);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        panel.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.NORTH);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        panel.add(bar, BorderLayout.CENTER);
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        return dlg;
    }
}
