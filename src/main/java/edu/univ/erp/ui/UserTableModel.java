package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class UserTableModel extends AbstractTableModel {

    private final String[] columnNames = {"User ID", "Username", "Role", "Status"};
    private List<AdminService.UserRow> users;

    public UserTableModel(List<AdminService.UserRow> users) {
        this.users = users;
    }

    public void setUsers(List<AdminService.UserRow> users) {
        this.users = users;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return users == null ? 0 : users.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int row, int col) {
        AdminService.UserRow user = users.get(row);
        return switch (col) {
            case 0 -> user.id;
            case 1 -> user.username;
            case 2 -> user.role;
            case 3 -> user.status;
            default -> null;
        };
    }

    public AdminService.UserRow getUserAt(int row) {
        return users.get(row);
    }
}
