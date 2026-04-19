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
    private static final int DEFAULT_VACATION_DAYS = 10;

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

    public Employee readEmployeeInput(Scanner scanner) {
        System.out.println("\nAdd new employee");

        idInput = readEmployeeId(scanner);
        passwordInput = readPassword(scanner);
        nameInput = readNonEmptyString(scanner, "Employee full name: ");

        employeeTypeInput = readEmploymentType(scanner);
        jobRoleInput = readJobRole(scanner);
        canManageShiftInput = readYesNo(scanner, "Can manage shift? (y/n): ");

        bankNumberInput = readNonEmptyString(scanner, "Bank number: ");
        branchNumberInput = readNonEmptyString(scanner, "Branch number: ");
        accountNumberInput = readNonEmptyString(scanner, "Account number: ");

        globalSalaryInput = readPositiveDouble(scanner, "Base salary: ");
        hourlySalaryInput = readPositiveDouble(scanner, "Overtime hourly rate: ");
        employmentScopeInput = readEmploymentScope(scanner);
        workedHoursInput = 0;
        startDateInput = readLocalDate(scanner, "Start date (YYYY-MM-DD): ");

        Salary salary = new Salary(globalSalaryInput, hourlySalaryInput, workedHoursInput, employmentScopeInput);
        EmploymentTerms employmentTerms = new EmploymentTerms(
            startDateInput,
            employmentScopeInput,
            globalSalaryInput,
            hourlySalaryInput,
            DEFAULT_VACATION_DAYS
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

    private String readEmployeeId(Scanner scanner) {
        while (true) {
            System.out.print("Employee id: ");
            String id = scanner.nextLine().trim();
            
            if (id.isEmpty()) {
                System.out.println("Error: Employee ID cannot be empty.");
                continue;
            }
            
            if (!id.matches("\\d{9}")) {
                System.out.println("Error: Employee ID must be exactly 9 digits.");
                continue;
            }
            
            return id;
        }
    }

    private String readPassword(Scanner scanner) {
        while (true) {
            System.out.print("Employee password: ");
            String password = scanner.nextLine();
            
            if (password.isEmpty()) {
                System.out.println("Error: Password cannot be empty.");
                continue;
            }
            return password;
        }
    }

    private String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            
            if (value.isEmpty()) {
                System.out.println("Error: This field cannot be empty.");
                continue;
            }
            
            return value;
        }
    }

    private double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            
            if (value.isEmpty()) {
                System.out.println("Error: This field cannot be empty.");
                continue;
            }
            
            try {
                double parsed = Double.parseDouble(value);
                if (parsed < 0) {
                    System.out.println("Error: Value must be non-negative.");
                    continue;
                }
                return parsed;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
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
