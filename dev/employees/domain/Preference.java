package employees.domain;

import java.time.DayOfWeek;

/**
 * Preference class represents an employee's preference for a specific day and shift type.
 * It contains the day of the week and the preferred shift type.
 */
public class Preference {
    private DayOfWeek day;
    private ShiftType shiftType;

    public Preference(DayOfWeek day, ShiftType shiftType) {
        this.day = day;
        this.shiftType = shiftType;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    @Override
    public String toString() {
        return "Preference{" +
            "day=" + day +
            ", shiftType=" + shiftType +
            '}';
    }
}
