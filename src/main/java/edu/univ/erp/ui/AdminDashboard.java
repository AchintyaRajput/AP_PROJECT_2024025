package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.BackupService;
import edu.univ.erp.data.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutionException;


public class AdminDashboard extends JFrame {

    private final User currentAdmin;

    
    private static final String MYSQL_BIN = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Avi@2006";
    private static final String DB_NAME = "erp_db";

    
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    
    private ManageUsersUI usersPanel;
    private ManageCoursesUI coursesPanel;
    private ManageSectionsUI sectionsPanel;
    private JPanel backupPanel;
    private JPanel homePanel;

    
    private final JPopupMenu notifPopup = new JPopupMenu();

    public AdminDashboard(User admin) {
        this.currentAdmin = admin;

        
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);

        setTitle("University ERP — Admin Dashboard (" + admin.getUsername() + ")");
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(12, 18, 12, 18));
        topBar.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("University ERP — Admin Dashboard");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 30, 30));

        
        JButton bellBtn = new JButton("\uD83D\uDD14");
        bellBtn.setFont(new Font("Inter", Font.PLAIN, 22));
        bellBtn.setFocusPainted(false);
        bellBtn.setContentAreaFilled(false);
        bellBtn.setBorder(null);
        bellBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellBtn.addActionListener(e -> toggleNotifications());

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(bellBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(225, 245, 225));
        sidebar.setBorder(new EmptyBorder(20, 14, 20, 14));
        sidebar.setPreferredSize(new Dimension(240, getHeight()));

         
        JLabel adminLabel = new JLabel("<html><b>" + escapeHtml(currentAdmin.getUsername()) + "</b><br/><small>Admin</small></html>");
        adminLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        adminLabel.setBorder(new EmptyBorder(6, 6, 18, 6));
        sidebar.add(adminLabel);

        
        sidebar.add(makeSidebarButton("Dashboard Home", e -> showHome()));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(makeSidebarButton("Manage Users", e -> showUsers()));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(makeSidebarButton("Manage Courses", e -> showCourses()));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(makeSidebarButton("Manage Sections", e -> showSections()));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(makeSidebarButton("Backup & Restore", e -> showBackup()));
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(makeSidebarButton("Change Password", e -> openChangePassword()));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(makeSidebarButton("Maintenance Mode", e -> openMaintenance()));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(makeSidebarButton("Logout", e -> {

            
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor((Component) e.getSource());

            if (frame != null) {
                frame.dispose(); 
            }

            
            SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
        }));


        add(sidebar, BorderLayout.WEST);

        
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(cardPanel, BorderLayout.CENTER);

        
        buildPanels();

        
        showHome();

        setVisible(true);
    }

    
    private void buildPanels() {
        
        homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());
        homePanel.setBackground(Color.WHITE);

        JLabel welcome = new JLabel("Welcome, " + currentAdmin.getUsername(), SwingConstants.LEFT);
        welcome.setFont(new Font("Inter", Font.BOLD, 20));
        welcome.setBorder(new EmptyBorder(6, 6, 20, 6));

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFont(new Font("Inter", Font.PLAIN, 14));
        info.setText("Use the left sidebar to navigate administrative actions.\n\n" +
                "Manage users, courses and sections. Backup and restore the database from the Backup & Restore panel.\n\n" +
                "Change Password and Maintenance Mode open as small dialogs.");

        info.setBackground(new Color(245, 247, 248));
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                new EmptyBorder(12,12,12,12)
        ));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);
        top.add(welcome);
        top.add(info);

        homePanel.add(top, BorderLayout.NORTH);

        cardPanel.add(homePanel, "home");

        
        usersPanel = new ManageUsersUI(currentAdmin);
        cardPanel.add(usersPanel, "users");

        
        coursesPanel = new ManageCoursesUI(currentAdmin);
        cardPanel.add(coursesPanel, "courses");

       
        sectionsPanel = new ManageSectionsUI(currentAdmin);
        cardPanel.add(sectionsPanel, "sections");

        
        backupPanel = buildBackupPanel();
        cardPanel.add(backupPanel, "backup");
    }

    
    private JButton makeSidebarButton(String text, java.awt.event.ActionListener a) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(205, 235, 205));
        btn.setForeground(Color.BLACK);
        btn.addActionListener(a);
        return btn;
    }

   
    private void showHome() {
        cardLayout.show(cardPanel, "home");
    }

    private void showUsers() {
        cardLayout.show(cardPanel, "users");
        
        SwingUtilities.invokeLater(() -> {
            try { usersPanel.loadUsers(); } catch (Exception ignored) {}
        });
    }

    private void showCourses() {
        cardLayout.show(cardPanel, "courses");
        SwingUtilities.invokeLater(() -> {
            try { coursesPanel.loadCourses(); } catch (Exception ignored) {}
        });
    }

    private void showSections() {
        cardLayout.show(cardPanel, "sections");
        SwingUtilities.invokeLater(() -> {
            try { sectionsPanel.loadSections(); } catch (Exception ignored) {}
        });
    }

    private void showBackup() {
        cardLayout.show(cardPanel, "backup");
    }

    
    private void openChangePassword() {
        ChangePasswordUI dlg = new ChangePasswordUI(currentAdmin);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    
    private void openMaintenance() {
        MaintenanceSettingsUI dlg = new MaintenanceSettingsUI();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    
    private void toggleNotifications() {
        notifPopup.removeAll();

        
        boolean loaded = false;
        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT message, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 8"
             )) {

            ps.setInt(1, currentAdmin.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String msg = rs.getString(1);
                String when = rs.getString(2);
                JMenuItem it = new JMenuItem("<html>" + escapeHtml(msg) + "<br/><small>" + when + "</small></html>");
                it.setEnabled(false);
                notifPopup.add(it);
                loaded = true;
            }
        } catch (Exception ignored) {
            
        }

        if (!loaded) {
            JMenuItem it = new JMenuItem("No notifications");
            it.setEnabled(false);
            notifPopup.add(it);
        }

       
        Point p = this.getLocationOnScreen();
        notifPopup.show(this, this.getWidth() - 260, 52);
    }

   
    private JPanel buildBackupPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        JLabel t = new JLabel("Backup & Restore");
        t.setFont(new Font("Inter", Font.BOLD, 20));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(12));

        JPanel card = new JPanel();
        card.setBackground(new Color(238, 247, 233));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html>Use the controls below to create a MySQL dump backup or restore from an existing SQL file.<br/>" +
                "Restoring will overwrite current data — please ensure you have a backup.</html>");
        desc.setFont(new Font("Inter", Font.PLAIN, 14));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        desc.setBorder(new EmptyBorder(0,0,12,0));
        card.add(desc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        btnRow.setBackground(new Color(238, 247, 233));
        JButton btnBackup = new JButton("Create Backup (.sql)");
        JButton btnRestore = new JButton("Restore From .sql");

        styleGreenAction(btnBackup);
        styleWhiteAction(btnRestore);

        btnRow.add(btnBackup);
        btnRow.add(btnRestore);
        card.add(btnRow);

        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Inter", Font.PLAIN, 13));
        statusArea.setBackground(new Color(245, 247, 248));
        statusArea.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        statusArea.setText("Last action status will appear here.");
        statusArea.setRows(6);
        card.add(Box.createVerticalStrut(12));
        card.add(new JScrollPane(statusArea));

        
        btnBackup.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save ERP DB backup (.sql)");
            chooser.setSelectedFile(new File("erp_db_backup.sql"));

            int res = chooser.showSaveDialog(this);
            if (res != JFileChooser.APPROVE_OPTION) return;

            File outFile = chooser.getSelectedFile();
            BackupService service = new BackupService(MYSQL_BIN, DB_USER, DB_PASS, DB_NAME);

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
                            statusArea.setText("Backup completed: " + r.message);
                            JOptionPane.showMessageDialog(AdminDashboard.this, r.message, "Backup Completed", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            statusArea.setText("Backup failed: " + r.message);
                            JOptionPane.showMessageDialog(AdminDashboard.this, r.message, "Backup Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        statusArea.setText("Backup failed: " + ex.getMessage());
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Backup failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
            dlg.setVisible(true);
        });

        btnRestore.addActionListener(e -> {
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
                            statusArea.setText("Restore completed: " + r.message);
                            JOptionPane.showMessageDialog(AdminDashboard.this, r.message, "Restore Completed", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            statusArea.setText("Restore failed: " + r.message);
                            JOptionPane.showMessageDialog(AdminDashboard.this, r.message, "Restore Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        statusArea.setText("Restore failed: " + ex.getMessage());
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Restore failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
            dlg.setVisible(true);
        });

        p.add(card);
        return p;
    }

    
    private JDialog createProgressDialog(String message) {
        JDialog dlg = new JDialog(this, true);
        dlg.setUndecorated(true);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.NORTH);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        panel.add(bar, BorderLayout.CENTER);
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        return dlg;
    }

  
    private void styleGreenAction(JButton b) {
        b.setPreferredSize(new Dimension(180, 40));
        b.setFont(new Font("Inter", Font.BOLD, 14));
        b.setBackground(new Color(139, 195, 74));
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
    }

    private void styleWhiteAction(JButton b) {
        b.setPreferredSize(new Dimension(180, 40));
        b.setFont(new Font("Inter", Font.BOLD, 14));
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        b.setFocusPainted(false);
    }

  
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br/>");
    }
}
