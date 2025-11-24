package edu.univ.erp.ui;

import edu.univ.erp.service.CourseService;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for adding or editing a course.
 */
public class AddCourseDialog extends JDialog {

    private final JTextField idField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField creditsField = new JTextField();

    private final CourseService courseService;
    private final CourseService.CourseRow editingCourse;

    public AddCourseDialog(Frame owner, CourseService service, CourseService.CourseRow courseToEdit) {
        super(owner, true);
        this.courseService = service;
        this.editingCourse = courseToEdit;

        setTitle(courseToEdit == null ? "Add Course" : "Edit Course");
        setSize(400, 260);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initForm();
        initButtons();

        if (courseToEdit != null) loadCourseData();
    }

    private void initForm() {
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        form.add(new JLabel("Course ID:"));
        form.add(idField);

        form.add(new JLabel("Title:"));
        form.add(titleField);

        form.add(new JLabel("Credits:"));
        form.add(creditsField);

        add(form, BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnCancel = new JButton("Cancel");
        JButton btnSave = new JButton("Save");

        bottom.add(btnCancel);
        bottom.add(btnSave);

        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());
    }

    private void loadCourseData() {
        idField.setText(editingCourse.id);
        idField.setEnabled(false); // Do not allow editing ID
        titleField.setText(editingCourse.title);
        creditsField.setText(String.valueOf(editingCourse.credits));
    }

    private void onSave() {
        String courseId = idField.getText().trim();
        String title = titleField.getText().trim();
        String creditsText = creditsField.getText().trim();

        if (courseId.isEmpty() || title.isEmpty() || creditsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Credits must be a number.");
            return;
        }

        boolean success;

        // Editing existing course
        if (editingCourse != null) {
            success = courseService.updateCourse(courseId, title, credits);
        }
        // Adding a new course
        else {
            success = courseService.addCourse(courseId, title, credits);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Course saved successfully.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save course.");
        }
    }
}
