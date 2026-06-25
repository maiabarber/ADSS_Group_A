package employee.domain;

/**
 * Role enum represents the different roles that an employee can have in the system.
 * It currently includes CASHIER, STOREKEEPER, and DRIVER roles.
 */
public enum Role {
    CASHIER,
    STOREKEEPER,
    DRIVER;

    public static Role fromSelection(String selection) {
        if (selection == null || selection.trim().isEmpty()) {
            throw new IllegalArgumentException("Job role selection cannot be empty");
        }

        switch (selection.trim()) {
            case "1":
                return CASHIER;
            case "2":
                return STOREKEEPER;
            case "3":
                return DRIVER;
            default:
                throw new IllegalArgumentException("Job role must be 1 (CASHIER), 2 (STOREKEEPER), or 3 (DRIVER)");
        }
    }
}
