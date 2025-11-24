package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.SectionService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * UI for managing sections (Admin).
 */
public class ManageSectionsUI extends JFrame {

    private final SectionService sectionService = new SectionService();
    private SectionTableModel tableModel;
    private JTable table;
    private final User currentAdmin;

    public ManageSectionsUI(User admin) {
        this.currentAdmin = admin;

        setTitle("Manage Sections");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadSections();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== TOP BUTTON PANEL =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnAdd = new JButton("Add Section");
        JButton btnEdit = new JButton("Edit Section");
        JButton btnDelete = new JButton("Delete Section");
        JButton btnRefresh = new JButton("Refresh");

        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        tableModel = new SectionTableModel(null);
        table = new JTable(tableModel);
        table.setRowHeight(24);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BUTTON ACTIONS =====

        // Add Section
        btnAdd.addActionListener(e -> {
            new AddSectionDialog(this, sectionService, null).setVisible(true);
            loadSections();
        });

        // Edit Section
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a section to edit.");
                return;
            }
            var section = tableModel.getSectionAt(row);
            new AddSectionDialog(this, sectionService, section).setVisible(true);
            loadSections();
        });

        // Delete Section
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a section to delete.");
                return;
            }

            var section = tableModel.getSectionAt(row);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete section " + section.id + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                boolean ok = sectionService.deleteSection(section.id);

                if (!ok) {
                    JOptionPane.showMessageDialog(
                            this,
                            "❌ Cannot delete this section.\nStudents are currently enrolled.",
                            "Delete Blocked",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Section deleted successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

                loadSections();
            }
        });

        // Refresh
        btnRefresh.addActionListener(e -> loadSections());
    }

    // Load all sections into the table
    private void loadSections() {
        List<SectionService.SectionRow> list = sectionService.getAllSections();
        tableModel.setSections(list);
    }
}
