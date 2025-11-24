package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Displays student's weekly timetable with 30-minute slots from 09:00 -> 18:00.
 * A class that spans a range (e.g. 09:00-10:30) will fill all overlapping 30-min slots.
 */
public class StudentTimetableUI extends JFrame {

    private final User currentStudent;
    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel model;

    // slot step
    private static final int STEP_MIN = 30;

    // timetable range: 09:00 -> 18:00
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(18, 0);

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    // Days of week (columns)
    private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri"};

    // computed slots (strings like "09:00-09:30")
    private final List<String> slotKeys;

    public StudentTimetableUI(User student) {
        this.currentStudent = student;
        this.slotKeys = buildSlotKeys();

        setTitle("Weekly Timetable - " + student.getUsername());
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        loadTimetable();

        setVisible(true);
    }

    private List<String> buildSlotKeys() {
        List<String> keys = new ArrayList<>();
        LocalTime t = START;
        while (t.plusMinutes(STEP_MIN).compareTo(END) <= 0) {
            LocalTime t2 = t.plusMinutes(STEP_MIN);
            keys.add(t.format(TF) + "-" + t2.format(TF));
            t = t2;
        }
        return keys;
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Weekly Timetable", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(getColumnHeaders(), 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // read-only
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private String[] getColumnHeaders() {
        String[] headers = new String[DAYS.length + 1];
        headers[0] = "Time";
        System.arraycopy(DAYS, 0, headers, 1, DAYS.length);
        return headers;
    }

    private void loadTimetable() {
        model.setRowCount(0);

        // Initialize empty grid: slot -> (day -> text)
        Map<String, Map<String, String>> grid = new LinkedHashMap<>();
        for (String slot : slotKeys) {
            Map<String, String> dayMap = new HashMap<>();
            for (String d : DAYS) dayMap.put(d, "");
            grid.put(slot, dayMap);
        }

        // Load student enrollments
        List<StudentService.EnrollmentRow> list =
                studentService.getStudentEnrollments(currentStudent.getId());

        for (StudentService.EnrollmentRow e : list) {
            if (e.dayTime == null || !e.dayTime.contains(" ")) continue;

            String[] parts = e.dayTime.split(" ", 2);
            if (parts.length != 2) continue;
            String day = parts[0].trim();
            String times = parts[1].trim();

            if (!Arrays.asList(DAYS).contains(day)) continue;

            // parse times like "09:00-10:30" or "9:00-10:30"
            if (!times.contains("-")) continue;
            String[] tr = times.split("-", 2);
            LocalTime s = parseLenientTime(tr[0].trim());
            LocalTime en = parseLenientTime(tr[1].trim());
            if (s == null || en == null) continue;

            // Fill all slots overlapped by [s,en)
            for (String slot : slotKeys) {
                String[] tparts = slot.split("-", 2);
                LocalTime slotStart = LocalTime.parse(tparts[0], TF);
                LocalTime slotEnd = LocalTime.parse(tparts[1], TF);

                // overlap if slotStart < en && slotEnd > s
                if (slotStart.isBefore(en) && slotEnd.isAfter(s)) {
                    // place course id + title
                    String text = e.courseId + " (" + e.courseTitle + ")";
                    grid.get(slot).put(day, text);
                }
            }
        }

        // Add rows into the table in order
        for (String slot : slotKeys) {
            Vector<String> row = new Vector<>();
            row.add(slot);
            for (String d : DAYS) {
                row.add(grid.get(slot).get(d));
            }
            model.addRow(row);
        }
    }

    // accept "9:00" or "09:00"
    private static LocalTime parseLenientTime(String s) {
        try {
            return LocalTime.parse(s, TF); // "09:00"
        } catch (Exception ex) {
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("H:mm"); // "9:00"
                return LocalTime.parse(s, f);
            } catch (Exception ex2) {
                return null;
            }
        }
    }
}
