package employees.domain;

import java.io.Serializable;

public class ShiftAssignment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Employee employee;
    private Role role;

    public ShiftAssignment() {
    }

    public ShiftAssignment(Employee employee, Shift shift, Role role) {
        this.employee = employee;
        this.role = role;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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
            ", role=" + role +
            '}';
    }
}
