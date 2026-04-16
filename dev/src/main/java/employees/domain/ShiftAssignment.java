package employees.domain;

public class ShiftAssignment {
    private Employee employee;
    private Role Role;

    public ShiftAssignment() {
    }

    public ShiftAssignment(Employee employee, Role Role) {
        this.employee = employee;
        this.Role = Role;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Role getJobRole() {
        return Role;
    }

    public void setJobRole(Role Role) {
        this.Role = Role;
    }

    @Override
    public String toString() {
        return "ShiftAssignment{" +
            "employee=" + employee +
            ", Role=" + Role +
            '}';
    }
}
