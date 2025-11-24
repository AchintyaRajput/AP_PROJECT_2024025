package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ManageUsersUI extends JFrame {

    private final AdminService adminService = new AdminService();
    private UserTableModel tableModel;
    private JTable userTable;

    private final User currentAdmin;

    public ManageUsersUI(User currentAdmin) {
        this.currentAdmin = currentAdmin;

        setTitle("Manage Users - Admin");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== TOP PANEL (Buttons) =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Add User");
        JButton btnDelete = new JButton("Delete User");
        JButton btnRefresh = new JButton("Refresh");

        topPanel.add(btnAdd);
        topPanel.add(btnDelete);
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        tableModel = new UserTableModel(null);
        userTable = new JTable(tableModel);
        userTable.setRowHeight(24);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // ===== BUTTON ACTIONS =====

        // Add User
        btnAdd.addActionListener(e -> {
            new AddUserDialog(this, adminService).setVisible(true);
            loadUsers();
        });

        // Delete User
        btnDelete.addActionListener(e -> {
            int selected = userTable.getSelectedRow();
            if (selected == -1) {
                JOptionPane.showMessageDialog(this, "Select a user to delete.");
                return;
            }

            AdminService.UserRow user = tableModel.getUserAt(selected);

            // Prevent self-delete
            if (user.id == currentAdmin.getUserId()) {
                JOptionPane.showMessageDialog(this, "You cannot delete your own admin account.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete user '" + user.username + "'?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = adminService.deleteUser(user.id, user.role);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "User deleted.");
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete user.");
                }
            }
        });

        // Refresh
        btnRefresh.addActionListener(e -> loadUsers());
    }


    // ===== Load Users into Table =====
    private void loadUsers() {
        List<AdminService.UserRow> list = adminService.getAllUsers();
        tableModel.setUsers(list);
    }
}
