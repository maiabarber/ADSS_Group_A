package employee.presentation;

import employee.domain.BankAccount;
import employee.domain.Constraint;
import employee.domain.Employee;
import employee.domain.EmploymentScope;
import employee.domain.EmploymentTerms;
import employee.domain.EmploymentType;
import employee.domain.HR_Manager;
import employee.domain.Preference;
import employee.domain.Role;
import employee.domain.Salary;
import employee.domain.Shift;
import employee.domain.ShiftAssignment;
import employee.domain.ShiftType;
import employee.domain.DriverAssignmentRequest;
import employee.domain.Branch;
import employee.domain.BranchManager;
import employee.domain.User;
import employee.domain.WeeklyAvailabilityRequest;
import employee.repository.EmployeeRepository;
import employee.repository.RepositoryException;
import employee.repository.ShiftRepository;
import employee.repository.SubmissionDeadlineRepository;
import dataaccess.repository.impl.DatabaseEmployeeRepository;
import dataaccess.repository.impl.DatabaseShiftRepository;
import dataaccess.repository.impl.DatabaseSubmissionDeadlineRepository;
import dataaccess.repository.impl.DatabaseUserRepository;
import employee.service.AuthenticationService;
import employee.service.SubmissionDeadlineService;
import employee.service.WeeklyAvailabilityService;
import employee.service.EmployeeTransportationService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import transportation.domain.Site;
import transportation.domain.SiteType;
import transportation.domain.ShippingZone;

/**
 * ConsolePresentation class provides a console-based user interface for the
 * employee scheduling system.
 * It allows users to log in, submit their weekly availability, and perform
 * various actions based on their role (HR manager or employee).
 * The class interacts with the authentication service, employee repository, and
 * other controllers to manage user input and system operations.
 */
public class ConsolePresentation {
    private static final int DEFAULT_ANNUAL_VACATION_DAYS = 10;
    private final AuthenticationService authenticationService;
    private final EmployeeRepository employeeRepository;
    private final LoginPresentation loginPresentation;
    private final EmployeePresentation employeePresentation;
    private final ShiftPresentation shiftPresentation;
    private final UserController userController;
    private final ShiftController shiftController;
    private final ShiftRepository shiftRepository;
    private final SubmissionDeadlineRepository submissionDeadlineRepository;
    private final SubmissionDeadlineService submissionDeadlineService;
    private final WeeklyAvailabilityService weeklyAvailabilityService;
    private final EmployeeTransportationService employeeTransportationService;
    private final BranchManager branchManager;
    private Branch activeBranch;
    private int lastVacationResetYear = -1;

