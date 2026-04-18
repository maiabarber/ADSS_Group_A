package employees.domain;

/**
 * EmploymentScope enum represents the scope of employment for an employee.
 * It includes FULL_TIME and PART_TIME options, each with a required number of hours.
 */
public enum EmploymentScope {
    FULL_TIME(190),
    PART_TIME(95);

    private final int requiredHours;

    EmploymentScope(int requiredHours) {
        this.requiredHours = requiredHours;
    }

    public int getRequiredHours() {
        return requiredHours;
    }
}
