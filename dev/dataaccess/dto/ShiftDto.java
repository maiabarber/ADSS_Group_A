package dataaccess.dto;

import employee.domain.Role;
import employee.domain.ShiftType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ShiftDto {
    private final LocalDate date;
    private final ShiftType shiftType;
    private final String shiftManagerId;
    private final BranchDto branch;
    private final Map<Role, Integer> requiredRoleCounts;
    private final List<ShiftAssignmentDto> assignments;
    private final boolean cancellationCardTransferred;
    private final String cancellationCardTransferredById;

    public ShiftDto(
            LocalDate date,
            ShiftType shiftType,
            String shiftManagerId,
            BranchDto branch,
            Map<Role, Integer> requiredRoleCounts,
            List<ShiftAssignmentDto> assignments,
            boolean cancellationCardTransferred,
            String cancellationCardTransferredById) {
        this.date = date;
        this.shiftType = shiftType;
        this.shiftManagerId = shiftManagerId;
        this.branch = branch;
        this.requiredRoleCounts = requiredRoleCounts == null ? new EnumMap<>(Role.class) : new EnumMap<>(requiredRoleCounts);
        this.assignments = assignments == null ? new ArrayList<>() : new ArrayList<>(assignments);
        this.cancellationCardTransferred = cancellationCardTransferred;
        this.cancellationCardTransferredById = cancellationCardTransferredById;
    }

    public LocalDate getDate() {
        return date;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public String getShiftManagerId() {
        return shiftManagerId;
    }

    public BranchDto getBranch() {
        return branch;
    }

    public Map<Role, Integer> getRequiredRoleCounts() {
        return Collections.unmodifiableMap(requiredRoleCounts);
    }

    public List<ShiftAssignmentDto> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    public boolean isCancellationCardTransferred() {
        return cancellationCardTransferred;
    }

    public String getCancellationCardTransferredById() {
        return cancellationCardTransferredById;
    }
}