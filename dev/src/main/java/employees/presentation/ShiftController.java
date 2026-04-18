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

/**
 * ShiftController class manages the scheduling of shifts, employee assignments, and related operations.
 * It maintains lists of employees, current shifts, and shift history. The controller provides methods for
 * adding shifts, assigning employees to shifts, handling substitutions, managing cancellation requests,
 * calculating worked hours and salaries, and listing shifts managed by a specific employee.
 */
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

    /** Req #4: Substitute an existing assignment with a different available employee.
     *  The replacement must not already be on the shift and must be authorized for the same role. */
    public void substituteEmployee(User requestedBy, Shift shift, Employee originalEmployee, Employee replacement) {
        if (!(requestedBy instanceof HR_Manager) || !((HR_Manager) requestedBy).isHRManager()) {
            throw new IllegalArgumentException("Only CA manager can make substitutions");
        }

        // Find the original assignment
        ShiftAssignment originalAssignment = null;
        for (ShiftAssignment a : shift.getAssignments()) {
            if (a.getEmployee().getId().equals(originalEmployee.getId())) {
                originalAssignment = a;
                break;
            }
        }
        if (originalAssignment == null) {
            throw new IllegalArgumentException(originalEmployee.getName() + " is not assigned to this shift");
        }

        Role role = originalAssignment.getRole();

        // Replacement must not already be on this shift
        for (ShiftAssignment a : shift.getAssignments()) {
            if (a.getEmployee().getId().equals(replacement.getId())) {
                throw new IllegalArgumentException(replacement.getName() + " is already assigned to this shift");
            }
        }

        // Replacement must be authorized for same role
        if (!replacement.getAuthorizedRoles().contains(role)) {
            throw new IllegalArgumentException(replacement.getName() + " is not authorized for role " + role);
        }

        // Check constraints for replacement (req #3 behaviour applies here too)
        boolean hasConflict = false;
        WeeklyAvailabilityRequest availability = replacement.getWeeklyAvailabilityRequest();
        if (availability != null) {
            for (Constraint constraint : availability.getConstraints()) {
                if (constraint.getDay() == shift.getDate().getDayOfWeek() &&
                    constraint.getShiftType() == shift.getShiftType()) {
                    hasConflict = true;
                    break;
                }
            }
        }

        // Remove original, add replacement
        shift.removeAssignment(originalAssignment);
        ShiftAssignment newAssignment = new ShiftAssignment(replacement, shift, role, hasConflict);
        if (!hasConflict) {
            newAssignment.setApproved(true);
        }
        shift.addAssignment(newAssignment);

        if (hasConflict) {
            System.out.println("Warning: Substitution conflicts with " + replacement.getName() +
                "'s constraints. A pending approval request has been sent to the employee.");
        }
    }

    /** Req #5: Employee can request to cancel an assigned shift. */
    public void requestShiftCancellation(Employee employee, Shift shift) {
        ShiftAssignment employeeAssignment = null;
        for (ShiftAssignment assignment : shift.getAssignments()) {
            if (assignment.getEmployee().getId().equals(employee.getId())) {
                employeeAssignment = assignment;
                break;
            }
        }

        if (employeeAssignment == null) {
            throw new IllegalArgumentException("Employee is not assigned to this shift");
        }
        employeeAssignment.setCancellationRequested(true);
    }

    /** Returns all assignments where cancellation was requested by employees. */
    public List<ShiftAssignment> getCancellationRequests() {
        List<ShiftAssignment> requests = new ArrayList<>();
        for (Shift shift : shifts) {
            for (ShiftAssignment assignment : shift.getAssignments()) {
                if (assignment.isCancellationRequested()) {
                    requests.add(assignment);
                }
            }
        }
        return requests;
    }

    /** HR handles cancellation by substituting another employee. */
    public void handleCancellationWithSubstitution(User requestedBy, ShiftAssignment cancellationRequest, Employee replacement) {
        if (!cancellationRequest.isCancellationRequested()) {
            throw new IllegalArgumentException("Assignment is not marked as cancellation request");
        }
        substituteEmployee(
            requestedBy,
            cancellationRequest.getShift(),
            cancellationRequest.getEmployee(),
            replacement
        );
    }

    /** Req #10: calculate worked hours from approved assignments and update salary. */
    public double calculateWorkedHoursForEmployee(Employee employee) {
        double totalHours = 0;

        for (Shift shift : shifts) {
            for (ShiftAssignment assignment : shift.getAssignments()) {
                if (assignment.getEmployee().getId().equals(employee.getId()) && assignment.isApproved()) {
                    totalHours += getShiftHours(shift.getShiftType());
                }
            }
        }
        return totalHours;
    }

    /** Updates employee salary according to approved assigned shifts and returns final salary. */
    public double recalculateEmployeeSalary(Employee employee) {
        if (employee.getSalary() == null) {
            throw new IllegalArgumentException("Employee salary configuration is missing");
        }

        double workedHours = calculateWorkedHoursForEmployee(employee);
        employee.getSalary().setWorkedHours(workedHours);
        return employee.getSalary().recalculateFinalSalary();
    }

    private double getShiftHours(ShiftType shiftType) {
        switch (shiftType) {
            case MORNING:
                return 8;
            case MORNING_OVERTIME:
                return 10;
            case EVENING:
                return 8;
            case DOUBLE_SHIFT:
                return 16;
            default:
                throw new IllegalArgumentException("Unsupported shift type: " + shiftType);
        }
    }

    /** Req #9: list shifts where this employee is the assigned shift manager. */
    public List<Shift> getShiftsManagedBy(Employee employee) {
        List<Shift> managedShifts = new ArrayList<>();
        for (Shift shift : shifts) {
            if (shift.getShiftManager() != null && shift.getShiftManager().getId().equals(employee.getId())) {
                managedShifts.add(shift);
            }
        }
        return managedShifts;
    }

    /** Req #9: shift manager transfers cancellation card for a selected shift. */
    public void transferCancellationCard(Employee shiftManager, Shift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("Shift is required");
        }
        shift.transferCancellationCard(shiftManager);
    }
}