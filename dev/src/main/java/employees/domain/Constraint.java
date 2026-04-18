package employees.domain;
import java.time.DayOfWeek;

/**
 * Constraint class represents a work constraint for an employee.
 * It includes the day of the week and the type of shift.
 */
public class Constraint {
    private DayOfWeek day;
    private ShiftType shiftType;

    public Constraint(DayOfWeek day, ShiftType shiftType) {
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
        return "Constraint{" +
            "day=" + day +
            ", shiftType=" + shiftType +
            '}';
    }
}
