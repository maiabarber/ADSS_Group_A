package employees.domain;

import java.time.DayOfWeek;

public class Preference {
    private DayOfWeek day;
    private ShiftType shiftType;

    public Preference() {
    }

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
