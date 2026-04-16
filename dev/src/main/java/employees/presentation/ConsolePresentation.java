package employees.presentation;

import employees.domain.BankAccount;
import employees.domain.Employee;
import employees.domain.EmploymentType;
import employees.domain.HR_Manager;
import employees.domain.Role;
import employees.domain.Salary;
import employees.domain.User;
import employees.domain.WeeklyAvailabilityRequest;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;
import employees.repository.impl.InMemoryEmployeeRepository;
import employees.repository.impl.InMemoryUserRepository;
import employees.service.AuthenticationService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class ConsolePresentation {
    private final AuthenticationService authenticationService;
    private final EmployeeRepository employeeRepository;

    public ConsolePresentation() {
        this.authenticationService = new AuthenticationService(new InMemoryUserRepository());
        this.employeeRepository = new InMemoryEmployeeRepository();
    }

    public void run() {
        try {
            authenticationService.registerUser(new User("employee1", "pass123"));
            authenticationService.registerUser(new HR_Manager("hr001", "hrpass"));
        } catch (Exception e) {
            System.out.println("Failed to register demo users: " + e.getMessage());
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("User id: ");
            String id = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            Optional<User> loggedInUser = authenticationService.login(id, password);
            if (!loggedInUser.isPresent()) {
                System.out.println("Invalid credentials.");
                return;
            }

            System.out.println("Login successful. Welcome " + loggedInUser.get().getId() + ".");

            boolean running = true;
            while (running) {
                System.out.println("\nChoose action:");
                if (isHrManager(loggedInUser.get())) {
                    System.out.println("1. Add new employee");
                    System.out.println("2. Logout");
                    System.out.println("3. Exit");
                } else {
                    System.out.println("1. Logout");
                    System.out.println("2. Exit");
                }
                System.out.print("Selection: ");

                String choice = scanner.nextLine();
                if (isHrManager(loggedInUser.get())) {
                    switch (choice) {
                        case "1":
                            addNewEmployeeFlow(scanner);
                            break;
                        case "2":
                            if (authenticationService.logout()) {
                                System.out.println("You have been logged out.");
                            } else {
                                System.out.println("No user is currently logged in.");
                            }
                            running = false;
                            break;
                        case "3":
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid selection.");
                    }
                } else {
                    switch (choice) {
                        case "1":
                            if (authenticationService.logout()) {
                                System.out.println("You have been logged out.");
                            } else {
                                System.out.println("No user is currently logged in.");
                            }
                            running = false;
                            break;
                        case "2":
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid selection.");
                    }
                }
            }
        }
    }

    private boolean isHrManager(User user) {
        return user instanceof HR_Manager && ((HR_Manager) user).isHRManager();
    }

    private void addNewEmployeeFlow(Scanner scanner) {
        try {
            System.out.println("\nAdd new employee");
            System.out.print("Employee id: ");
            String id = scanner.nextLine();

            System.out.print("Employee password: ");
            String password = scanner.nextLine();

            System.out.print("Employee full name: ");
            String name = scanner.nextLine();

            EmploymentType employmentType = readEmploymentType(scanner);
            Set<Role> authorizedRoles = readAuthorizedRoles(scanner);
            boolean canManageShift = readYesNo(scanner, "Can manage shift? (y/n): ");

            System.out.print("Bank number: ");
            String bankNumber = scanner.nextLine();
            System.out.print("Branch number: ");
            String branchNumber = scanner.nextLine();
            System.out.print("Account number: ");
            String accountNumber = scanner.nextLine();

            double globalSalary = readDouble(scanner, "Base salary: ");
            double hourlySalary = readDouble(scanner, "Overtime hourly rate: ");

            Employee employee = new Employee(
                id,
                password,
                new BankAccount(bankNumber, branchNumber, accountNumber),
                name,
                new Salary(globalSalary, hourlySalary, 0),
                employmentType,
                LocalDate.now(),
                authorizedRoles,
                canManageShift,
                false,
                null,
                new WeeklyAvailabilityRequest()
            );

            authenticationService.registerUser(employee);
            employeeRepository.save(employee);
            System.out.println("Employee added successfully.");
        } catch (RepositoryException e) {
            System.out.println("Failed to add employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
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

    private Set<Role> readAuthorizedRoles(Scanner scanner) {
        Set<Role> roles = new HashSet<>();
        if (readYesNo(scanner, "Authorize CASHIER role? (y/n): ")) {
            roles.add(Role.CASHIER);
        }
        if (readYesNo(scanner, "Authorize STOREKEEPER role? (y/n): ")) {
            roles.add(Role.STOREKEEPER);
        }
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be selected");
        }
        return roles;
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
