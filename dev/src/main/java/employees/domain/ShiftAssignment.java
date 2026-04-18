package employees.domain;

import java.io.Serializable;

public class ShiftAssignment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Employee employee;
    private Shift shift;
    private Role role;
    private boolean approved;
    private boolean requiresApproval;

    public ShiftAssignment() {
        this.approved = false;
        this.requiresApproval = false;
    }

    public ShiftAssignment(Employee employee, Shift shift, Role role) {
        this.employee = employee;
        this.shift = shift;
        this.role = role;
        this.approved = false;
        this.requiresApproval = false;
    }

    public ShiftAssignment(Employee employee, Shift shift, Role role, boolean requiresApproval) {
        this.employee = employee;
        this.shift = shift;
        this.role = role;
        this.approved = false;
        this.requiresApproval = requiresApproval;
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

    @Override
    public String toString() {
        return "ShiftAssignment{" +
            "employee=" + employee +
            ", shift=" + shift +
            ", role=" + role +
            '}';
    }
}
