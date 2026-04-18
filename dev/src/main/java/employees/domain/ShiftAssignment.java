package employees.domain;

import java.io.Serializable;

public class ShiftAssignment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Employee employee;
    private Shift shift;
    private Role role;
    private boolean approved;
    private boolean requiresApproval;
    private boolean cancellationRequested;

    public ShiftAssignment() {
        this.approved = false;
        this.requiresApproval = false;
        this.cancellationRequested = false;
    }

    public ShiftAssignment(Employee employee, Shift shift, Role role) {
        this.employee = employee;
        this.shift = shift;
        this.role = role;
        this.approved = false;
        this.requiresApproval = false;
        this.cancellationRequested = false;
    }

    public ShiftAssignment(Employee employee, Shift shift, Role role, boolean requiresApproval) {
        this.employee = employee;
        this.shift = shift;
        this.role = role;
        this.approved = false;
        this.requiresApproval = requiresApproval;
        this.cancellationRequested = false;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    /** Returns true if the assignment is waiting for the employee to respond */
    public boolean isPending() {
        return requiresApproval && !approved;
    }

    public boolean isCancellationRequested() {
        return cancellationRequested;
    }

    public void setCancellationRequested(boolean cancellationRequested) {
        this.cancellationRequested = cancellationRequested;
    }

    @Override
    public String toString() {
        return "ShiftAssignment{" +
            "employee=" + employee +
            ", shift=" + shift +
            ", role=" + role +
            ", requiresApproval=" + requiresApproval +
            ", approved=" + approved +
            ", cancellationRequested=" + cancellationRequested +
            '}';
    }
}
