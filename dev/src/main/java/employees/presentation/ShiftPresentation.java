package employees.presentation;

import employees.domain.ShiftType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ShiftPresentation {
    private LocalDate shiftDateInput;
    private ShiftType shiftTypeInput;
    private int requiredCashierInput;
    private int requiredStoreKeeperInput;
    private String selectManagerIdInput;
    private List<String> assignedEmployeeIdsInput = new ArrayList<>();

    public void readShiftInput(Scanner scanner) {
        System.out.print("Shift date (YYYY-MM-DD): ");
        shiftDateInput = LocalDate.parse(scanner.nextLine());

        System.out.println("Shift type:");
        System.out.println("1. MORNING");
        System.out.println("2. AFTERNOON");
        System.out.println("3. NIGHT");
        System.out.print("Selection: ");
        String shiftTypeSelection = scanner.nextLine();

        if ("1".equals(shiftTypeSelection)) {
            shiftTypeInput = ShiftType.MORNING;
        } else if ("2".equals(shiftTypeSelection)) {
            shiftTypeInput = ShiftType.AFTERNOON;
        } else {
            shiftTypeInput = ShiftType.NIGHT;
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

    public List<String> getAssignedEmployeeIdsInput() {
        return assignedEmployeeIdsInput;
    }
}
