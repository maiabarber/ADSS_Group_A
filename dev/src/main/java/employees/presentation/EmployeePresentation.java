package employees.presentation;

import employees.domain.BankAccount;
import employees.domain.Employee;
import employees.domain.EmploymentType;
import employees.domain.Role;
import employees.domain.Salary;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Scanner;

public class EmployeePresentation {
    private String idInput;
    private String passwordInput;
    private String nameInput;
    private String bankNumberInput;
    private String branchNumberInput;
    private String accountNumberInput;
    private EmploymentType employeeTypeInput;
    private LocalDate startDateInput;
    private Role jobRoleInput;
    private boolean canManageShiftInput;
    private double globalSalaryInput;
    private double hourlySalaryInput;
    private double workedHoursInput;

    public Employee readEmployeeInput(Scanner scanner) {
        System.out.println("\nAdd new employee");

        System.out.print("Employee id: ");
        idInput = scanner.nextLine();

        System.out.print("Employee password: ");
        passwordInput = scanner.nextLine();

        System.out.print("Employee full name: ");
        nameInput = scanner.nextLine();

        employeeTypeInput = readEmploymentType(scanner);
        jobRoleInput = readJobRole(scanner);
        canManageShiftInput = readYesNo(scanner, "Can manage shift? (y/n): ");

        System.out.print("Bank number: ");
        bankNumberInput = scanner.nextLine();

        System.out.print("Branch number: ");
        branchNumberInput = scanner.nextLine();

        System.out.print("Account number: ");
        accountNumberInput = scanner.nextLine();

        globalSalaryInput = readDouble(scanner, "Base salary: ");
        hourlySalaryInput = readDouble(scanner, "Overtime hourly rate: ");
        workedHoursInput = 0;
        startDateInput = LocalDate.now();

        return new Employee(
            idInput,
            passwordInput,
            new BankAccount(bankNumberInput, branchNumberInput, accountNumberInput),
            nameInput,
            new Salary(globalSalaryInput, hourlySalaryInput, workedHoursInput),
            employeeTypeInput,
            startDateInput,
            Collections.singleton(jobRoleInput),
            canManageShiftInput,
            false,
            null,
            new AvailabilityPresentation().toWeeklyAvailabilityRequest()
        );
    }

    private EmploymentType readEmploymentType(Scanner scanner) {
        while (true) {
            System.out.println("Employment type:");
            System.out.println("1. STUDENT");
            System.out.println("2. PARENT");
            System.out.println("3. REGULAR");
            System.out.print("Selection: ");
            String value = scanner.nextLine();

            if ("1".equals(value)) {
                return EmploymentType.STUDENT;
            }
            if ("2".equals(value)) {
                return EmploymentType.PARENT;
            }
            if ("3".equals(value)) {
                return EmploymentType.REGULAR;
            }

            System.out.println("Invalid selection.");
        }
    }

    private Role readJobRole(Scanner scanner) {
        while (true) {
            System.out.println("Job role:");
            System.out.println("1. CASHIER");
            System.out.println("2. STOREKEEPER");
            System.out.print("Selection: ");
            String value = scanner.nextLine();

            if ("1".equals(value)) {
                return Role.CASHIER;
            }
            if ("2".equals(value)) {
                return Role.STOREKEEPER;
            }

            System.out.println("Invalid selection.");
        }
    }

    private boolean readYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            if ("y".equalsIgnoreCase(value)) {
                return true;
            }
            if ("n".equalsIgnoreCase(value)) {
                return false;
            }
            System.out.println("Please enter y or n.");
        }
    }

    private double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
