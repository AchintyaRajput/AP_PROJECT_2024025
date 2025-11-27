package edu.univ.erp.ui;

import edu.univ.erp.service.CourseService;

import javax.swing.*;
import java.awt.*;

public class AddCourseDialog extends JDialog {

    private final JTextField idField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField creditsField = new JTextField();

    private final CourseService courseService;
    private final CourseService.CourseRow editingCourse;

    
    public AddCourseDialog(Window parent, CourseService service, CourseService.CourseRow courseToEdit) {
        super(parent instanceof Frame ? (Frame) parent : null, true);
        this.courseService = service;
        this.editingCourse = courseToEdit;

        setTitle(courseToEdit == null ? "Add Course" : "Edit Course");
        setSize(560, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initForm();
        initButtons();

        if (courseToEdit != null) loadCourseData();
    }

    private void initForm() {
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.setBackground(new Color(238, 247, 238));

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
        idField.setEnabled(false);
        titleField.setText(editingCourse.title);
        creditsField.setText(String.valueOf(editingCourse.credits));
    }

    private void onSave() {
        String id = idField.getText().trim();
        String title = titleField.getText().trim();
        String creditsText = creditsField.getText().trim();

        if (id.isEmpty() || title.isEmpty() || creditsText.isEmpty()) {
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

        if (editingCourse != null)
            success = courseService.updateCourse(id, title, credits);
        else
            success = courseService.addCourse(id, title, credits);

        if (success) {
            JOptionPane.showMessageDialog(this, "Course saved successfully.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save course.");
        }
    }
}
