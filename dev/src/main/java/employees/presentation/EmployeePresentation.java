package employees.presentation;

import employees.domain.BankAccount;
import employees.domain.Employee;
import employees.domain.EmploymentScope;
import employees.domain.EmploymentTerms;
import employees.domain.EmploymentType;
import employees.domain.Role;
import employees.domain.Salary;
import employees.domain.WeeklyAvailabilityRequest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Scanner;

/**
 * EmployeePresentation class serves as a presentation layer for handling employee input and creating Employee objects.
 * It provides methods to read employee details from the console and convert them into an Employee object.
 */
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
    private EmploymentScope employmentScopeInput;
    private int vacationDaysInput;

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
        employmentScopeInput = readEmploymentScope(scanner);
        vacationDaysInput = readInt(scanner, "Vacation days: ");
        workedHoursInput = 0;
        startDateInput = readLocalDate(scanner, "Start date (YYYY-MM-DD): ");

        Salary salary = new Salary(globalSalaryInput, hourlySalaryInput, workedHoursInput, employmentScopeInput);
        EmploymentTerms employmentTerms = new EmploymentTerms(
            startDateInput,
            employmentScopeInput,
            globalSalaryInput,
            hourlySalaryInput,
            vacationDaysInput
        );

        Employee employee = new Employee(
            idInput,
            passwordInput,
            new BankAccount(bankNumberInput, branchNumberInput, accountNumberInput),
            nameInput,
            salary,
            employeeTypeInput,
            employmentTerms,
            Collections.singleton(jobRoleInput),
            canManageShiftInput,
            false,
            null,
            new WeeklyAvailabilityRequest()
        );
        return employee;
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

    private EmploymentScope readEmploymentScope(Scanner scanner) {
        while (true) {
            System.out.println("Employment scope:");
            System.out.println("1. FULL_TIME");
            System.out.println("2. PART_TIME");
            System.out.print("Selection: ");
            String value = scanner.nextLine();

            if ("1".equals(value)) {
                return EmploymentScope.FULL_TIME;
            }
            if ("2".equals(value)) {
                return EmploymentScope.PART_TIME;
            }

            System.out.println("Invalid selection.");
        }
    }

    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private LocalDate readLocalDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            try {
                return LocalDate.parse(value);
            } catch (Exception e) {
                System.out.println("Please enter a valid date in YYYY-MM-DD format.");
            }
        }
    }

}
