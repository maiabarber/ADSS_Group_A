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

    public static EmploymentScope fromSelection(String selection) {
        if (selection == null || selection.trim().isEmpty()) {
            throw new IllegalArgumentException("Employment scope selection cannot be empty");
        }

        switch (selection.trim()) {
            case "1":
                return FULL_TIME;
            case "2":
                return PART_TIME;
            default:
                throw new IllegalArgumentException("Employment scope must be 1 (FULL_TIME) or 2 (PART_TIME)");
        }
    }
}
