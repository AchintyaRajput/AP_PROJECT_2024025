package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * ManageUsersUI as a JPanel inside AdminDashboard (CardLayout)
 * - Uses parent Frame for dialogs via SwingUtilities.getWindowAncestor(this)
 * - No JFrame creation here
 */
public class ManageUsersUI extends JPanel {

    private final AdminService adminService = new AdminService();
    private UserTableModel tableModel;
    private JTable userTable;

    private final User currentAdmin;

    public ManageUsersUI(User currentAdmin) {
        this.currentAdmin = currentAdmin;

        initUI();
        loadUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ==========================
        // TOP ACTION BAR
        // ==========================
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("Add User");
        JButton btnDelete = new JButton("Delete User");
        JButton btnRefresh = new JButton("Refresh");

        styleButton(btnAdd);
        styleButton(btnDelete);
        styleButton(btnRefresh);

        topPanel.add(btnAdd);
        topPanel.add(btnDelete);
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // ==========================
        // TABLE
        // ==========================
        tableModel = new UserTableModel(null);
        userTable = new JTable(tableModel);
        userTable.setRowHeight(24);

        JScrollPane scroller = new JScrollPane(userTable);
        scroller.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        add(scroller, BorderLayout.CENTER);

        // ==========================
        // BUTTON ACTIONS
        // ==========================

        // ADD USER
        btnAdd.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            if (parentWindow instanceof Frame frame) {
                new AddUserDialog(frame, adminService).setVisible(true);
            } else {
                new AddUserDialog(null, adminService).setVisible(true);
            }

            loadUsers();
        });

        // DELETE USER
        btnDelete.addActionListener(e -> {
            int selected = userTable.getSelectedRow();
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            if (selected == -1) {
                JOptionPane.showMessageDialog(parentWindow, "Select a user to delete.");
                return;
            }

            AdminService.UserRow user = tableModel.getUserAt(selected);

            // Prevent deleting own admin account
            if (user.id == currentAdmin.getUserId()) {
                JOptionPane.showMessageDialog(parentWindow,
                        "You cannot delete your own admin account.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    parentWindow,
                    "Are you sure you want to delete user '" + user.username + "'?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = adminService.deleteUser(user.id, user.role);
                if (ok) {
                    JOptionPane.showMessageDialog(parentWindow, "User deleted.");
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(parentWindow, "Failed to delete user.");
                }
            }
        });

        // REFRESH
        btnRefresh.addActionListener(e -> loadUsers());
    }

    // ==========================
    // STYLE HELPERS
    // ==========================
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(205, 235, 205));
        btn.setForeground(Color.BLACK);
        btn.setPreferredSize(new Dimension(130, 32));
    }

    // ==========================
    // PUBLIC LOAD METHOD
    // ==========================
    public void loadUsers() {
        List<AdminService.UserRow> list = adminService.getAllUsers();
        tableModel.setUsers(list);
    }
}
