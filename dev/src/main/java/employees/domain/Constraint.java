package employees.domain;

import java.time.DayOfWeek;

public class Constraint {
    private DayOfWeek day;

    public Constraint() {
    }

    public Constraint(DayOfWeek day) {
        this.day = day;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Constraint{" +
            "day=" + day +
            '}';
    }
}
