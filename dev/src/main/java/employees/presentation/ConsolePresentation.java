package employees.presentation;

import employees.domain.Employee;
import employees.domain.HR_Manager;
import employees.domain.User;
import employees.domain.WeeklyAvailabilityRequest;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;
import employees.repository.SubmissionDeadlineRepository;
import employees.repository.impl.InMemoryEmployeeRepository;
import employees.repository.impl.InMemorySubmissionDeadlineRepository;
import employees.repository.impl.InMemoryUserRepository;
import employees.service.AuthenticationService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

public class ConsolePresentation {
    private final AuthenticationService authenticationService;
    private final EmployeeRepository employeeRepository;
    private final LoginPresentation loginPresentation;
    private final EmployeePresentation employeePresentation;
    private final UserController userController;
    private final ShiftController shiftController;
    private final SubmissionDeadlineRepository submissionDeadlineRepository;

    public ConsolePresentation() {
        this.authenticationService = new AuthenticationService(new InMemoryUserRepository());
        this.employeeRepository = new InMemoryEmployeeRepository();
        this.loginPresentation = new LoginPresentation();
        this.employeePresentation = new EmployeePresentation();
        this.userController = new UserController();
        this.shiftController = new ShiftController();
        this.submissionDeadlineRepository = new InMemorySubmissionDeadlineRepository();
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
            loginPresentation.readLoginInput(scanner);

            Optional<User> loggedInUser = authenticationService.login(
                loginPresentation.getIdInput(),
                loginPresentation.getPasswordInput()
            );
            if (!loggedInUser.isPresent()) {
                System.out.println("Invalid credentials.");
                return;
            }

            if (isHrManager(loggedInUser.get())) {
                userController.setManager((HR_Manager) loggedInUser.get());
            }

            System.out.println("Login successful. Welcome " + loggedInUser.get().getId() + ".");

            boolean running = true;
            while (running) {
                System.out.println("\nChoose action:");
                if (isHrManager(loggedInUser.get())) {
                    System.out.println("1. Set weekly submission deadline");
                    System.out.println("2. Add new employee");
                    System.out.println("3. Update employee details");
                    System.out.println("4. Fire employee");
                    System.out.println("5. Logout");
                } else {
                    System.out.println("1. Logout");
                }
                System.out.print("Selection: ");

                String choice = scanner.nextLine();
                if (isHrManager(loggedInUser.get())) {
                    switch (choice) {
                        case "1":
                            setSubmissionDeadlineFlow(scanner);
                            break;
                        case "2":
                            addNewEmployeeFlow(scanner);
                            break;
                        case "3":
                            updateEmployeeDetailsFlow(scanner);
                            break;
                        case "4":
                            fireEmployeeFlow(scanner);
                            shiftController.setEmployees(userController.getEmployees());
                            break;
                        case "5":
                            if (authenticationService.logout()) {
                                System.out.println("You have been logged out.");
                            } else {
                                System.out.println("No user is currently logged in.");
                            }
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
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            Employee employee = employeePresentation.readEmployeeInput(scanner);
            Optional<LocalDate> configuredDeadline = submissionDeadlineRepository.findCurrent();
            if (configuredDeadline.isPresent()) {
                WeeklyAvailabilityRequest weeklyAvailabilityRequest = employee.getWeeklyAvailabilityRequest();
                if (weeklyAvailabilityRequest == null) {
                    weeklyAvailabilityRequest = new WeeklyAvailabilityRequest();
                    employee.setWeeklyAvailabilityRequest(weeklyAvailabilityRequest);
                }
                weeklyAvailabilityRequest.setSubmissionDeadline(configuredDeadline.get());
            }
            userController.addEmployee(currentUser.get(), employee, authenticationService, employeeRepository);
            System.out.println("Employee added successfully.");
        } catch (RepositoryException e) {
            System.out.println("Failed to add employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Not authorized: " + e.getMessage());
        }
    }

    private void setSubmissionDeadlineFlow(Scanner scanner) {
        LocalDate newDeadline = readLocalDate(
            scanner,
            "Deadline for submitting constraints and preferences for the upcoming week (YYYY-MM-DD): "
        );

        try {
            submissionDeadlineRepository.save(newDeadline);
            System.out.println("Weekly submission deadline was set to " + newDeadline + ".");
        } catch (RepositoryException e) {
            System.out.println("Failed to save weekly submission deadline: " + e.getMessage());
        }
    }

    private void updateEmployeeDetailsFlow(Scanner scanner) {
        Optional<User> currentUser = authenticationService.getCurrentUser();
        if (!currentUser.isPresent()) {
            System.out.println("No user is currently logged in.");
            return;
        }

        System.out.print("Enter employee ID: ");
        String employeeId = scanner.nextLine();

        Optional<Employee> employeeOptional;
        try {
            employeeOptional = employeeRepository.findById(employeeId);
        } catch (RepositoryException e) {
            System.out.println("Failed to load employee: " + e.getMessage());
            return;
        }

        if (!employeeOptional.isPresent()) {
            System.out.println("Employee not found.");
            return;
        }

        Employee employee = employeeOptional.get();

        String newName = employee.getName();
        Double newGlobalSalary = employee.getSalary().getGlobalSalary();
        Double newHourlySalary = employee.getSalary().getHourlySalary();
        Boolean newCanManageShift = employee.canManageShift();

        boolean choosingFields = true;
        while (choosingFields) {
            System.out.println("Choose what to update:");
            System.out.println("1. Name");
            System.out.println("2. Global salary");
            System.out.println("3. Hourly salary");
            System.out.println("4. Can manage shifts");
            System.out.println("5. Finish");
            System.out.print("Selection: ");

            String updateChoice = scanner.nextLine();
            switch (updateChoice) {
                case "1":
                    System.out.print("Enter new name (current: " + newName + "): ");
                    String nameInput = scanner.nextLine();
                    if (!nameInput.isEmpty()) {
                        newName = nameInput;
                    }
                    break;
                case "2":
                    System.out.print("Enter new global salary (current: " + newGlobalSalary + "): ");
                    String globalSalaryInput = scanner.nextLine();
                    if (!globalSalaryInput.isEmpty()) {
                        try {
                            newGlobalSalary = Double.parseDouble(globalSalaryInput);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid global salary.");
                            return;
                        }
                    }
                    break;
                case "3":
                    System.out.print("Enter new hourly salary (current: " + newHourlySalary + "): ");
                    String hourlySalaryInput = scanner.nextLine();
                    if (!hourlySalaryInput.isEmpty()) {
                        try {
                            newHourlySalary = Double.parseDouble(hourlySalaryInput);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid hourly salary.");
                            return;
                        }
                    }
                    break;
                case "4":
                    System.out.print("Can manage shifts? (current: " + newCanManageShift + ", true/false): ");
                    String canManageShiftInput = scanner.nextLine();
                    if (!canManageShiftInput.isEmpty()) {
                        if (!"true".equalsIgnoreCase(canManageShiftInput) &&
                            !"false".equalsIgnoreCase(canManageShiftInput)) {
                            System.out.println("Invalid value. Please enter true or false.");
                            return;
                        }
                        newCanManageShift = Boolean.parseBoolean(canManageShiftInput);
                    }
                    break;
                case "5":
                    choosingFields = false;
                    break;
                default:
                    System.out.println("Invalid selection.");
            }
        }

        try {
            boolean updated = userController.updateEmployeeDetails(
                currentUser.get(),
                employee,
                newName,
                newGlobalSalary,
                newHourlySalary,
                newCanManageShift,
                employeeRepository
            );

            if (updated) {
                System.out.println("Employee details updated successfully.");
            } else {
                System.out.println("Employee not found.");
            }
        } catch (RepositoryException e) {
            System.out.println("Failed to update employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Not authorized: " + e.getMessage());
        }
    }

    private void fireEmployeeFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            System.out.print("Employee id to fire: ");
            String employeeId = scanner.nextLine();

            boolean fired = userController.fireEmployee(
                currentUser.get(),
                employeeId,
                authenticationService,
                employeeRepository
            );

            if (fired) {
                System.out.println("Employee marked as fired.");
            } else {
                System.out.println("Employee not found.");
            }
        } catch (RepositoryException e) {
            System.out.println("Failed to fire employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Not authorized: " + e.getMessage());
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