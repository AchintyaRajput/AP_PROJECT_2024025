package edu.univ.erp.util;

import java.time.LocalDateTime;

public class DropDeadline {

    // Default deadline (set to any date you want)
    public static LocalDateTime deadline = LocalDateTime.of(2025, 10, 27, 23, 59);

    public static void setDeadline(LocalDateTime newDeadline) {
        deadline = newDeadline;
    }

    public static LocalDateTime getDeadline() {
        return deadline;
    }
}
