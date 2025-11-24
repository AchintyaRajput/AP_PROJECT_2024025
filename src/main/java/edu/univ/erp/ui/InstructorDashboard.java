package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class InstructorDashboard extends JFrame {

    private final User currentInstructor;
    private final InstructorService instructorService = new InstructorService();

    public InstructorDashboard(User instructor) {
        this.currentInstructor = instructor;

        setTitle("Instructor Dashboard - " + instructor.getUsername());
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== HEADER PANEL =====
        JPanel top = new JPanel(new BorderLayout());

        JLabel heading = new JLabel("Instructor Dashboard - " + instructor.getUsername(),
                SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        top.add(heading, BorderLayout.NORTH);

        // ===== MAINTENANCE BANNER =====
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Grade Editing Disabled",
                    SwingConstants.CENTER
            );
            banner.setOpaque(true);
            banner.setBackground(new Color(255, 204, 0));
            banner.setForeground(Color.BLACK);
            banner.setFont(new Font("SansSerif", Font.BOLD, 14));
            top.add(banner, BorderLayout.SOUTH);
        }

        add(top, BorderLayout.NORTH);

        // ===== LEFT: Notification Panel =====
        NotificationPanel notifications = new NotificationPanel(instructor);
        add(notifications, BorderLayout.WEST);

        // ===== CENTER BUTTON PANEL =====
        JPanel center = new JPanel(new GridLayout(4, 1, 20, 20));
        center.setBorder(BorderFactory.createEmptyBorder(30, 120, 30, 120));

        JButton btnSections = new JButton("View My Sections");
        JButton btnExportCSV = new JButton("Export Grades (CSV)");
        JButton btnChangePass = new JButton("Change Password");
        JButton btnLogout = new JButton("Logout");

        center.add(btnSections);
        center.add(btnExportCSV);
        center.add(btnChangePass);
        center.add(btnLogout);

        add(center, BorderLayout.CENTER);

        // ===== ACTION LISTENERS =====
        btnSections.addActionListener(e ->
                new InstructorSectionsUI(currentInstructor).setVisible(true)
        );

        btnExportCSV.addActionListener(e -> exportCSV());

        btnChangePass.addActionListener(e ->
                new ChangePasswordUI(currentInstructor).setVisible(true)
        );

        btnLogout.addActionListener(e -> {
            dispose();
            JOptionPane.showMessageDialog(null, "Logged out successfully.");
        });

        setVisible(true);
    }

    // ============================================================
    // CSV EXPORT LOGIC
    // ============================================================
    private void exportCSV() {
        // 1. Fetch sections
        List<InstructorService.SectionRow> sections =
                instructorService.getInstructorSections(currentInstructor.getId());

        if (sections.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no sections assigned.");
            return;
        }

        // 2. Let user choose a section
        String[] sectionNames = new String[sections.size()];
        for (int i = 0; i < sections.size(); i++) {
            var s = sections.get(i);
            sectionNames[i] = s.sectionId + " - " + s.courseId + " (" + s.courseTitle + ")";
        }

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Select a section to export grades:",
                "Export Grades CSV",
                JOptionPane.PLAIN_MESSAGE,
                null,
                sectionNames,
                sectionNames[0]
        );

        if (choice == null) return;

        int selectedIndex = -1;
        for (int i = 0; i < sectionNames.length; i++) {
            if (sectionNames[i].equals(choice)) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex == -1) return;

        int sectionId = sections.get(selectedIndex).sectionId;

        // 3. Choose save file
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("section_" + sectionId + "_grades.csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        // 4. Fetch and export
        List<InstructorService.GradeExportRow> rows =
                instructorService.getGradesForExport(sectionId);

        boolean ok = instructorService.exportGradesToCSV(file, rows);

        if (ok)
            JOptionPane.showMessageDialog(this, "CSV exported successfully!");
        else
            JOptionPane.showMessageDialog(this, "Error exporting CSV.");
    }
}
