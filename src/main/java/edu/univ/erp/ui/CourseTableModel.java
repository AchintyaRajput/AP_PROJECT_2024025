package edu.univ.erp.ui;

import edu.univ.erp.service.CourseService;

import javax.swing.table.AbstractTableModel;
import java.util.List;


public class CourseTableModel extends AbstractTableModel {

    private final String[] columns = {"Course ID", "Title", "Credits"};
    private List<CourseService.CourseRow> courses;

    public CourseTableModel(List<CourseService.CourseRow> courses) {
        this.courses = courses;
    }

    public void setCourses(List<CourseService.CourseRow> courses) {
        this.courses = courses;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return courses == null ? 0 : courses.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        CourseService.CourseRow c = courses.get(row);

        return switch (column) {
            case 0 -> c.id;
            case 1 -> c.title;
            case 2 -> c.credits;
            default -> null;
        };
    }

    public CourseService.CourseRow getCourseAt(int row) {
        return courses.get(row);
    }
}
