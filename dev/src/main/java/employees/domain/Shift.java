package employees.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Shift implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate date;
    private ShiftType shiftType;
    private Employee shiftManager;
    private final Map<Role, Integer> requiredRoleCounts;
    private List<ShiftAssignment> assignments;
    private boolean cancellationCardTransferred;
    private LocalDateTime cancellationCardTransferTime;
    private Employee cancellationCardTransferredBy;

    public Shift() {
        this.requiredRoleCounts = new EnumMap<>(Role.class);
        initializeDefaultRoleRequirements();
        this.assignments = new ArrayList<>();
        this.cancellationCardTransferred = false;
    }

    public Shift(LocalDate date, ShiftType shiftType, Employee shiftManager, int requiredCashiers, int requiredStorekeepers) {
        this();
        this.date = date;
        this.shiftType = shiftType;
        this.shiftManager = shiftManager;
        setRequiredCashiers(requiredCashiers);
        setRequiredStorekeepers(requiredStorekeepers);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public Employee getShiftManager() {
        return shiftManager;
    }

    public void setShiftManager(Employee shiftManager) {
        validateQualifiedShiftManager(shiftManager);
        this.shiftManager = shiftManager;
    }

    public void assignShiftManager(User selectedBy, Employee candidateShiftManager) {
        if (!(selectedBy instanceof HR_Manager) || !((HR_Manager) selectedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can assign shift manager");
        }

        validateQualifiedShiftManager(candidateShiftManager);
        this.shiftManager = candidateShiftManager;
    }

    private void validateQualifiedShiftManager(Employee candidateShiftManager) {
        if (candidateShiftManager == null) {
            throw new IllegalArgumentException("Shift manager must not be null");
        }
        if (!candidateShiftManager.canManageShift()) {
            throw new IllegalArgumentException("Employee is not certified to manage shifts");
        }
        if (candidateShiftManager.isFired()) {
            throw new IllegalArgumentException("Fired employee cannot be assigned as shift manager");
        }
    }

    public int getRequiredCashiers() {
        return getRequiredCount(Role.CASHIER);
    }

    private void setRequiredCashiers(int requiredCashiers) {
        setRequiredCount(Role.CASHIER, requiredCashiers);
    }

    public int getRequiredStorekeepers() {
        return getRequiredCount(Role.STOREKEEPER);
    }

    private void setRequiredStorekeepers(int requiredStorekeepers) {
        setRequiredCount(Role.STOREKEEPER, requiredStorekeepers);
    }

    public Map<Role, Integer> getRequiredRoleCounts() {
        return Collections.unmodifiableMap(requiredRoleCounts);
    }

    public void configureRequiredRoleCounts(User selectedBy, Map<Role, Integer> selectedRoleCounts) {
        ensureHrManager(selectedBy);
        initializeDefaultRoleRequirements();

        if (selectedRoleCounts == null) {
            return;
        }

        for (Map.Entry<Role, Integer> entry : selectedRoleCounts.entrySet()) {
            setRequiredCount(entry.getKey(), entry.getValue());
        }
    }

    public List<ShiftAssignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    public void setAssignments(List<ShiftAssignment> assignments) {
        this.assignments = new ArrayList<>(assignments != null ? assignments : Collections.emptyList());
    }

    public void addAssignment(ShiftAssignment assignment) {
        if (assignment != null) {
            assignments.add(assignment);
        }
    }

    public void removeAssignment(ShiftAssignment assignment) {
        assignments.remove(assignment);
    }

    public boolean isCancellationCardTransferred() {
        return cancellationCardTransferred;
    }

    public LocalDateTime getCancellationCardTransferTime() {
        return cancellationCardTransferTime;
    }

    public Employee getCancellationCardTransferredBy() {
        return cancellationCardTransferredBy;
    }

    /** Req #9: only the shift manager can transfer a cancellation card at the cash register. */
    public void transferCancellationCard(Employee requestedBy) {
        if (requestedBy == null) {
            throw new IllegalArgumentException("Requested by employee is required");
        }
        if (shiftManager == null) {
            throw new IllegalArgumentException("Shift manager is not assigned for this shift");
        }
        if (!shiftManager.getId().equals(requestedBy.getId())) {
            throw new IllegalArgumentException("Only the shift manager can transfer cancellation card");
        }
        if (requestedBy.isFired()) {
            throw new IllegalArgumentException("Fired employee cannot perform cancellation card transfer");
        }

        this.cancellationCardTransferred = true;
        this.cancellationCardTransferTime = LocalDateTime.now();
        this.cancellationCardTransferredBy = requestedBy;
    }

    @Override
    public String toString() {
        return "Shift{" +
            "date=" + date +
            ", shiftType=" + shiftType +
            ", shiftManager=" + shiftManager +
            ", requiredRoleCounts=" + requiredRoleCounts +
            ", assignments=" + assignments +
            ", cancellationCardTransferred=" + cancellationCardTransferred +
            ", cancellationCardTransferTime=" + cancellationCardTransferTime +
            ", cancellationCardTransferredBy=" + cancellationCardTransferredBy +
            '}';
    }

    private void initializeDefaultRoleRequirements() {
        requiredRoleCounts.clear();
        for (Role role : Role.values()) {
            requiredRoleCounts.put(role, 1);
        }
    }

    private int getRequiredCount(Role role) {
        return requiredRoleCounts.getOrDefault(role, 1);
    }

    private void setRequiredCount(Role role, Integer count) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        if (count == null || count < 1) {
            throw new IllegalArgumentException("Required count for role " + role + " must be at least 1");
        }
        requiredRoleCounts.put(role, count);
    }

    private void ensureHrManager(User selectedBy) {
        if (!(selectedBy instanceof HR_Manager) || !((HR_Manager) selectedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can configure required roles for a shift");
        }
    }
}
