package employees.presentation;

import employees.domain.BankAccount;
import employees.domain.Employee;
import employees.domain.EmploymentScope;
import employees.domain.EmploymentTerms;
import employees.domain.EmploymentType;
import employees.domain.Role;
import employees.domain.Salary;
import employees.domain.User;
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

        while (true) {
            idInput = readEmployeeId(scanner);
            passwordInput = readPassword(scanner);
            nameInput = readEmployeeName(scanner);

            employeeTypeInput = readEmploymentType(scanner);
            jobRoleInput = readJobRole(scanner);
            canManageShiftInput = readYesNo(scanner, "Can manage shift? (y/n): ");

            bankNumberInput = readPositiveNumber(scanner, "Bank number: ");
            branchNumberInput = readPositiveNumber(scanner, "Branch number: ");
            accountNumberInput = readPositiveNumber(scanner, "Account number: ");

            globalSalaryInput = readPositiveDouble(scanner, "Base salary: ");
            hourlySalaryInput = readPositiveDouble(scanner, "Overtime hourly rate: ");
            employmentScopeInput = readEmploymentScope(scanner);
            workedHoursInput = 0;
            startDateInput = readStartDate(scanner, "Start date (YYYY-MM-DD): ");

            try {
                BankAccount bankAccount = new BankAccount(bankNumberInput, branchNumberInput, accountNumberInput);
                Salary salary = new Salary(globalSalaryInput, hourlySalaryInput, workedHoursInput, employmentScopeInput);
                EmploymentTerms employmentTerms = new EmploymentTerms(
                    startDateInput,
                    employmentScopeInput,
                    globalSalaryInput,
                    hourlySalaryInput,
                    DEFAULT_VACATION_DAYS
                );

                return new Employee(
                    idInput,
                    passwordInput,
                    bankAccount,
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
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please enter the employee details again.");
            }
        }
    }

    private String readEmployeeId(Scanner scanner) {
        while (true) {
            System.out.print("Employee id: ");
            String id = scanner.nextLine();
            try {
                User.validateIdInput(id);
                return id;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private String readPassword(Scanner scanner) {
        while (true) {
            System.out.print("Employee password: ");
            String password = scanner.nextLine();
            try {
                User.validatePasswordInput(password);
                return password;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private String readEmployeeName(Scanner scanner) {
        while (true) {
            System.out.print("Employee full name: ");
            String name = scanner.nextLine();
            try {
                Employee.validateName(name);
                return name;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private String readPositiveNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            
            try {
                validatePositiveNumberByField(prompt, value);
                return value;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void validatePositiveNumberByField(String prompt, String value) {
        switch (prompt) {
            case "Bank number: ":
                BankAccount.validateBankNumber(value);
                break;
            case "Branch number: ":
                BankAccount.validateBranchNumber(value);
                break;
            case "Account number: ":
                BankAccount.validateAccountNumber(value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported numeric field: " + prompt);
        }
    }

    private double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            
            try {
                return Salary.parseNonNegativeAmount(value, prompt.trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
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
            String value = scanner.nextLine().trim();

            try {
                return EmploymentType.fromSelection(value);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private Role readJobRole(Scanner scanner) {
        while (true) {
            System.out.println("Job role:");
            System.out.println("1. CASHIER");
            System.out.println("2. STOREKEEPER");
            System.out.print("Selection: ");
            String value = scanner.nextLine().trim();

            try {
                return Role.fromSelection(value);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private boolean readYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(value)) {
                return true;
            }
            if ("n".equals(value)) {
                return false;
            }
            System.out.println("Error: Please enter y or n.");
        }
    }

    private EmploymentScope readEmploymentScope(Scanner scanner) {
        while (true) {
            System.out.println("Employment scope:");
            System.out.println("1. FULL_TIME");
            System.out.println("2. PART_TIME");
            System.out.print("Selection: ");
            String value = scanner.nextLine().trim();

            try {
                return EmploymentScope.fromSelection(value);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private LocalDate readStartDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                LocalDate parsed = LocalDate.parse(value);
                EmploymentTerms.validateStartDate(parsed, LocalDate.now());
                return parsed;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

}
