package employees.presentation;

import employees.domain.Constraint;
import employees.domain.Employee;
import employees.domain.HR_Manager;
import employees.domain.Preference;
import employees.domain.Role;
import employees.domain.Shift;
import employees.domain.ShiftType;
import employees.domain.User;
import employees.domain.WeeklyAvailabilityRequest;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;
import employees.repository.SubmissionDeadlineRepository;
import employees.repository.impl.InMemoryEmployeeRepository;
import employees.repository.impl.InMemorySubmissionDeadlineRepository;
import employees.repository.impl.InMemoryUserRepository;
import employees.service.AuthenticationService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
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
            } else if (loggedInUser.get() instanceof Employee) {
                Employee employee = (Employee) loggedInUser.get();
                promptForFixedDayOffIfNeeded(employee, scanner);
                ensureWeeklyAvailabilityCurrent(employee);
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
                    System.out.println("5. Approve employee as shift manager");
                    System.out.println("6. Assign employee to shift");
                    System.out.println("7. Logout");
                } else {
                    System.out.println("1. Submit weekly constraints and preferences");
                    System.out.println("2. Logout");
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
                            approveAsShiftManagerFlow(scanner);
                            break;
                        case "6":
                            assignEmployeeToShiftFlow(scanner);
                            break;
                        case "7":
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
                            submitWeeklyAvailabilityFlow(loggedInUser.get(), scanner);
                            break;
                        case "2":
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
            ensureWeeklyAvailabilityCurrent(employee);
            userController.addEmployee(currentUser.get(), employee, authenticationService, employeeRepository);
            System.out.println("Employee added successfully.");
        } catch (RepositoryException e) {
            System.out.println("Failed to add employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Not authorized: " + e.getMessage());
        }
    }

private void submitWeeklyAvailabilityFlow(User loggedInUser, Scanner scanner) {
    if (!(loggedInUser instanceof Employee)) {
        System.out.println("Only employees can submit constraints and preferences.");
        return;
    }

    Employee employee = (Employee) loggedInUser;
    ensureWeeklyAvailabilityCurrent(employee);

    WeeklyAvailabilityRequest weeklyAvailabilityRequest = employee.getWeeklyAvailabilityRequest();
    if (weeklyAvailabilityRequest == null) {
        weeklyAvailabilityRequest = new WeeklyAvailabilityRequest();
        employee.setWeeklyAvailabilityRequest(weeklyAvailabilityRequest);
        ensureWeeklyAvailabilityCurrent(employee);
    }

    List<Constraint> constraints = readConstraints(scanner);
    List<Preference> preferences = readPreferences(scanner);
    weeklyAvailabilityRequest.setConstraints(constraints);
    weeklyAvailabilityRequest.setPreferences(preferences);

    try {
        employeeRepository.save(employee);
        System.out.println("Weekly constraints and preferences were submitted successfully.");
    } catch (RepositoryException e) {
        System.out.println("Failed to save weekly submission: " + e.getMessage());
    }
}

