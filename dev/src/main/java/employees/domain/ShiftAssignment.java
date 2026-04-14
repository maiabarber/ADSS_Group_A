package employees.domain;

public class ShiftAssignment {
    private Employee employee;
    private JobRole jobRole;

    public ShiftAssignment() {
    }

    public ShiftAssignment(Employee employee, JobRole jobRole) {
        this.employee = employee;
        this.jobRole = jobRole;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public JobRole getJobRole() {
        return jobRole;
    }

    public void setJobRole(JobRole jobRole) {
        this.jobRole = jobRole;
    }

    @Override
    public String toString() {
        return "ShiftAssignment{" +
            "employee=" + employee +
            ", jobRole=" + jobRole +
            '}';
    }
}
