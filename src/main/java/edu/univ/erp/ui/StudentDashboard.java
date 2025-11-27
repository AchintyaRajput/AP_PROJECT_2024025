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

    private String realName;   

    private CardLayout cardLayout;
    private JPanel cardPanel;

    
    private StudentAvailableSectionsUI availablePanel;
    private StudentEnrollmentsUI enrollmentsPanel;
    private StudentGradesUI gradesPanel;
    private StudentTimetableUI timetablePanel;

    public StudentDashboard(User user) {
        this.currentStudent = user;

        
        StudentService ss = new StudentService();
        realName = ss.getStudentName(currentStudent.getId());

        
        if (realName == null || realName.isBlank()) {
            realName = currentStudent.getUsername();
        }

        setTitle("Student Dashboard - " + realName);
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

        JLabel heading = new JLabel("Student Dashboard - " + realName, SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        top.add(heading, BorderLayout.WEST);

        
        JButton bell = new JButton("\uD83D\uDD14");
        bell.setFont(new Font("SansSerif", Font.PLAIN, 20));
        bell.setFocusPainted(false);
        bell.setBackground(Color.WHITE);
        bell.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bell.addActionListener(e -> openNotificationsPopup());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(Color.WHITE);
        right.add(bell);

        top.add(right, BorderLayout.EAST);

        
        if (DatabaseConnection.isMaintenanceOn()) {
            JLabel banner = new JLabel(
                    "⚠ Maintenance Mode Active — Enrollment & Drop Disabled",
                    SwingConstants.CENTER
            );
            banner.setOpaque(true);
            banner.setBackground(new Color(255, 204, 0));
            banner.setForeground(Color.BLACK);
            banner.setFont(new Font("SansSerif", Font.BOLD, 14));
            banner.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.add(top, BorderLayout.NORTH);
            wrapper.add(banner, BorderLayout.SOUTH);

            add(wrapper, BorderLayout.NORTH);
        } else {
            add(top, BorderLayout.NORTH);
        }
    }

    
    private void initSidebarAndContent() {

        
        JPanel sidebar = new JPanel(new GridLayout(10, 1, 0, 12));
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 12, 30, 12));
        sidebar.setBackground(new Color(220, 240, 220));

        JLabel sectionLabel = new JLabel("<html><b>Student</b></html>", SwingConstants.CENTER);
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        sidebar.add(sectionLabel);

        JButton btnHome = makeSidebarButton("Home");
        JButton btnAvailable = makeSidebarButton("Available Sections");
        JButton btnEnrollments = makeSidebarButton("My Enrollments");
        JButton btnGrades = makeSidebarButton("My Grades");
        JButton btnTimetable = makeSidebarButton("My Timetable");
        JButton btnTranscript = makeSidebarButton("Download Transcript (CSV)");
        JButton btnChangePass = makeSidebarButton("Change Password");
        JButton btnLogout = makeSidebarButton("Logout");

        sidebar.add(btnHome);
        sidebar.add(btnAvailable);
        sidebar.add(btnEnrollments);
        sidebar.add(btnGrades);
        sidebar.add(btnTimetable);
        sidebar.add(btnTranscript);
        sidebar.add(btnChangePass);
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        availablePanel = new StudentAvailableSectionsUI(currentStudent);
        enrollmentsPanel = new StudentEnrollmentsUI(currentStudent);
        gradesPanel = new StudentGradesUI(currentStudent);
        timetablePanel = new StudentTimetableUI(currentStudent);

        
        cardPanel.add(createHomePanel(), "home");
        cardPanel.add(availablePanel, "available");
        cardPanel.add(enrollmentsPanel, "enrollments");
        cardPanel.add(gradesPanel, "grades");
        cardPanel.add(timetablePanel, "timetable");

        add(cardPanel, BorderLayout.CENTER);
        showCard("home");

        
        btnHome.addActionListener(e -> showCard("home"));
        btnAvailable.addActionListener(e -> showCard("available"));
        btnEnrollments.addActionListener(e -> showCard("enrollments"));
        btnGrades.addActionListener(e -> showCard("grades"));
        btnTimetable.addActionListener(e -> showCard("timetable"));

        btnTranscript.addActionListener(e -> exportTranscript());

        btnChangePass.addActionListener(e ->
                new ChangePasswordUI(currentStudent).setVisible(true)
        );

        btnLogout.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor((Component) e.getSource());
            if (frame != null) frame.dispose();
            SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
        });
    }

    private JButton makeSidebarButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void showCard(String key) {
        cardLayout.show(cardPanel, key);
    }

   
     
    private JPanel createHomePanel() {

        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome, " + realName);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel info = new JLabel("<html><br>Use the sidebar to access all academic functions.</html>");
        info.setFont(new Font("SansSerif", Font.PLAIN, 16));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(welcome);
        p.add(info);

        return p;
    }

    
    private void openNotificationsPopup() {
        JDialog dlg = new JDialog(this, "Notifications", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 420);
        dlg.setLocationRelativeTo(this);

        NotificationPanel np = new NotificationPanel(currentStudent);
        dlg.getContentPane().add(np);

        dlg.setVisible(true);
    }

    
    private void exportTranscript() {

        List<StudentService.TranscriptRow> rows =
                studentService.getTranscriptRows(currentStudent.getId());

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "No transcript data available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Transcript CSV");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

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
                    "Transcript exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to export transcript.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
