package employees.domain;

/**
 * EmploymentType enum represents the type of employment for an employee.
 * It includes STUDENT, PARENT, and REGULAR options.
 */
public enum EmploymentType {
    STUDENT,
    PARENT,
    REGULAR;

    public static EmploymentType fromSelection(String selection) {
        if (selection == null || selection.trim().isEmpty()) {
            throw new IllegalArgumentException("Employment type selection cannot be empty");
        }

        switch (selection.trim()) {
            case "1":
                return STUDENT;
            case "2":
                return PARENT;
            case "3":
                return REGULAR;
            default:
                throw new IllegalArgumentException("Employment type must be 1 (STUDENT), 2 (PARENT), or 3 (REGULAR)");
        }
    }
}