    public ConsolePresentation() {
        this.authenticationService = new AuthenticationService(new DatabaseUserRepository());
        this.employeeRepository = new DatabaseEmployeeRepository();
        this.loginPresentation = new LoginPresentation();
        this.employeePresentation = new EmployeePresentation();
        this.shiftPresentation = new ShiftPresentation();
        this.userController = new UserController(authenticationService, employeeRepository);
        this.shiftController = new ShiftController();
        this.shiftRepository = new DatabaseShiftRepository();
        this.submissionDeadlineRepository = new DatabaseSubmissionDeadlineRepository();
        this.submissionDeadlineService = new SubmissionDeadlineService();
        this.weeklyAvailabilityService = new WeeklyAvailabilityService(submissionDeadlineRepository,
                employeeRepository);
        this.employeeTransportationService = new EmployeeTransportationService(shiftRepository, employeeRepository);
        this.branchManager = new BranchManager();
        this.activeBranch = null;
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean appRunning = true;
            while (appRunning) {
                System.out.println("\nWelcome to the \"Super Lee\" employee scheduling system!");
                loginPresentation.readLoginInput(scanner);

                Optional<User> loggedInUser = authenticationService.login(
                        loginPresentation.getIdInput(),
                        loginPresentation.getPasswordInput());
                if (!loggedInUser.isPresent()) {
                    if (authenticationService.isFiredCredentials(
                            loginPresentation.getIdInput(),
                            loginPresentation.getPasswordInput())) {
                        System.out.println("Login blocked: this employee is marked as fired.");
                        continue;
                    }
                    System.out.println("Invalid credentials.");
                    continue;
                }

                resetVacationDaysForCurrentYear();

                if (isHrManager(loggedInUser.get())) {
                    userController.setManager((HR_Manager) loggedInUser.get());
                } else if (loggedInUser.get() instanceof Employee) {
                    Employee employee = (Employee) loggedInUser.get();
                    promptForFixedDayOffIfNeeded(employee, scanner);
                    ensureWeeklyAvailabilityCurrent(employee);
                }

                System.out.println("Login successful. Welcome " + loggedInUser.get().getId() + ".");

                boolean sessionActive = true;
                while (sessionActive && appRunning) {
                    System.out.println("\nChoose action:");
                    if (activeBranch != null) {
                        System.out.println("Current branch workspace: " + activeBranch.getBranchName());
                    }
                    if (isHrManager(loggedInUser.get())) {
                        System.out.println("1. Set weekly submission deadline");
                        System.out.println("2. Add new employee");
                        System.out.println("3. Update employee details");
                        System.out.println("4. Fire employee");
                        System.out.println("5. Approve employee as shift manager");
                        System.out.println("6. Create shift");
                        System.out.println("7. Assign employee to shift");
                        System.out.println("8. Substitute employee in shift");
                        System.out.println("9. Handle cancellation requests");
                        System.out.println("10. Calculate employee salary from shifts");
                        System.out.println("11. Logout");
                        System.out.println("12. Exit");
                        System.out.println("13. Handle driver assignment requests");
                        System.out.println("14. Manage branches / select branch workspace");
                    } else {
                        System.out.println("1. Submit weekly constraints and preferences");
                        System.out.println("2. View and respond to pending shift assignments");
                        System.out.println("3. Request shift cancellation");
                        System.out.println("4. Transfer cancellation card at cash register");
                        System.out.println("5. Logout");
                        System.out.println("6. Exit");
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
                                break;
                            case "5":
                                approveAsShiftManagerFlow(scanner);
                                break;
                            case "6":
                                createShiftFlow(scanner);
                                break;
                            case "7":
                                assignEmployeeToShiftFlow(scanner);
                                break;
                            case "8":
                                substituteEmployeeFlow(scanner);
                                break;
                            case "9":
                                handleCancellationRequestsFlow(scanner);
                                break;
                            case "10":
                                calculateEmployeeSalaryFlow(scanner);
                                break;
                            case "11":
                                if (authenticationService.logout()) {
                                    System.out.println("You have been logged out.");
                                } else {
                                    System.out.println("No user is currently logged in.");
                                }
                                sessionActive = false;
                                break;
                            case "12":
                                authenticationService.logout();
                                System.out.println("You have been logged out.");
                                System.out.println("Application terminated.");
                                sessionActive = false;
                                appRunning = false;
                                break;
                            case "13":
                                handleDriverAssignmentRequestsFlow(scanner);
                                break;
                            case "14":
                                manageBranchWorkspaceFlow(scanner);
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
                                respondToPendingAssignmentsFlow(loggedInUser.get(), scanner);
                                break;
                            case "3":
                                requestShiftCancellationFlow(loggedInUser.get(), scanner);
                                break;
                            case "4":
                                transferCancellationCardFlow(loggedInUser.get(), scanner);
                                break;
                            case "5":
                                if (authenticationService.logout()) {
                                    System.out.println("You have been logged out.");
                                } else {
                                    System.out.println("No user is currently logged in.");
                                }
                                sessionActive = false;
                                break;
                            case "6":
                                authenticationService.logout();
                                System.out.println("You have been logged out.");
                                System.out.println("Application terminated.");
                                sessionActive = false;
                                appRunning = false;
                                break;
                            default:
                                System.out.println("Invalid selection.");
                        }
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

            Employee employee = employeePresentation.readEmployeeInput(scanner, activeBranch);
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
            userController.addEmployee(currentUser.get(), employee);
            System.out.println("Employee added successfully.");
        } catch (RepositoryException e) {
            System.out.println("Error: Failed to add employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
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
        System.out.println("Current vacation days balance: " + employee.getVacationDaysBalance());
        int vacationDaysToUse = readInt(scanner, "Vacation days to use this week: ");

        List<DayOfWeek> selectedVacationDays = readVacationDaysToUse(scanner, vacationDaysToUse);

        try {
            int remainingVacationDays = weeklyAvailabilityService.submitWeeklyAvailability(
                    employee,
                    constraints,
                    preferences,
                    vacationDaysToUse,
                    selectedVacationDays,
                    LocalDate.now());
            System.out.println(
                    "Weekly constraints and preferences were submitted successfully. Remaining vacation days: " +
                            remainingVacationDays + ".");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("Failed to save weekly submission: " + e.getMessage());
        }
    }

    private List<DayOfWeek> readVacationDaysToUse(Scanner scanner, int daysToUse) {
        List<DayOfWeek> selectedDays = new ArrayList<>();
        for (int i = 0; i < daysToUse; i++) {
            System.out.println("Choose vacation day #" + (i + 1) + ":");
            DayOfWeek chosenDay = readDayOfWeek(scanner);
            selectedDays.add(chosenDay);
        }

        return selectedDays;
    }

    private void resetVacationDaysForCurrentYear() {
        int currentYear = Year.now().getValue();
        if (lastVacationResetYear == currentYear) {
            return;
        }

        try {
            List<Employee> allEmployees = employeeRepository.findAll();
            for (Employee employee : allEmployees) {
                if (employee.getEmploymentTerms() != null) {
                    employee.resetVacationDays(DEFAULT_ANNUAL_VACATION_DAYS);
                    employeeRepository.save(employee);
                }
            }
            lastVacationResetYear = currentYear;
        } catch (RepositoryException e) {
            System.out.println("Failed to reset annual vacation days: " + e.getMessage());
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
        while (true) {
            LocalDate newDeadline = readLocalDate(
                    scanner,
                    "Deadline for submitting constraints and preferences for the upcoming week (DD-MM-YYYY): ");

            try {
                submissionDeadlineService.setWeeklySubmissionDeadline(newDeadline, submissionDeadlineRepository);
                System.out.println("Weekly submission deadline was set to " + newDeadline + ".");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.out.println("Please enter the deadline again.");
            } catch (RepositoryException e) {
                System.out.println("Failed to save weekly submission deadline: " + e.getMessage());
                return;
            }
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
                    newCanManageShift);

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
                    employeeId);

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

            String value = scanner.nextLine().trim();
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
                    System.out.println("Error: Day of week must be 1-7.");
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

            String value = scanner.nextLine().trim();
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
                    System.out.println("Error: Shift type must be 1-4.");
            }
        }
    }

    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                int parsed = Integer.parseInt(value);
                if (parsed < 0) {
                    System.out.println("Error: Value must be non-negative.");
                    continue;
                }
                return parsed;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid integer.");
            }
        }
    }

    private LocalDate readLocalDate(Scanner scanner, String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu");
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return LocalDate.parse(value, formatter);
            } catch (Exception e) {
                System.out.println("Error: Date must be in DD-MM-YYYY format.");
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
            String value = scanner.nextLine().trim();

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
                    System.out.println("Error: Day of week must be 1-7.");
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
            userController.approveAsShiftManager(currentUser.get(), selectedEmployee.getId());
            System.out.println("Employee " + selectedEmployee.getName() + " has been approved as shift manager.");

        } catch (RepositoryException e) {
            System.out.println("Failed to approve employee: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Not authorized: " + e.getMessage());
        }
    }

    private void createShiftFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            if (!isHrManager(currentUser.get())) {
                System.out.println("Only HR manager can create shifts.");
                return;
            }

            if (activeBranch == null) {
                System.out.println("Select or create a branch workspace before creating shifts.");
                return;
            }

            shiftPresentation.readShiftInput(scanner);
            List<Employee> employees = getEmployeesForCurrentScope(false);

            Employee manager = null;
            String managerId = shiftPresentation.getSelectManagerIdInput();
            for (Employee employee : employees) {
                if (employee.getId().equals(managerId)) {
                    manager = employee;
                    break;
                }
            }

            if (manager == null) {
                System.out.println("Shift manager id not found.");
                return;
            }

            Shift shift = new Shift(
                    shiftPresentation.getShiftDateInput(),
                    shiftPresentation.getShiftTypeInput(),
                    null,
                    shiftPresentation.getRequiredCashierInput(),
                    shiftPresentation.getRequiredStoreKeeperInput(),
                    activeBranch);
            shift.assignShiftManager(currentUser.get(), manager);
            shiftController.addShift(shift);
            shiftRepository.save(shift);

            System.out.println("Shift created successfully: " + shift.getDate() + " - " + shift.getShiftType());
        } catch (RepositoryException e) {
            System.out.println("Failed to save shift: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Failed to create shift: " + e.getMessage());
        }
    }

    private void assignEmployeeToShiftFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            if (activeBranch == null) {
                System.out.println("Select or create a branch workspace before assigning employees to shifts.");
                return;
            }

            System.out.println("\n=== Assign Employee to Shift ===");

            List<Employee> employees = getEmployeesForCurrentScope(true);
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

            List<Shift> shifts = getShiftsForCurrentScope();
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

            System.out.println("Select role:");
            System.out.println("1. CASHIER");
            System.out.println("2. STOREKEEPER");
            System.out.println("3. DRIVER");
            System.out.print("Selection: ");
            String roleChoice = scanner.nextLine();

            Role selectedRole;
            if ("1".equals(roleChoice)) {
                selectedRole = Role.CASHIER;
            } else if ("2".equals(roleChoice)) {
                selectedRole = Role.STOREKEEPER;
            } else if ("3".equals(roleChoice)) {
                selectedRole = Role.DRIVER;
            } else {
                System.out.println("Invalid role selection.");
                return;
            }

            shiftController.assignEmployeeToShift(currentUser.get(), selectedEmployee, selectedShift, selectedRole);
            shiftRepository.save(selectedShift);

            System.out.println("Employee " + selectedEmployee.getName() + " has been assigned to shift " +
                    selectedShift.getDate() + " - " + selectedShift.getShiftType() + " as " + selectedRole);

        } catch (RepositoryException e) {
            System.out.println("Failed to save shift assignment: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Assignment failed: " + e.getMessage());
        }
    }

    private void respondToPendingAssignmentsFlow(User loggedInUser, Scanner scanner) {
        if (!(loggedInUser instanceof Employee)) {
            System.out.println("Only employees can respond to shift assignments.");
            return;
        }

        Employee employee = (Employee) loggedInUser;
        List<ShiftAssignment> pending = getPendingApprovalsForEmployeeInEmployeeBranch(employee);

        if (pending.isEmpty()) {
            System.out.println("You have no pending shift assignments.");
            return;
        }

        System.out.println("\n=== Pending Shift Assignments ===");
        for (int i = 0; i < pending.size(); i++) {
            ShiftAssignment assignment = pending.get(i);
            System.out.println((i + 1) + ". " + assignment.getShift().getDate() +
                    " - " + assignment.getShift().getShiftType() +
                    " as " + assignment.getRole() +
                    " [CONFLICTS WITH YOUR CONSTRAINTS]");
        }

        System.out.print("Select assignment number: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine()) - 1;
            if (choice < 0 || choice >= pending.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            ShiftAssignment selected = pending.get(choice);
            System.out.print("Do you approve this assignment? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            boolean approved = "yes".equals(response);

            shiftController.respondToAssignment(employee, selected, approved);
            shiftRepository.save(selected.getShift());

            if (approved) {
                System.out.println("Assignment approved. You are confirmed for this shift.");
            } else {
                System.out.println("Assignment rejected. The manager will be notified to assign another employee.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("Failed to save assignment response: " + e.getMessage());
        }
    }

    private void substituteEmployeeFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            if (activeBranch == null) {
                System.out.println("Select or create a branch workspace before substituting employees.");
                return;
            }

            System.out.println("\n=== Substitute Employee in Shift ===");

            List<Shift> shifts = getShiftsForCurrentScope();
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
                System.out.println("Invalid selection.");
                return;
            }
            Shift selectedShift = shifts.get(shiftChoice);

            // Show currently assigned employees
            List<ShiftAssignment> assignments = selectedShift.getAssignments();
            if (assignments.isEmpty()) {
                System.out.println("No employees assigned to this shift.");
                return;
            }
            System.out.println("Currently assigned employees:");
            for (int i = 0; i < assignments.size(); i++) {
                ShiftAssignment a = assignments.get(i);
                System.out.println((i + 1) + ". " + a.getEmployee().getName() + " (" + a.getRole() + ")");
            }
            System.out.print("Select employee to replace: ");
            int origChoice = Integer.parseInt(scanner.nextLine()) - 1;
            if (origChoice < 0 || origChoice >= assignments.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            Employee originalEmployee = assignments.get(origChoice).getEmployee();

            // Show available replacements (not already on shift)
            List<Employee> allEmployees = getEmployeesForCurrentScope(true);
            List<Employee> available = new ArrayList<>();
            for (Employee emp : allEmployees) {
                if (!emp.isFired()) {
                    boolean alreadyAssigned = false;
                    for (ShiftAssignment a : selectedShift.getAssignments()) {
                        if (a.getEmployee().getId().equals(emp.getId())) {
                            alreadyAssigned = true;
                            break;
                        }
                    }
                    if (!alreadyAssigned)
                        available.add(emp);
                }
            }

            if (available.isEmpty()) {
                System.out.println("No available employees for substitution.");
                return;
            }
            System.out.println("Available replacements:");
            for (int i = 0; i < available.size(); i++) {
                System.out.println((i + 1) + ". " + available.get(i).getName());
            }
            System.out.print("Select replacement employee: ");
            int replChoice = Integer.parseInt(scanner.nextLine()) - 1;
            if (replChoice < 0 || replChoice >= available.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            Employee replacement = available.get(replChoice);

            shiftController.substituteEmployee(currentUser.get(), selectedShift, originalEmployee, replacement);
            shiftRepository.save(selectedShift);
            System.out.println(originalEmployee.getName() + " has been replaced by " + replacement.getName() + ".");

        } catch (RepositoryException e) {
            System.out.println("Failed to save substitution: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Substitution failed: " + e.getMessage());
        }
    }

    private void requestShiftCancellationFlow(User loggedInUser, Scanner scanner) {
        if (!(loggedInUser instanceof Employee)) {
            System.out.println("Only employees can request shift cancellation.");
            return;
        }

        Employee employee = (Employee) loggedInUser;
        List<ShiftAssignment> myAssignments = new ArrayList<>();
        for (Shift shift : getShiftsForEmployeeBranch(employee)) {
            for (ShiftAssignment assignment : shift.getAssignments()) {
                if (assignment.getEmployee().getId().equals(employee.getId())) {
                    myAssignments.add(assignment);
                }
            }
        }

        if (myAssignments.isEmpty()) {
            System.out.println("You have no assigned shifts.");
            return;
        }

        System.out.println("\n=== Request Shift Cancellation ===");
        for (int i = 0; i < myAssignments.size(); i++) {
            ShiftAssignment assignment = myAssignments.get(i);
            String status = assignment.isCancellationRequested() ? "[REQUESTED]" : "";
            System.out.println((i + 1) + ". " + assignment.getShift().getDate() +
                    " - " + assignment.getShift().getShiftType() +
                    " as " + assignment.getRole() + " " + status);
        }

        System.out.print("Select assignment number: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine()) - 1;
            if (choice < 0 || choice >= myAssignments.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            ShiftAssignment selected = myAssignments.get(choice);
            if (selected.isCancellationRequested()) {
                System.out.println("Cancellation already requested for this shift.");
                return;
            }

            shiftController.requestShiftCancellation(employee, selected.getShift());
            shiftRepository.save(selected.getShift());
            System.out.println("Cancellation request submitted. The manager will handle a substitution.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("Failed to save cancellation request: " + e.getMessage());
        }
    }

    private void handleCancellationRequestsFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            List<ShiftAssignment> requests = getCancellationRequestsForCurrentScope();
            if (requests.isEmpty()) {
                System.out.println("No cancellation requests pending.");
                return;
            }

            System.out.println("\n=== Cancellation Requests ===");
            for (int i = 0; i < requests.size(); i++) {
                ShiftAssignment request = requests.get(i);
                System.out.println((i + 1) + ". " + request.getEmployee().getName() +
                        " requested cancellation for " + request.getShift().getDate() +
                        " - " + request.getShift().getShiftType() +
                        " (" + request.getRole() + ")");
            }

            System.out.print("Select request number: ");
            int requestChoice = Integer.parseInt(scanner.nextLine()) - 1;
            if (requestChoice < 0 || requestChoice >= requests.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            ShiftAssignment selectedRequest = requests.get(requestChoice);
            Shift selectedShift = selectedRequest.getShift();

            List<Employee> allEmployees = getEmployeesForCurrentScope(true);
            List<Employee> available = new ArrayList<>();
            for (Employee emp : allEmployees) {
                if (!emp.isFired()) {
                    boolean alreadyAssigned = false;
                    for (ShiftAssignment assignment : selectedShift.getAssignments()) {
                        if (assignment.getEmployee().getId().equals(emp.getId())) {
                            alreadyAssigned = true;
                            break;
                        }
                    }
                    if (!alreadyAssigned) {
                        available.add(emp);
                    }
                }
            }

            if (available.isEmpty()) {
                System.out.println("No available employees for substitution.");
                return;
            }

            System.out.println("Available replacements:");
            for (int i = 0; i < available.size(); i++) {
                System.out.println((i + 1) + ". " + available.get(i).getName());
            }

            System.out.print("Select replacement employee: ");
            int replacementChoice = Integer.parseInt(scanner.nextLine()) - 1;
            if (replacementChoice < 0 || replacementChoice >= available.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            Employee replacement = available.get(replacementChoice);
            shiftController.handleCancellationWithSubstitution(currentUser.get(), selectedRequest, replacement);
            shiftRepository.save(selectedShift);
            System.out.println("Cancellation handled. Substitution completed.");

        } catch (IllegalArgumentException e) {
            System.out.println("Failed to handle cancellation: " + e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("Failed to save cancellation handling: " + e.getMessage());
        }
    }

    private void calculateEmployeeSalaryFlow(Scanner scanner) {
        try {
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (!currentUser.isPresent()) {
                System.out.println("No user is currently logged in.");
                return;
            }

            if (activeBranch == null) {
                System.out.println("Select or create a branch workspace before calculating salaries.");
                return;
            }

            List<Employee> employees = getEmployeesForCurrentScope(false);
            if (employees.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }

            System.out.println("\n=== Calculate Employee Salary ===");
            for (int i = 0; i < employees.size(); i++) {
                Employee employee = employees.get(i);
                System.out.println((i + 1) + ". " + employee.getId() + " - " + employee.getName());
            }

            System.out.print("Select employee number: ");
            int choice = Integer.parseInt(scanner.nextLine()) - 1;
            if (choice < 0 || choice >= employees.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            Employee selectedEmployee = employees.get(choice);
            double workedHours = shiftController.calculateWorkedHoursForEmployee(selectedEmployee);
            double finalSalary = shiftController.recalculateEmployeeSalary(selectedEmployee);
            employeeRepository.save(selectedEmployee);

            System.out.println("Salary updated for " + selectedEmployee.getName() + ".");
            System.out.println("Worked hours: " + workedHours);
            System.out.println("Final salary: " + finalSalary);
        } catch (RepositoryException e) {
            System.out.println("Failed to save updated salary: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Salary calculation failed: " + e.getMessage());
        }
    }

    private void transferCancellationCardFlow(User loggedInUser, Scanner scanner) {
        if (!(loggedInUser instanceof Employee)) {
            System.out.println("Only employees can transfer cancellation cards.");
            return;
        }

        Employee employee = (Employee) loggedInUser;
        if (activeBranch == null) {
            System.out.println("Select or create a branch workspace before transferring cancellation cards.");
            return;
        }

        List<Shift> managedShifts = getManagedShiftsForCurrentScope(employee);
        if (managedShifts.isEmpty()) {
            System.out.println("You are not assigned as shift manager on any shift.");
            return;
        }

        System.out.println("\n=== Transfer Cancellation Card ===");
        for (int i = 0; i < managedShifts.size(); i++) {
            Shift shift = managedShifts.get(i);
            String status = shift.isCancellationCardTransferred() ? "[ALREADY TRANSFERRED]" : "";
            System.out.println((i + 1) + ". " + shift.getDate() + " - " + shift.getShiftType() + " " + status);
        }

        System.out.print("Select shift number: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine()) - 1;
            if (choice < 0 || choice >= managedShifts.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            Shift selectedShift = managedShifts.get(choice);
            shiftController.transferCancellationCard(employee, selectedShift);
            System.out.println("Cancellation card transfer recorded for shift " +
                    selectedShift.getDate() + " - " + selectedShift.getShiftType() + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private void handleDriverAssignmentRequestsFlow(Scanner scanner) {
        Optional<User> currentUser = authenticationService.getCurrentUser();
        if (!currentUser.isPresent()) {
            System.out.println("No user is currently logged in.");
            return;
        }

        if (!isHrManager(currentUser.get())) {
            System.out.println("Only HR manager can handle driver assignment requests.");
            return;
        }

        List<DriverAssignmentRequest> requests = employeeTransportationService.getOpenDriverAssignmentRequests();

        if (requests.isEmpty()) {
            System.out.println("No open driver assignment requests.");
            return;
        }

        System.out.println("\n=== Driver Assignment Requests ===");
        for (int i = 0; i < requests.size(); i++) {
            DriverAssignmentRequest request = requests.get(i);
            System.out.println((i + 1) + ". Delivery " + request.getDeliveryId()
                    + " needs driver " + request.getDriverId()
                    + " on " + request.getDeliveryDateTime().toLocalDate()
                    + " at " + request.getDeliveryDateTime().toLocalTime()
                    + ", shift " + request.getShiftType());
        }

        System.out.print("Select request number: ");
        int requestChoice;
        try {
            requestChoice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (requestChoice < 0 || requestChoice >= requests.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        DriverAssignmentRequest selectedRequest = requests.get(requestChoice);

        Shift shift = employeeTransportationService.getShiftByDateTime(
                selectedRequest.getDeliveryDateTime());

        if (shift == null) {
            selectedRequest.setStatusMessage(
                    "Waiting for HR manager to create shift "
                            + selectedRequest.getShiftType()
                            + " on "
                            + selectedRequest.getDeliveryDateTime().toLocalDate()
                            + " before assigning the driver");

            System.out.println("\nNo matching shift exists for this delivery time.");
            System.out.println("To handle this driver assignment request, HR must first create the required shift.");
            System.out.println("Required shift details:");
            System.out.println("Date: " + selectedRequest.getDeliveryDateTime().toLocalDate());
            System.out.println("Shift type: " + selectedRequest.getShiftType());
            System.out.println("Driver to assign later: " + selectedRequest.getDriverId());
            System.out.println("Delivery id: " + selectedRequest.getDeliveryId());

            System.out.println("\nWhat to do next:");
            System.out.println("1. You will now return to the HR main menu.");
            System.out.println("2. Choose option 6: Create shift.");
            System.out.println("3. Create the shift with the date and shift type shown above.");
            System.out.println("4. After creating the shift, choose option 13 again.");
            System.out
                    .println("5. Select this same request again, and the system will assign the driver to that shift.");
            return;
        }
        try {
            Optional<Employee> driverOptional = employeeRepository.findById(selectedRequest.getDriverId());

            if (!driverOptional.isPresent()) {
                System.out.println("Driver employee was not found.");
                return;
            }

            Employee driver = driverOptional.get();

            shiftController.assignEmployeeToShift(
                    currentUser.get(),
                    driver,
                    shift,
                    Role.DRIVER);

            shiftRepository.save(shift);
            employeeTransportationService.markDriverAssignmentRequestHandled(selectedRequest);

            System.out.println("Driver " + driver.getName()
                    + " was assigned to shift "
                    + shift.getDate() + " - " + shift.getShiftType()
                    + " for delivery " + selectedRequest.getDeliveryId() + ".");

            System.out.println("Transportation manager update: driver assignment request was completed.");
        } catch (RepositoryException e) {
            System.out.println("Failed to save driver assignment: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Driver assignment failed: " + e.getMessage());
        }
    }

    private void manageBranchWorkspaceFlow(Scanner scanner) {
        while (true) {
            System.out.println("\n=== Branch Management ===");
            System.out.println("1. Create branch");
            System.out.println("2. Select branch workspace");
            System.out.println("3. Clear active branch workspace");
            System.out.println("4. Back");
            System.out.print("Selection: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    createBranchFlow(scanner);
                    break;
                case "2":
                    selectBranchWorkspaceFlow(scanner);
                    break;
                case "3":
                    activeBranch = null;
                    System.out.println("Branch workspace cleared.");
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid selection.");
            }
        }
    }

    private void createBranchFlow(Scanner scanner) {
        try {
            Branch branch = readBranch(scanner);
            branchManager.addBranch(branch);
            System.out.println("Branch created successfully: " + branch.getBranchName());
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to create branch: " + e.getMessage());
        }
    }

    private void selectBranchWorkspaceFlow(Scanner scanner) {
        List<Branch> branches = branchManager.getAllBranches();
        if (branches.isEmpty()) {
            System.out.println("No branches available. Create one first.");
            return;
        }

        for (int i = 0; i < branches.size(); i++) {
            Branch branch = branches.get(i);
            System.out.println((i + 1) + ". " + branch.getBranchName() + " (" + branch.getLocation() + ")");
        }

        int selection = readInt(scanner, "Select branch number: ");
        if (selection < 1 || selection > branches.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        activeBranch = branches.get(selection - 1);
        System.out.println("Active branch workspace: " + activeBranch.getBranchName());
    }

    private Branch readBranch(Scanner scanner) {
        System.out.print("Branch ID: ");
        String branchId = scanner.nextLine();
        System.out.print("Branch name: ");
        String branchName = scanner.nextLine();
        System.out.print("Branch location: ");
        String location = scanner.nextLine();
        System.out.println("Enter delivery stop details for this branch:");
        Site stop = readSite(scanner, SiteType.BRANCH, null);
        Branch branch = new Branch(branchId, branchName, location, stop);
        stop.setBranch(branch);
        return branch;
    }

    private Site readSite(Scanner scanner, SiteType siteType, Branch branch) {
        System.out.print("Site name: ");
        String siteName = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        System.out.print("Phone number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Contact name: ");
        String contactName = scanner.nextLine();
        System.out.print("Shipping zone code: ");
        String zoneCode = scanner.nextLine();
        System.out.print("Shipping zone name: ");
        String zoneName = scanner.nextLine();

        return new Site(siteName, address, phoneNumber, contactName, new ShippingZone(zoneCode, zoneName), siteType, branch);
    }

    private List<Employee> getEmployeesForCurrentScope(boolean includeGlobalDrivers) {
        List<Employee> employees = userController.getEmployees();
        if (activeBranch == null) {
            return employees;
        }

        List<Employee> scopedEmployees = new ArrayList<>();
        for (Employee employee : employees) {
            if (employee.getBranch() != null && employee.getBranch().equals(activeBranch)) {
                scopedEmployees.add(employee);
                continue;
            }

            if (includeGlobalDrivers && isGlobalDriver(employee)) {
                scopedEmployees.add(employee);
            }
        }
        return scopedEmployees;
    }

    private List<Shift> getShiftsForCurrentScope() {
        List<Shift> shifts = shiftController.getShifts();
        if (activeBranch == null) {
            return shifts;
        }

        List<Shift> scopedShifts = new ArrayList<>();
        for (Shift shift : shifts) {
            if (shift.getBranch() != null && activeBranch.equals(shift.getBranch())) {
                scopedShifts.add(shift);
            }
        }
        return scopedShifts;
    }

    private List<ShiftAssignment> getCancellationRequestsForCurrentScope() {
        List<ShiftAssignment> requests = shiftController.getCancellationRequests();
        if (activeBranch == null) {
            return requests;
        }

        List<ShiftAssignment> scopedRequests = new ArrayList<>();
        for (ShiftAssignment request : requests) {
            if (request.getShift().getBranch() != null && activeBranch.equals(request.getShift().getBranch())) {
                scopedRequests.add(request);
            }
        }
        return scopedRequests;
    }

    private List<Shift> getManagedShiftsForCurrentScope(Employee employee) {
        List<Shift> managedShifts = shiftController.getShiftsManagedBy(employee);
        if (activeBranch == null) {
            return managedShifts;
        }

        List<Shift> scopedShifts = new ArrayList<>();
        for (Shift shift : managedShifts) {
            if (shift.getBranch() != null && activeBranch.equals(shift.getBranch())) {
                scopedShifts.add(shift);
            }
        }
        return scopedShifts;
    }

    private List<Shift> getShiftsForEmployeeBranch(Employee employee) {
        List<Shift> shifts = shiftController.getShifts();
        if (employee == null || employee.getBranch() == null) {
            return new ArrayList<>();
        }

        List<Shift> scopedShifts = new ArrayList<>();
        for (Shift shift : shifts) {
            if (employee.getBranch().equals(shift.getBranch())) {
                scopedShifts.add(shift);
            }
        }
        return scopedShifts;
    }

    private List<ShiftAssignment> getPendingApprovalsForEmployeeInEmployeeBranch(Employee employee) {
        List<ShiftAssignment> pending = shiftController.getPendingApprovalsForEmployee(employee);
        List<ShiftAssignment> scopedPending = new ArrayList<>();
        for (ShiftAssignment assignment : pending) {
            if (employee.getBranch() != null && employee.getBranch().equals(assignment.getShift().getBranch())) {
                scopedPending.add(assignment);
            }
        }
        return scopedPending;
    }

    private boolean isGlobalDriver(Employee employee) {
        return employee != null
                && employee.getAuthorizedRoles().contains(Role.DRIVER)
                && !employee.getAuthorizedRoles().contains(Role.CASHIER)
                && !employee.getAuthorizedRoles().contains(Role.STOREKEEPER)
                && employee.getBranch() == null;
    }
}
