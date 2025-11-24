package edu.univ.erp.ui;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.util.List;

public class StudentDashboard extends JFrame {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    public StudentDashboard(User user) {
        this.currentStudent = user;

        setTitle("Student Dashboard - " + user.getUsername());
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== HEADER =====
        JPanel top = new JPanel(new BorderLayout());
        JLabel heading = new JLabel("Student Dashboard - " + user.getUsername(),
                SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        top.add(heading, BorderLayout.NORTH);

        // ===== Maintenance Banner =====
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Enrollment & Drop are Disabled",
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
        NotificationPanel notifications = new NotificationPanel(user);
        add(notifications, BorderLayout.WEST);

        // ===== CENTER BUTTONS =====
        JPanel center = new JPanel(new GridLayout(7, 1, 20, 20));
        center.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnAvailable = new JButton("View Available Sections");
        JButton btnEnrollments = new JButton("My Enrollments");
        JButton btnGrades = new JButton("My Grades");
        JButton btnTimetable = new JButton("My Timetable");
        JButton btnTranscript = new JButton("Download Transcript (CSV)");
        JButton btnChangePass = new JButton("Change Password");
        JButton btnLogout = new JButton("Logout");

        center.add(btnAvailable);
        center.add(btnEnrollments);
        center.add(btnGrades);
        center.add(btnTimetable);
        center.add(btnTranscript);
        center.add(btnChangePass);
        center.add(btnLogout);

        add(center, BorderLayout.CENTER);

        // ===== ACTIONS =====

        btnAvailable.addActionListener(e ->
                new StudentAvailableSectionsUI(currentStudent).setVisible(true)
        );

        btnEnrollments.addActionListener(e ->
                new StudentEnrollmentsUI(currentStudent).setVisible(true)
        );

        btnGrades.addActionListener(e ->
                new StudentGradesUI(currentStudent).setVisible(true)
        );

        btnTimetable.addActionListener(e ->
                new StudentTimetableUI(currentStudent).setVisible(true)
        );

        btnTranscript.addActionListener(e -> exportTranscript());

        btnChangePass.addActionListener(e ->
                new ChangePasswordUI(currentStudent).setVisible(true)
        );

        btnLogout.addActionListener(e -> {
            dispose();
            JOptionPane.showMessageDialog(null, "Logged out successfully.");
        });

        setVisible(true);
    }

    // ==========================
    // CSV EXPORT IMPLEMENTATION
    // ==========================
    private void exportTranscript() {

        List<StudentService.TranscriptRow> rows =
                studentService.getTranscriptRows(currentStudent.getId());

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No transcript data available.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Transcript CSV");

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".csv")) {

            fw.write("Course ID,Course Title,Semester,Year,Final Grade,Status\n");

            for (var r : rows) {
                fw.write(r.courseId + "," +
                        "\"" + r.courseTitle + "\"," +
                        r.semester + "," +
                        r.year + "," +
                        (r.finalGrade == null ? "" : r.finalGrade) + "," +
                        r.status + "\n");
            }

            JOptionPane.showMessageDialog(this,
                    "Transcript exported successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to export transcript.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
