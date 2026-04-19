package employees.domain;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WeeklyAvailabilityRules contains domain rules for vacation usage and weekly constraints.
 */
public class WeeklyAvailabilityRules {
    public void validateVacationUsage(Employee employee, int vacationDaysToUse, List<DayOfWeek> selectedVacationDays) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee is required");
        }
        if (vacationDaysToUse < 0) {
            throw new IllegalArgumentException("Vacation days to use this week cannot be negative.");
        }

        int currentVacationBalance = employee.getVacationDaysBalance();
        if (vacationDaysToUse > currentVacationBalance) {
            throw new IllegalArgumentException("Cannot use more vacation days than available balance.");
        }

        List<DayOfWeek> selected = selectedVacationDays != null ? selectedVacationDays : Collections.emptyList();
        if (selected.size() != vacationDaysToUse) {
            throw new IllegalArgumentException("Please select exactly " + vacationDaysToUse + " vacation day(s).");
        }

        for (int i = 0; i < selected.size(); i++) {
            DayOfWeek day = selected.get(i);
            if (day == null) {
                throw new IllegalArgumentException("Vacation day selection cannot contain empty values.");
            }
            for (int j = i + 1; j < selected.size(); j++) {
                if (day == selected.get(j)) {
                    throw new IllegalArgumentException("Vacation day was selected more than once: " + day + ".");
                }
            }
            if (employee.getFixedDayOff() != null && employee.getFixedDayOff() == day) {
                throw new IllegalArgumentException("Cannot use a vacation day on fixed day off: " + day + ".");
            }
        }
    }

    public List<Constraint> mergeConstraintsWithVacationDays(List<Constraint> constraints, List<DayOfWeek> vacationDays) {
        List<Constraint> merged = new ArrayList<>(constraints != null ? constraints : Collections.emptyList());
        List<DayOfWeek> selected = vacationDays != null ? vacationDays : Collections.emptyList();

        for (DayOfWeek day : selected) {
            addConstraintIfMissing(merged, day, ShiftType.MORNING);
            addConstraintIfMissing(merged, day, ShiftType.MORNING_OVERTIME);
            addConstraintIfMissing(merged, day, ShiftType.EVENING);
            addConstraintIfMissing(merged, day, ShiftType.DOUBLE_SHIFT);
        }

        return merged;
    }

    private void addConstraintIfMissing(List<Constraint> constraints, DayOfWeek day, ShiftType shiftType) {
        for (Constraint existing : constraints) {
            if (existing.getDay() == day && existing.getShiftType() == shiftType) {
                return;
            }
        }
        constraints.add(new Constraint(day, shiftType));
    }
}
