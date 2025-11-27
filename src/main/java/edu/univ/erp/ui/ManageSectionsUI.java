package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.SectionService;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class ManageSectionsUI extends JPanel {

    private final SectionService sectionService = new SectionService();
    private SectionTableModel tableModel;
    private JTable table;

    private final User currentAdmin;

    public ManageSectionsUI(User admin) {
        this.currentAdmin = admin;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        initUI();
        loadSections();
    }

    private void initUI() {

        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("Add Section");
        JButton btnEdit = new JButton("Edit Section");
        JButton btnDelete = new JButton("Delete Section");
        JButton btnRefresh = new JButton("Refresh");

        styleButton(btnAdd);
        styleButton(btnEdit);
        styleButton(btnDelete);
        styleButton(btnRefresh);

        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);

        add(top, BorderLayout.NORTH);

        
        tableModel = new SectionTableModel(null);
        table = new JTable(tableModel);
        table.setRowHeight(24);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        add(scroll, BorderLayout.CENTER);

        
        btnAdd.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            if (parentWindow instanceof Frame frame) {
                new AddSectionDialog(frame, sectionService, null).setVisible(true);
            } else {
                new AddSectionDialog(null, sectionService, null).setVisible(true);
            }

            loadSections();
        });

        
        btnEdit.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(parentWindow, "Select a section to edit.");
                return;
            }

            var section = tableModel.getSectionAt(row);

            if (parentWindow instanceof Frame frame) {
                new AddSectionDialog(frame, sectionService, section).setVisible(true);
            } else {
                new AddSectionDialog(null, sectionService, section).setVisible(true);
            }

            loadSections();
        });

        
        btnDelete.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(parentWindow, "Select a section to delete.");
                return;
            }

            var section = tableModel.getSectionAt(row);

            int confirm = JOptionPane.showConfirmDialog(
                    parentWindow,
                    "Delete section " + section.id + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                boolean ok = sectionService.deleteSection(section.id);

                if (!ok) {
                    JOptionPane.showMessageDialog(
                            parentWindow,
                            "❌ Cannot delete this section.\nStudents are currently enrolled.",
                            "Delete Blocked",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            parentWindow,
                            "Section deleted successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

                loadSections();
            }
        });

        
        btnRefresh.addActionListener(e -> loadSections());
    }

    
    private void styleButton(JButton b) {
        b.setFont(new Font("Inter", Font.BOLD, 14));
        b.setBackground(new Color(205, 235, 205));
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(150, 32));
    }

    
    public void loadSections() {
        List<SectionService.SectionRow> list = sectionService.getAllSections();
        tableModel.setSections(list);
    }
}
