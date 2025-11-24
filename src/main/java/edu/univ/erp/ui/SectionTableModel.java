package edu.univ.erp.ui;

import edu.univ.erp.service.SectionService;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * TableModel for displaying sections in a JTable.
 */
public class SectionTableModel extends AbstractTableModel {

    private final String[] columns = {
            "ID", "Course ID", "Course Title",
            "Instructor", "Day/Time",
            "Room", "Capacity", "Semester", "Year"
    };

    private List<SectionService.SectionRow> sections;

    public SectionTableModel(List<SectionService.SectionRow> sections) {
        this.sections = sections;
    }

    public void setSections(List<SectionService.SectionRow> sections) {
        this.sections = sections;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return sections == null ? 0 : sections.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        SectionService.SectionRow s = sections.get(row);

        return switch (col) {
            case 0 -> s.id;
            case 1 -> s.courseId;
            case 2 -> s.courseTitle;
            case 3 -> s.instructorName;
            case 4 -> s.dayTime;
            case 5 -> s.room;
            case 6 -> s.capacity;
            case 7 -> s.semester;
            case 8 -> s.year;
            default -> null;
        };
    }

    public SectionService.SectionRow getSectionAt(int row) {
        return sections.get(row);
    }
}
