package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.CourseService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * ManageCoursesUI embedded as a JPanel inside AdminDashboard.
 * Uses SwingUtilities.getWindowAncestor(this) as parent for dialogs.
 */
public class ManageCoursesUI extends JPanel {

    private final CourseService courseService = new CourseService();
    private CourseTableModel tableModel;
    private JTable table;

    private final User currentAdmin;

    public ManageCoursesUI(User admin) {
        this.currentAdmin = admin;

        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        initUI();
        loadCourses();
    }

    private void initUI() {

        // ======================================
        // TOP BUTTON BAR
        // ======================================
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("Add Course");
        JButton btnEdit = new JButton("Edit Course");
        JButton btnDelete = new JButton("Delete Course");
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

        // ======================================
        // TABLE
        // ======================================
        tableModel = new CourseTableModel(null);
        table = new JTable(tableModel);
        table.setRowHeight(24);

        JScrollPane scroller = new JScrollPane(table);
        scroller.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        add(scroller, BorderLayout.CENTER);

        // ======================================
        // BUTTON LISTENERS
        // ======================================

        // -------- Add Course --------
        btnAdd.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            if (parentWindow instanceof Frame frame) {
                new AddCourseDialog(frame, courseService, null).setVisible(true);
            } else {
                new AddCourseDialog(null, courseService, null).setVisible(true);
            }

            loadCourses();
        });

        // -------- Edit Course --------
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            if (row == -1) {
                JOptionPane.showMessageDialog(parentWindow, "Select a course to edit.");
                return;
            }

            var course = tableModel.getCourseAt(row);

            if (parentWindow instanceof Frame frame) {
                new AddCourseDialog(frame, courseService, course).setVisible(true);
            } else {
                new AddCourseDialog(null, courseService, course).setVisible(true);
            }

            loadCourses();
        });

        // -------- Delete Course --------
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            if (row == -1) {
                JOptionPane.showMessageDialog(parentWindow, "Select a course to delete.");
                return;
            }

            var course = tableModel.getCourseAt(row);

            int confirm = JOptionPane.showConfirmDialog(
                    parentWindow,
                    "Delete course " + course.id + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = courseService.deleteCourse(course.id);
                if (!ok) {
                    JOptionPane.showMessageDialog(parentWindow,
                            "Could not delete course.\n(It may be linked to a section.)");
                }
                loadCourses();
            }
        });

        // -------- Refresh --------
        btnRefresh.addActionListener(e -> loadCourses());
    }

    // =========================================
    // BUTTON STYLE HELPER
    // =========================================
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(205, 235, 205));
        btn.setForeground(Color.BLACK);
        btn.setPreferredSize(new Dimension(140, 32));
    }

    // =========================================
    // LOAD COURSES PUBLIC METHOD
    // =========================================
    public void loadCourses() {
        List<CourseService.CourseRow> list = courseService.getAllCourses();
        tableModel.setCourses(list);
    }
}
