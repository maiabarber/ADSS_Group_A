package employees.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shift implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate date;
    private ShiftType shiftType;
    private Employee shiftManager;
    private int requiredCashiers;
    private int requiredStorekeepers;
    private List<ShiftAssignment> assignments;

    public Shift() {
        this.assignments = new ArrayList<>();
    }

    public Shift(LocalDate date, ShiftType shiftType, Employee shiftManager, int requiredCashiers, int requiredStorekeepers) {
        this.date = date;
        this.shiftType = shiftType;
        this.shiftManager = shiftManager;
        this.requiredCashiers = requiredCashiers;
        this.requiredStorekeepers = requiredStorekeepers;
        this.assignments = new ArrayList<>();
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
        this.shiftManager = shiftManager;
    }

    public int getRequiredCashiers() {
        return requiredCashiers;
    }

    public void setRequiredCashiers(int requiredCashiers) {
        this.requiredCashiers = requiredCashiers;
    }

    public int getRequiredStorekeepers() {
        return requiredStorekeepers;
    }

    public void setRequiredStorekeepers(int requiredStorekeepers) {
        this.requiredStorekeepers = requiredStorekeepers;
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

    @Override
    public String toString() {
        return "Shift{" +
            "date=" + date +
            ", shiftType=" + shiftType +
            ", shiftManager=" + shiftManager +
            ", requiredCashiers=" + requiredCashiers +
            ", requiredStorekeepers=" + requiredStorekeepers +
            ", assignments=" + assignments +
            '}';
    }
}
