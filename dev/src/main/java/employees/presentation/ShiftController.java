package employees.presentation;

import employees.domain.Employee;
import employees.domain.HR_Manager;
import employees.domain.Role;
import employees.domain.Shift;
import employees.domain.ShiftAssignment;
import employees.domain.User;

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

    public void assignEmployeeToShift(User assignedBy, Employee employee, Shift shift, Role role) 
    throws IllegalArgumentException {
    if (!(assignedBy instanceof HR_Manager) || !((HR_Manager) assignedBy).isHRManager()) {
        throw new IllegalArgumentException("Only CA manager can assign employees to shifts");
    }
    
    // Validate that employee is authorized for this role
    if (!employee.getAuthorizedRoles().contains(role)) {
        throw new IllegalArgumentException("Employee " + employee.getName() + " is not authorized for role " + role);
    }
    
    // Validate that employee is not already assigned to this shift
    for (ShiftAssignment existing : shift.getAssignments()) {
        if (existing.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Employee " + employee.getName() + " is already assigned to this shift");
        }
    }
    
    ShiftAssignment assignment = new ShiftAssignment(employee, shift, role);
    shift.addAssignment(assignment);
    }


}
