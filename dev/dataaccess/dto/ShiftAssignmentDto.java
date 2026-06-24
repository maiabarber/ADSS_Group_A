package dataaccess.dto;

import employee.domain.Role;
import employee.domain.ShiftType;

import java.time.LocalDate;

public final class ShiftAssignmentDto {
    private final String employeeId;
    private final LocalDate shiftDate;
    private final ShiftType shiftType;
    private final Role role;
    private final boolean approved;
    private final boolean requiresApproval;
    private final boolean cancellationRequested;
    private final boolean conflictsWithFixedDayOff;

    public ShiftAssignmentDto(
            String employeeId,
            LocalDate shiftDate,
            ShiftType shiftType,
            Role role,
            boolean approved,
            boolean requiresApproval,
            boolean cancellationRequested,
            boolean conflictsWithFixedDayOff) {
        this.employeeId = employeeId;
        this.shiftDate = shiftDate;
        this.shiftType = shiftType;
        this.role = role;
        this.approved = approved;
        this.requiresApproval = requiresApproval;
        this.cancellationRequested = cancellationRequested;
        this.conflictsWithFixedDayOff = conflictsWithFixedDayOff;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public Role getRole() {
        return role;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public boolean isCancellationRequested() {
        return cancellationRequested;
    }

    public boolean isConflictsWithFixedDayOff() {
        return conflictsWithFixedDayOff;
    }
}