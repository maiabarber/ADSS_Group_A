package employees.domain;

/**
 * Role enum represents the different roles that an employee can have in the system.
 * It currently includes CASHIER and STOREKEEPER roles.
 */
public enum Role {
    CASHIER,
    STOREKEEPER;

    public static Role fromSelection(String selection) {
        if (selection == null || selection.trim().isEmpty()) {
            throw new IllegalArgumentException("Job role selection cannot be empty");
        }

        switch (selection.trim()) {
            case "1":
                return CASHIER;
            case "2":
                return STOREKEEPER;
            default:
                throw new IllegalArgumentException("Job role must be 1 (CASHIER) or 2 (STOREKEEPER)");
        }
    }
}
