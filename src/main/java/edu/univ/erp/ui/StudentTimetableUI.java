package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashMap;

/**
 * Updated Student Timetable UI
 * - Green highlight for classes
 * - Refresh button
 */
public class StudentTimetableUI extends JPanel {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel model;

    private static final int STEP_MIN = 30;
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(18, 0);
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri"};

    private final java.util.List<String> slotKeys;

    public StudentTimetableUI(User student) {
        this.currentStudent = student;
        this.slotKeys = buildSlotKeys();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initHeader();
        initTable();
        initRefreshButton();
        loadTimetable();
    }

    private java.util.List<String> buildSlotKeys() {
        java.util.List<String> keys = new ArrayList<>();
        LocalTime t = START;
        while (t.plusMinutes(STEP_MIN).compareTo(END) <= 0) {
            LocalTime t2 = t.plusMinutes(STEP_MIN);
            keys.add(t.format(TF) + "-" + t2.format(TF));
            t = t2;
        }
        return keys;
    }

    private void initHeader() {
        JLabel title = new JLabel("Weekly Timetable", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(220, 240, 220));
        header.add(title, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
    }

    private void initTable() {

        String[] headers = new String[DAYS.length + 1];
        headers[0] = "Time";
        System.arraycopy(DAYS, 0, headers, 1, DAYS.length);

        model = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);

        // custom renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            Color green = new Color(200, 240, 200);

            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c
            ) {
                Component comp = super.getTableCellRendererComponent(tbl, val, sel, foc, r, c);
                comp.setBackground(Color.WHITE);

                if (c > 0 && val != null && !val.toString().isEmpty()) {
                    comp.setBackground(green);
                }

                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
    }

    private void initRefreshButton() {
        JButton btnRefresh = new JButton("Refresh Timetable");
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnRefresh);

        btnRefresh.addActionListener(e -> loadTimetable());

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadTimetable() {
        model.setRowCount(0);

        java.util.Map<String, java.util.Map<String, String>> grid = new LinkedHashMap<>();

        for (String slot : slotKeys) {
            java.util.Map<String, String> m = new HashMap<>();
            for (String d : DAYS) m.put(d, "");
            grid.put(slot, m);
        }

        java.util.List<StudentService.EnrollmentRow> list =
                studentService.getStudentEnrollments(currentStudent.getId());

        for (StudentService.EnrollmentRow e : list) {

            if (e.dayTime == null || !e.dayTime.contains(" ")) continue;

            String[] parts = e.dayTime.split(" ", 2);
            if (parts.length != 2) continue;

            String day = parts[0];
            String times = parts[1];

            if (!Arrays.asList(DAYS).contains(day)) continue;
            if (!times.contains("-")) continue;

            String[] timeParts = times.split("-");
            LocalTime s = parseTime(timeParts[0].trim());
            LocalTime en = parseTime(timeParts[1].trim());
            if (s == null || en == null) continue;

            for (String slot : slotKeys) {
                String[] sp = slot.split("-");
                LocalTime ss = LocalTime.parse(sp[0], TF);
                LocalTime ee = LocalTime.parse(sp[1], TF);

                if (ss.isBefore(en) && ee.isAfter(s)) {
                    grid.get(slot).put(day, e.courseId + " (" + e.courseTitle + ")");
                }
            }
        }

        for (String slot : slotKeys) {
            java.util.Vector<String> row = new java.util.Vector<>();
            row.add(slot);
            for (String d : DAYS) row.add(grid.get(slot).get(d));
            model.addRow(row);
        }
    }

    private static LocalTime parseTime(String s) {
        try {
            return LocalTime.parse(s, TF);
        } catch (Exception ex) {
            try {
                return LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"));
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
