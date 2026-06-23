package employee.presentation;

import employee.domain.Constraint;
import employee.domain.Employee;
import employee.domain.HR_Manager;
import employee.domain.Preference;
import employee.domain.Role;
import employee.domain.Shift;
import employee.domain.ShiftAssignment;
import employee.domain.ShiftType;
import employee.domain.User;
import employee.domain.WeeklyAvailabilityRequest;
import employee.domain.DriverAvailability;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    /**
     * System-managed availability records for drivers, keyed by employeeId.
     * Each entry corresponds to a driver record in the transportation module (same employeeId)
     * and specifies which days and shift types the driver is permitted to work.
     * Managed exclusively by HR managers via {@link #registerDriverAvailability} and
     * {@link #updateDriverAvailability}.
     */
    private final Map<String, DriverAvailability> driverAvailabilities = new HashMap<>();

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

    // -------------------------------------------------------------------------
    // Driver availability registry (HR-managed)
    // -------------------------------------------------------------------------

    /**
     * Registers or replaces the system availability record for a driver.
     * Must be called by an HR manager. The {@code employeeId} on the
     * {@link DriverAvailability} must match an existing employee who holds the DRIVER role.
     *
     * @param managedBy    the HR manager making the change
     * @param availability the driver's system availability record
     */
    public void registerDriverAvailability(User managedBy, DriverAvailability availability) {
        ensureHrManager(managedBy);
        if (availability == null) {
            throw new IllegalArgumentException("DriverAvailability must not be null");
        }
        driverAvailabilities.put(availability.getEmployeeId(), availability);
    }

    /**
     * Updates the permitted shift types for a driver on a specific day.
     * Must be called by an HR manager.
     *
     * @param managedBy  the HR manager making the change
     * @param employeeId the driver's employee ID
     * @param day        the day of the week to configure
     * @param shifts     the shift types available that day; null/empty clears the day
     */
    public void updateDriverAvailability(User managedBy, String employeeId, DayOfWeek day, Set<ShiftType> shifts) {
        ensureHrManager(managedBy);
        DriverAvailability record = driverAvailabilities.get(employeeId);
        if (record == null) {
            throw new IllegalArgumentException("No driver availability record found for employeeId: " + employeeId);
        }
        record.setAvailableShifts(day, shifts);
    }

    /**
     * Returns the system availability record for a driver, or {@code null} if none is registered.
     *
     * @param employeeId the driver's employee ID
     * @return the availability record, or null
     */
    public DriverAvailability getDriverAvailability(String employeeId) {
        return driverAvailabilities.get(employeeId);
    }

    /** Returns an unmodifiable view of all registered driver availability records. */
    public Map<String, DriverAvailability> getAllDriverAvailabilities() {
        return Collections.unmodifiableMap(driverAvailabilities);
    }

    public void addShift(Shift shift) {
        if (shift != null) {
            shifts.add(shift);
            addToShiftHistory(shift);
        }
    }

    public void addToShiftHistory(Shift shift) {
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
        throw new IllegalArgumentException("Only HR manager can assign employees to shifts");
    }
    
    // Validate that employee is authorized for this role
    if (!employee.getAuthorizedRoles().contains(role)) {
        throw new IllegalArgumentException("Employee " + employee.getName() + " is not authorized for role " + role);
    }

    // For DRIVER role: verify a system availability record exists and covers this shift
    if (role == Role.DRIVER) {
        DriverAvailability driverRecord = driverAvailabilities.get(employee.getId());
        if (driverRecord == null) {
            throw new IllegalArgumentException("Employee " + employee.getName() +
                " has no driver availability record registered in the system");
        }
        if (!driverRecord.isAvailableFor(shift.getDate().getDayOfWeek(), shift.getShiftType())) {
            throw new IllegalArgumentException("Driver " + employee.getName() +
                " is not available for " + shift.getShiftType() +
                " shifts on " + shift.getDate().getDayOfWeek() +
                " according to the system record");
        }
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
    
    WeeklyAvailabilityRequest availability = employee.getWeeklyAvailabilityRequest();
    if (isFullDayVacationConstraint(availability, shift.getDate().getDayOfWeek())) {
        throw new IllegalArgumentException("Employee " + employee.getName() +
            " is on vacation on " + shift.getDate().getDayOfWeek() + " and cannot be assigned");
    }

    // Check if assignment conflicts with employee constraints or fixed day off
    boolean hasConstraintConflict = false;
    if (availability != null) {
        for (Constraint constraint : availability.getConstraints()) {
            if (constraint.getDay() == shift.getDate().getDayOfWeek() &&
                constraint.getShiftType() == shift.getShiftType()) {
                hasConstraintConflict = true;
                break;
            }
        }
    }
    boolean hasFixedDayOffConflict = employee.getFixedDayOff() != null &&
        employee.getFixedDayOff() == shift.getDate().getDayOfWeek();
    boolean hasConflict = hasConstraintConflict || hasFixedDayOffConflict;

    // No conflict → approve immediately
    // Conflict → create PENDING assignment, employee must approve
    ShiftAssignment assignment = new ShiftAssignment(
        employee,
        shift,
        role,
        hasConflict,
        hasFixedDayOffConflict
    );
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
        if (approved && assignment.isConflictsWithFixedDayOff()) {
            employee.addVacationDays(1);
        }
        if (!approved) {
            assignment.getShift().removeAssignment(assignment);
        }
    }

    /** Req #4: Substitute an existing assignment with a different available employee.
     *  The replacement must not already be on the shift and must be authorized for the same role. */
    public void substituteEmployee(User requestedBy, Shift shift, Employee originalEmployee, Employee replacement) {
        if (!(requestedBy instanceof HR_Manager) || !((HR_Manager) requestedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can make substitutions");
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

        // For DRIVER role: verify system availability record covers this shift
        if (role == Role.DRIVER) {
            DriverAvailability driverRecord = driverAvailabilities.get(replacement.getId());
            if (driverRecord == null) {
                throw new IllegalArgumentException("Employee " + replacement.getName() +
                    " has no driver availability record registered in the system");
            }
            if (!driverRecord.isAvailableFor(shift.getDate().getDayOfWeek(), shift.getShiftType())) {
                throw new IllegalArgumentException("Driver " + replacement.getName() +
                    " is not available for " + shift.getShiftType() +
                    " shifts on " + shift.getDate().getDayOfWeek() +
                    " according to the system record");
            }
        }

        // Check constraints for replacement (req #3 behaviour applies here too)
        boolean hasConstraintConflict = false;
        WeeklyAvailabilityRequest availability = replacement.getWeeklyAvailabilityRequest();
        if (isFullDayVacationConstraint(availability, shift.getDate().getDayOfWeek())) {
            throw new IllegalArgumentException(replacement.getName() +
                " is on vacation on " + shift.getDate().getDayOfWeek() + " and cannot be assigned");
        }
        if (availability != null) {
            for (Constraint constraint : availability.getConstraints()) {
                if (constraint.getDay() == shift.getDate().getDayOfWeek() &&
                    constraint.getShiftType() == shift.getShiftType()) {
                    hasConstraintConflict = true;
                    break;
                }
            }
        }
        boolean hasFixedDayOffConflict = replacement.getFixedDayOff() != null &&
            replacement.getFixedDayOff() == shift.getDate().getDayOfWeek();
        boolean hasConflict = hasConstraintConflict || hasFixedDayOffConflict;

        // Remove original, add replacement
        shift.removeAssignment(originalAssignment);
        ShiftAssignment newAssignment = new ShiftAssignment(
            replacement,
            shift,
            role,
            hasConflict,
            hasFixedDayOffConflict
        );
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

    private boolean isFullDayVacationConstraint(WeeklyAvailabilityRequest availability, java.time.DayOfWeek day) {
        if (availability == null) {
            return false;
        }

        boolean hasMorning = false;
        boolean hasMorningOvertime = false;
        boolean hasEvening = false;
        boolean hasDoubleShift = false;

        for (Constraint constraint : availability.getConstraints()) {
            if (constraint.getDay() != day) {
                continue;
            }
            switch (constraint.getShiftType()) {
                case MORNING:
                    hasMorning = true;
                    break;
                case MORNING_OVERTIME:
                    hasMorningOvertime = true;
                    break;
                case EVENING:
                    hasEvening = true;
                    break;
                case DOUBLE_SHIFT:
                    hasDoubleShift = true;
                    break;
                default:
                    break;
            }
        }

        return hasMorning && hasMorningOvertime && hasEvening && hasDoubleShift;
    }
}