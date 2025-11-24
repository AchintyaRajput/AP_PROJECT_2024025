package edu.univ.erp.ui;

import edu.univ.erp.service.CourseService;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * UI for managing courses (Admin only).
 */
public class ManageCoursesUI extends JFrame {

    private final CourseService courseService = new CourseService();
    private CourseTableModel tableModel;
    private JTable table;
    private final User currentAdmin;

    public ManageCoursesUI(User admin) {
        this.currentAdmin = admin;

        setTitle("Manage Courses");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadCourses();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== TOP BUTTON PANEL =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnAdd = new JButton("Add Course");
        JButton btnEdit = new JButton("Edit Course");
        JButton btnDelete = new JButton("Delete Course");
        JButton btnRefresh = new JButton("Refresh");

        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        tableModel = new CourseTableModel(null);
        table = new JTable(tableModel);
        table.setRowHeight(24);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BUTTON LISTENERS =====

        // Add Course
        btnAdd.addActionListener(e -> {
            new AddCourseDialog(this, courseService, null).setVisible(true);
            loadCourses();
        });

        // Edit Course
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a course to edit.");
                return;
            }
            var course = tableModel.getCourseAt(row);
            new AddCourseDialog(this, courseService, course).setVisible(true);
            loadCourses();
        });

        // Delete Course
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a course to delete.");
                return;
            }
            var course = tableModel.getCourseAt(row);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete course " + course.id + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = courseService.deleteCourse(course.id);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Could not delete course. Maybe linked to a section?");
                }
                loadCourses();
            }
        });

        // Refresh
        btnRefresh.addActionListener(e -> loadCourses());
    }

    // Load courses into table
    private void loadCourses() {
        List<CourseService.CourseRow> list = courseService.getAllCourses();
        tableModel.setCourses(list);
    }
}
