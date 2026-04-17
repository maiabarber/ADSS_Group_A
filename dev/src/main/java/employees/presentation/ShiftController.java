package employees.presentation;

import employees.domain.Employee;
import employees.domain.Shift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShiftController {
    private final List<Employee> employees = new ArrayList<>();
    private final List<Shift> shifts = new ArrayList<>();
    private final List<Shift> shiftHistory = new ArrayList<>();

    public List<Employee> getEmployees() {
        return Collections.unmodifiableList(employees);
    }

    public List<Shift> getShifts() {
        return Collections.unmodifiableList(shifts);
    }

    public List<Shift> getShiftHistory() {
        return Collections.unmodifiableList(shiftHistory);
    }

    public void setEmployees(List<Employee> employees) {
        this.employees.clear();
        if (employees != null) {
            this.employees.addAll(employees);
        }
    }

    public void addShift(Shift shift) {
        if (shift != null) {
            shifts.add(shift);
            addToShiftHistory(shift);
        }
    }

    private void addToShiftHistory(Shift shift) {
        String shiftId = shift.getDate() + ":" + shift.getShiftType();

        for (Shift existingShift : shiftHistory) {
            String existingShiftId = existingShift.getDate() + ":" + existingShift.getShiftType();
            if (shiftId.equals(existingShiftId)) {
                return;
            }
        }

        shiftHistory.add(shift);
    }
}