private void promptForFixedDayOffIfNeeded(User loggedInUser, Scanner scanner) {
    if (!(loggedInUser instanceof Employee)) {
        return;
    }

    Employee employee = (Employee) loggedInUser;
    if (employee.getFixedDayOff() != null) {
        return;
    }

    DayOfWeek fixedDayOff = readFixedDayOff(scanner);
    employee.setFixedDayOff(fixedDayOff);

    try {
        employeeRepository.save(employee);
        System.out.println("Your fixed day off was set to " + fixedDayOff + ".");
    } catch (RepositoryException e) {
        System.out.println("Failed to save fixed day off: " + e.getMessage());
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

    private void ensureWeeklyAvailabilityCurrent(Employee employee) {
        WeeklyAvailabilityRequest weeklyAvailabilityRequest = employee.getWeeklyAvailabilityRequest();
        if (weeklyAvailabilityRequest == null) {
            weeklyAvailabilityRequest = new WeeklyAvailabilityRequest();
            employee.setWeeklyAvailabilityRequest(weeklyAvailabilityRequest);
        }

        LocalDate currentWeekStart = getCurrentWeekStartDate();
        LocalDate requestWeekStart = weeklyAvailabilityRequest.getWeekStartDate();
        if (requestWeekStart == null || !requestWeekStart.equals(currentWeekStart)) {
            weeklyAvailabilityRequest.resetForWeek(currentWeekStart);
        }
    }

    private LocalDate getCurrentWeekStartDate() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private List<Constraint> readConstraints(Scanner scanner) {
        int count = readInt(scanner, "Number of constraints: ");
        List<Constraint> constraints = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            System.out.println("Constraint #" + (i + 1) + ":");
            DayOfWeek day = readDayOfWeek(scanner);
            ShiftType shiftType = readShiftType(scanner);
            constraints.add(new Constraint(day, shiftType));
        }

        return constraints;
    }

    private List<Preference> readPreferences(Scanner scanner) {
        int count = readInt(scanner, "Number of preferences: ");
        List<Preference> preferences = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            System.out.println("Preference #" + (i + 1) + ":");
            DayOfWeek day = readDayOfWeek(scanner);
            ShiftType shiftType = readShiftType(scanner);
            preferences.add(new Preference(day, shiftType));
        }

        return preferences;
    }

    private DayOfWeek readDayOfWeek(Scanner scanner) {
        while (true) {
            System.out.println("Day of week:");
            System.out.println("1. MONDAY");
            System.out.println("2. TUESDAY");
            System.out.println("3. WEDNESDAY");
            System.out.println("4. THURSDAY");
            System.out.println("5. FRIDAY");
            System.out.println("6. SATURDAY");
            System.out.println("7. SUNDAY");
            System.out.print("Selection: ");

            String value = scanner.nextLine();
            switch (value) {
                case "1":
                    return DayOfWeek.MONDAY;
                case "2":
                    return DayOfWeek.TUESDAY;
                case "3":
                    return DayOfWeek.WEDNESDAY;
                case "4":
                    return DayOfWeek.THURSDAY;
                case "5":
                    return DayOfWeek.FRIDAY;
                case "6":
                    return DayOfWeek.SATURDAY;
                case "7":
                    return DayOfWeek.SUNDAY;
                default:
                    System.out.println("Invalid selection.");
            }
        }
    }

    private ShiftType readShiftType(Scanner scanner) {
        while (true) {
            System.out.println("Shift type:");
            System.out.println("1. MORNING (6-14)");
            System.out.println("2. MORNING_OVERTIME (6-16)");
            System.out.println("3. EVENING (14-22)");
            System.out.println("4. DOUBLE_SHIFT (full day)");
            System.out.print("Selection: ");

            String value = scanner.nextLine();
            switch (value) {
                case "1":
                    return ShiftType.MORNING;
                case "2":
                    return ShiftType.MORNING_OVERTIME;
                case "3":
                    return ShiftType.EVENING;
                case "4":
                    return ShiftType.DOUBLE_SHIFT;
                default:
                    System.out.println("Invalid selection.");
            }
        }
    }

    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();

            try {
                int parsed = Integer.parseInt(value);
                if (parsed < 0) {
                    System.out.println("Please enter a non-negative integer.");
                    continue;
                }
                return parsed;
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

    private DayOfWeek readFixedDayOff(Scanner scanner) {
        while (true) {
            System.out.println("Choose your fixed day off:");
            System.out.println("1. MONDAY");
            System.out.println("2. TUESDAY");
            System.out.println("3. WEDNESDAY");
            System.out.println("4. THURSDAY");
            System.out.println("5. FRIDAY");
            System.out.println("6. SATURDAY");
            System.out.println("7. SUNDAY");
            System.out.print("Selection: ");
            String value = scanner.nextLine();

            switch (value) {
                case "1":
                    return DayOfWeek.MONDAY;
                case "2":
                    return DayOfWeek.TUESDAY;
                case "3":
                    return DayOfWeek.WEDNESDAY;
                case "4":
                    return DayOfWeek.THURSDAY;
                case "5":
                    return DayOfWeek.FRIDAY;
                case "6":
                    return DayOfWeek.SATURDAY;
                case "7":
                    return DayOfWeek.SUNDAY;
                default:
                    System.out.println("Invalid selection.");
            }
        }
    }

    private void approveAsShiftManagerFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            System.out.println("\n=== Approve Employee as Shift Manager ===");
            System.out.println("Available employees:");
            List<Employee> employees = userController.getEmployees();
            
            if (employees.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }
            
            for (int i = 0; i < employees.size(); i++) {
                Employee emp = employees.get(i);
                String status = emp.canManageShift() ? "(Already approved)" : "(Not approved)";
                System.out.println((i + 1) + ". " + emp.getId() + " - " + emp.getName() + " " + status);
            }
            
            System.out.print("Select employee number: ");
            int choice = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (choice < 0 || choice >= employees.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            
            Employee selectedEmployee = employees.get(choice);
            userController.approveAsShiftManager(currentUser.get(), selectedEmployee.getId(), employeeRepository);
            System.out.println("Employee " + selectedEmployee.getName() + " has been approved as shift manager.");
            
        } catch (RepositoryException e) {
            System.out.println("Failed to approve employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Not authorized: " + e.getMessage());
        }
    }

    private void assignEmployeeToShiftFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            System.out.println("\n=== Assign Employee to Shift ===");

            // Show available employees
            List<Employee> employees = userController.getEmployees();
            if (employees.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }

            System.out.println("Available employees:");
            for (int i = 0; i < employees.size(); i++) {
                Employee emp = employees.get(i);
                System.out.println((i + 1) + ". " + emp.getId() + " - " + emp.getName());
            }

            System.out.print("Select employee number: ");
            int employeeChoice = Integer.parseInt(scanner.nextLine()) - 1;

            if (employeeChoice < 0 || employeeChoice >= employees.size()) {
                System.out.println("Invalid employee selection.");
                return;
            }

            Employee selectedEmployee = employees.get(employeeChoice);

            // Show available shifts
            List<Shift> shifts = shiftController.getShifts();
            if (shifts.isEmpty()) {
                System.out.println("No shifts found.");
                return;
            }

            System.out.println("Available shifts:");
            for (int i = 0; i < shifts.size(); i++) {
                Shift shift = shifts.get(i);
                System.out.println((i + 1) + ". " + shift.getDate() + " - " + shift.getShiftType());
            }

            System.out.print("Select shift number: ");
            int shiftChoice = Integer.parseInt(scanner.nextLine()) - 1;

            if (shiftChoice < 0 || shiftChoice >= shifts.size()) {
                System.out.println("Invalid shift selection.");
                return;
            }

            Shift selectedShift = shifts.get(shiftChoice);

            // Select role
            System.out.println("Select role:");
            System.out.println("1. CASHIER");
            System.out.println("2. STOREKEEPER");
            System.out.print("Selection: ");
            String roleChoice = scanner.nextLine();

            Role selectedRole;
            if ("1".equals(roleChoice)) {
                selectedRole = Role.CASHIER;
            } else if ("2".equals(roleChoice)) {
                selectedRole = Role.STOREKEEPER;
            } else {
                System.out.println("Invalid role selection.");
                return;
            }

            // Perform assignment
            shiftController.assignEmployeeToShift(currentUser.get(), selectedEmployee, selectedShift, selectedRole);
            System.out.println("Employee " + selectedEmployee.getName() + " has been assigned to shift " +
                             selectedShift.getDate() + " - " + selectedShift.getShiftType() + " as " + selectedRole);

        } catch (IllegalArgumentException e) {
            System.out.println("Assignment failed: " + e.getMessage());
        } 
    }
}