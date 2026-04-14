package employees.domain;

import java.time.DayOfWeek;

public class Preference {
    private DayOfWeek day;

    public Preference() {
    }

    public Preference(DayOfWeek day) {
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
        return "Preference{" +
            "day=" + day +
            '}';
    }
}
