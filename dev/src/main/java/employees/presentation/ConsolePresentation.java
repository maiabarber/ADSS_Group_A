package employees.presentation;

import employees.domain.Employee;
import employees.domain.HR_Manager;
import employees.domain.User;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;
import employees.repository.impl.InMemoryEmployeeRepository;
import employees.repository.impl.InMemoryUserRepository;
import employees.service.AuthenticationService;

import java.util.Optional;
import java.util.Scanner;

public class ConsolePresentation {
    private final AuthenticationService authenticationService;
    private final EmployeeRepository employeeRepository;
    private final LoginPresentation loginPresentation;
    private final EmployeePresentation employeePresentation;
    private final UserController userController;
    private final ShiftController shiftController;

    public ConsolePresentation() {
        this.authenticationService = new AuthenticationService(new InMemoryUserRepository());
        this.employeeRepository = new InMemoryEmployeeRepository();
        this.loginPresentation = new LoginPresentation();
        this.employeePresentation = new EmployeePresentation();
        this.userController = new UserController();
        this.shiftController = new ShiftController();
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
                    System.out.println("1. Add new employee");
                    System.out.println("2. Fire employee");
                    System.out.println("3. Logout");
                    System.out.println("4. Exit");
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
                            shiftController.setEmployees(userController.getEmployees());
                            break;
                        case "2":
                            fireEmployeeFlow(scanner);
                            shiftController.setEmployees(userController.getEmployees());
                            break;
                        case "3":
                            if (authenticationService.logout()) {
                                System.out.println("You have been logged out.");
                            } else {
                                System.out.println("No user is currently logged in.");
                            }
                            running = false;
                            break;
                        case "4":
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
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user logged in.");
                return;
            }

            Employee employee = employeePresentation.readEmployeeInput(scanner);
            userController.addEmployee(currentUser.get(), employee, authenticationService, employeeRepository);
            System.out.println("Employee added successfully.");
        } catch (RepositoryException e) {
            System.out.println("Failed to add employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Authorization failed: " + e.getMessage());
        }
    }

    private void fireEmployeeFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user logged in.");
                return;
            }

            System.out.print("Employee id to fire: ");
            String employeeId = scanner.nextLine();

            boolean fired = userController.fireEmployee(currentUser.get(), employeeId, authenticationService, employeeRepository);
            if (fired) {
                System.out.println("Employee marked as fired.");
            } else {
                System.out.println("Employee not found.");
            }
        } catch (RepositoryException e) {
            System.out.println("Failed to fire employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Authorization failed: " + e.getMessage());
        }
    }
}
