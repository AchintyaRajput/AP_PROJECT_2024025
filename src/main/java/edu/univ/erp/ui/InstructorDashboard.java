package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class InstructorDashboard extends JFrame {

    private final User currentInstructor;
    private final InstructorService instructorService = new InstructorService();

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private InstructorSectionsUI sectionsPanel;

    public InstructorDashboard(User instructor) {
        this.currentInstructor = instructor;

        setTitle("Instructor Dashboard - " + instructor.getUsername());
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        initTopBar();
        initSidebarAndContent();

        setVisible(true);
    }

    private void initTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel heading = new JLabel("Instructor Dashboard - " + currentInstructor.getUsername());
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        top.add(heading, BorderLayout.WEST);

        JButton bell = new JButton("\uD83D\uDD14");
        bell.setFont(new Font("SansSerif", Font.PLAIN, 20));
        bell.setFocusable(false);
        bell.setBackground(Color.WHITE);
        bell.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bell.addActionListener(e -> showNotificationsPopup());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(Color.WHITE);
        right.add(bell);

        top.add(right, BorderLayout.EAST);

        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Grade Editing Disabled",
                    SwingConstants.CENTER
            );
            banner.setOpaque(true);
            banner.setBackground(new Color(255, 204, 0));
            banner.setForeground(Color.BLACK);
            banner.setFont(new Font("SansSerif", Font.BOLD, 14));

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.add(top, BorderLayout.NORTH);
            wrap.add(banner, BorderLayout.SOUTH);
            add(wrap, BorderLayout.NORTH);
        } else {
            add(top, BorderLayout.NORTH);
        }
    }

    private void initSidebarAndContent() {

        JPanel sidebar = new JPanel(new GridLayout(6, 1, 0, 12));
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 12, 30, 12));
        sidebar.setBackground(new Color(220, 240, 220));

        JLabel title = new JLabel("<html><b>Instructor</b></html>", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        sidebar.add(title);

        JButton btnSections = makeButton("My Sections");
        JButton btnExport = makeButton("Export Grades (CSV)");
        JButton btnPass = makeButton("Change Password");
        JButton btnLogout = makeButton("Logout");

        sidebar.add(btnSections);
        sidebar.add(btnExport);
        sidebar.add(btnPass);
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        sectionsPanel = new InstructorSectionsUI(currentInstructor);
        cardPanel.add(sectionsPanel, "sections");

        add(cardPanel, BorderLayout.CENTER);
        showCard("sections");

        btnSections.addActionListener(e -> {
            sectionsPanel.loadSections();
            showCard("sections");
        });

        btnExport.addActionListener(e -> exportCSV());

        btnPass.addActionListener(e ->
                new ChangePasswordUI(currentInstructor).setVisible(true)
        );

        btnLogout.addActionListener(e -> {
            JFrame f = (JFrame) SwingUtilities.getWindowAncestor((Component) e.getSource());
            if (f != null) f.dispose();
            SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
        });
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void showCard(String key) {
        cardLayout.show(cardPanel, key);
    }

    
    private void showNotificationsPopup() {
        JDialog dlg = new JDialog(this, "Notifications", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 420);
        dlg.setLocationRelativeTo(this);

        dlg.add(new NotificationPanel(currentInstructor));

        dlg.setVisible(true);
    }

    
    private void exportCSV() {

        List<InstructorService.SectionRow> sections =
                instructorService.getInstructorSections(currentInstructor.getId());

        if (sections.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no sections assigned.");
            return;
        }

        String[] names = new String[sections.size()];
        for (int i = 0; i < sections.size(); i++) {
            var s = sections.get(i);
            names[i] = s.sectionId + " - " + s.courseId + " (" + s.courseTitle + ")";
        }

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Select section to export:",
                "Export CSV",
                JOptionPane.PLAIN_MESSAGE,
                null,
                names,
                names[0]
        );

        if (choice == null) return;

        int index = java.util.Arrays.asList(names).indexOf(choice);
        int sectionId = sections.get(index).sectionId;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("section_" + sectionId + "_grades.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File out = chooser.getSelectedFile();

        // ***** FIXED HERE → USE NEW METHOD *****
        List<InstructorService.GradeExportRow> rows =
                instructorService.getGradesForExport(sectionId);

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No grade data available.");
            return;
        }

        try (FileWriter fw = new FileWriter(out)) {
            fw.write("Student,Component,Score,Max Marks,Weight,Weighted Score\n");

            for (var r : rows) {
                fw.write(r.studentName + "," +
                        r.component + "," +
                        r.score + "," +
                        r.maxMarks + "," +
                        r.weight + "," +
                        r.weightedScore + "\n");
            }

            JOptionPane.showMessageDialog(this, "CSV exported successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to export CSV: " + ex.getMessage());
        }
    }
}
