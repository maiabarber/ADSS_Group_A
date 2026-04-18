package employees.presentation;

import employees.domain.ShiftType;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * ShiftPresentation class serves as a presentation layer for handling shift input and creating Shift objects.
 * It provides methods to read shift details from the console and convert them into a Shift object.
 */
public class ShiftPresentation {
    private LocalDate shiftDateInput;
    private ShiftType shiftTypeInput;
    private int requiredCashierInput;
    private int requiredStoreKeeperInput;
    private String selectManagerIdInput;

    public void readShiftInput(Scanner scanner) {
        System.out.print("Shift date (YYYY-MM-DD): ");
        shiftDateInput = LocalDate.parse(scanner.nextLine());

        System.out.println("Shift type:");
        System.out.println("1. MORNING (6-14)");
        System.out.println("2. MORNING_OVERTIME (6-16)");
        System.out.println("3. EVENING (14-22)");
        System.out.println("4. DOUBLE_SHIFT (full day)");
        System.out.print("Selection: ");
        String shiftTypeSelection = scanner.nextLine();

        if ("1".equals(shiftTypeSelection)) {
            shiftTypeInput = ShiftType.MORNING;
        } else if ("2".equals(shiftTypeSelection)) {
            shiftTypeInput = ShiftType.MORNING_OVERTIME;
        } else if ("3".equals(shiftTypeSelection)) {
            shiftTypeInput = ShiftType.EVENING;
        } else if ("4".equals(shiftTypeSelection)) {
            shiftTypeInput = ShiftType.DOUBLE_SHIFT;
        } else {
            System.out.println("Invalid selection, defaulting to MORNING");
            shiftTypeInput = ShiftType.MORNING;
        }
        requiredCashierInput = readPositiveInt(scanner, "Required cashiers: ");
        requiredStoreKeeperInput = readPositiveInt(scanner, "Required storekeepers: ");

        System.out.print("Shift manager id: ");
        selectManagerIdInput = scanner.nextLine();
    }

    private int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            try {
                int parsed = Integer.parseInt(value);
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Please enter a positive integer.");
        }
    }

    public LocalDate getShiftDateInput() {
        return shiftDateInput;
    }

    public ShiftType getShiftTypeInput() {
        return shiftTypeInput;
    }

    public int getRequiredCashierInput() {
        return requiredCashierInput;
    }

    public int getRequiredStoreKeeperInput() {
        return requiredStoreKeeperInput;
    }

    public String getSelectManagerIdInput() {
        return selectManagerIdInput;
    }
}
