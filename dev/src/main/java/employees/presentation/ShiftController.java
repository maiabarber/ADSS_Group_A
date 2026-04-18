package employees.presentation;

import employees.domain.Constraint;
import employees.domain.Employee;
import employees.domain.HR_Manager;
import employees.domain.Preference;
import employees.domain.Role;
import employees.domain.Shift;
import employees.domain.ShiftAssignment;
import employees.domain.ShiftType;
import employees.domain.User;
import employees.domain.WeeklyAvailabilityRequest;

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

    // Req #2: DOUBLE_SHIFT and MORNING_OVERTIME require explicit employee preference
    if (shift.getShiftType() == ShiftType.DOUBLE_SHIFT || shift.getShiftType() == ShiftType.MORNING_OVERTIME) {
        boolean hasPreference = false;
        WeeklyAvailabilityRequest availability = employee.getWeeklyAvailabilityRequest();
        if (availability != null) {
            for (Preference preference : availability.getPreferences()) {
                if (preference.getDay() == shift.getDate().getDayOfWeek() &&
                    preference.getShiftType() == shift.getShiftType()) {
                    hasPreference = true;
                    break;
                }
            }
        }
        if (!hasPreference) {
            throw new IllegalArgumentException("Employee " + employee.getName() +
                " has not indicated a preference for " + shift.getShiftType() +
                " on " + shift.getDate().getDayOfWeek());
        }
    }
    
    // Check if assignment conflicts with employee constraints
    boolean hasConflict = false;
    WeeklyAvailabilityRequest availability = employee.getWeeklyAvailabilityRequest();
    if (availability != null) {
        for (Constraint constraint : availability.getConstraints()) {
            if (constraint.getDay() == shift.getDate().getDayOfWeek() &&
                constraint.getShiftType() == shift.getShiftType()) {
                hasConflict = true;
                break;
            }
        }
    }

    // No conflict → approve immediately
    // Conflict → create PENDING assignment, employee must approve
    ShiftAssignment assignment = new ShiftAssignment(employee, shift, role, hasConflict);
    if (!hasConflict) {
        assignment.setApproved(true);
    }
    shift.addAssignment(assignment);

    if (hasConflict) {
        System.out.println("Warning: Assignment conflicts with " + employee.getName() +
            "'s constraints. A pending approval request has been sent to the employee.");
    }
    }

    /** Returns all assignments waiting for this employee's approval */
    public List<ShiftAssignment> getPendingApprovalsForEmployee(Employee employee) {
        List<ShiftAssignment> pending = new ArrayList<>();
        for (Shift shift : shifts) {
            for (ShiftAssignment assignment : shift.getAssignments()) {
                if (assignment.getEmployee().getId().equals(employee.getId()) && assignment.isPending()) {
                    pending.add(assignment);
                }
            }
        }
        return pending;
    }

    /** Employee approves or rejects a pending assignment.
     *  If rejected, the assignment is removed from the shift so HR can pick another employee. */
    public void respondToAssignment(Employee employee, ShiftAssignment assignment, boolean approved) {
        if (!assignment.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Employee can only respond to their own assignments");
        }
        if (!assignment.isPending()) {
            throw new IllegalArgumentException("Assignment is not pending approval");
        }
        assignment.setApproved(approved);
        if (!approved) {
            assignment.getShift().removeAssignment(assignment);
        }
    }

}
