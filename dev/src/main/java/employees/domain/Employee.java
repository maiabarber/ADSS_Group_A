package employees.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Employee class represents an employee in the system.
 * It extends the User class and includes additional attributes such as bank account, salary, employment type, etc.
 */
public class Employee extends User {
    private BankAccount bankAccount;
    private String name;
    private Salary salary;
    private EmploymentType employmentType;
    private LocalDate startDate;
    private EmploymentTerms employmentTerms;
    private Set<Role> authorizedRoles = new HashSet<>();
    private boolean canManageShift;
    private boolean isFired;
    private DayOfWeek fixedDayOff;
    private WeeklyAvailabilityRequest weeklyAvailabilityRequest;

    public Employee(
        String id,
        String password,
        BankAccount bankAccount,
        String name,
        Salary salary,
        EmploymentType employmentType,
        EmploymentTerms employmentTerms,
        Set<Role> authorizedRoles,
        boolean canManageShift,
        boolean isFired,
        DayOfWeek fixedDayOff,
        WeeklyAvailabilityRequest weeklyAvailabilityRequest
    ) {
        super(id, password);
        validateName(name);
        this.bankAccount = bankAccount;
        this.name = name;
        this.salary = salary;
        this.employmentType = employmentType;
        setEmploymentTerms(Objects.requireNonNull(employmentTerms, "employmentTerms must not be null"));
        setAuthorizedRoles(authorizedRoles);
        this.canManageShift = canManageShift;
        this.isFired = isFired;
        if (fixedDayOff != null) {
            setFixedDayOff(fixedDayOff);
        }
        this.weeklyAvailabilityRequest = weeklyAvailabilityRequest;
    }

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be empty");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 2) {
            throw new IllegalArgumentException("Employee name must have at least 2 characters");
        }

        if (trimmedName.length() > 100) {
            throw new IllegalArgumentException("Employee name cannot exceed 100 characters");
        }

        if (!trimmedName.contains(" ")) {
            throw new IllegalArgumentException("Employee name must include first name and last name");
        }

        if (!trimmedName.matches("[a-zA-Z0-9\\u0590-\\u05FF\\s'._-]+")) {
            throw new IllegalArgumentException(
                "Employee name can only contain letters, numbers, spaces, apostrophes, dots, underscores and hyphens"
            );
        }
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public Salary getSalary() {
        return salary;
    }

    public void setSalary(Salary salary) {
        this.salary = salary;
        if (salary != null && employmentTerms != null) {
            salary.setEmploymentScope(employmentTerms.getEmploymentScope());
            employmentTerms.setGlobalSalary(salary.getGlobalSalary());
            employmentTerms.setHourlySalary(salary.getHourlySalary());
        }
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        EmploymentTerms.validateStartDate(startDate, LocalDate.now());
        this.startDate = startDate;
        if (employmentTerms != null) {
            employmentTerms.setStartDate(startDate);
        }
    }

    public EmploymentTerms getEmploymentTerms() {
        return employmentTerms;
    }

    public void setEmploymentTerms(EmploymentTerms employmentTerms) {
        this.employmentTerms = employmentTerms;
        if (employmentTerms != null) {
            this.startDate = employmentTerms.getStartDate();
            this.salary = new Salary(
                employmentTerms.getGlobalSalary(),
                employmentTerms.getHourlySalary(),
                this.salary != null ? this.salary.getWorkedHours() : 0,
                employmentTerms.getEmploymentScope()
            );
        }
    }

    public double recalculateSalary() {
        if (salary == null || employmentTerms == null) {
            return 0;
        }
        salary.setEmploymentScope(employmentTerms.getEmploymentScope());
        return salary.recalculateFinalSalary();
    }

    public void setWorkedHours(double workedHours) {
        if (salary != null) {
            salary.setWorkedHours(workedHours);
            recalculateSalary();
        }
    }

    public Set<Role> getAuthorizedRoles() {
        return Collections.unmodifiableSet(authorizedRoles);
    }

    public void setAuthorizedRoles(Set<Role> authorizedRoles) {
        this.authorizedRoles.clear();
        if (authorizedRoles != null) {
            this.authorizedRoles.addAll(authorizedRoles);
        }
    }

    public boolean canManageShift() {
        return canManageShift;
    }

    public void setCanManageShift(boolean canManageShift) {
        this.canManageShift = canManageShift;
    }

    public boolean isFired() {
        return isFired;
    }

    public void setFired(boolean fired) {
        isFired = fired;
    }

    public DayOfWeek getFixedDayOff() {
        return fixedDayOff;
    }

    public void setFixedDayOff(DayOfWeek fixedDayOff) {
        Objects.requireNonNull(fixedDayOff, "fixedDayOff must not be null");
        if (this.fixedDayOff != null && this.fixedDayOff != fixedDayOff) {
            throw new IllegalStateException("Fixed day off can only be set once");
        }    
        this.fixedDayOff = fixedDayOff;
    }

    public WeeklyAvailabilityRequest getWeeklyAvailabilityRequest() {
        return weeklyAvailabilityRequest;
    }

    public void setWeeklyAvailabilityRequest(WeeklyAvailabilityRequest weeklyAvailabilityRequest) {
        this.weeklyAvailabilityRequest = weeklyAvailabilityRequest;
    }

    @Override
    public boolean canAuthenticate(String id, String password) {
        return matchesCredentials(id, password) && !isFired();
    }

    public boolean matchesFiredCredentials(String id, String password) {
        return matchesCredentials(id, password) && isFired();
    }

    public int getVacationDaysBalance() {
        if (employmentTerms == null) {
            return 0;
        }
        return employmentTerms.getVacationDays();
    }

    public void resetVacationDays(int days) {
        if (employmentTerms == null) {
            throw new IllegalStateException("Employment terms are required to manage vacation days");
        }
        employmentTerms.setVacationDays(days);
    }

    public void consumeVacationDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Vacation days to consume cannot be negative");
        }
        if (employmentTerms == null) {
            throw new IllegalStateException("Employment terms are required to manage vacation days");
        }
        int currentBalance = employmentTerms.getVacationDays();
        if (days > currentBalance) {
            throw new IllegalArgumentException("Not enough vacation days. Current balance: " + currentBalance);
        }
        employmentTerms.setVacationDays(currentBalance - days);
    }

    public void addVacationDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Vacation days to add cannot be negative");
        }
        if (employmentTerms == null) {
            throw new IllegalStateException("Employment terms are required to manage vacation days");
        }
        employmentTerms.setVacationDays(employmentTerms.getVacationDays() + days);
    }

    public void approveAsShiftManager(User approvedBy) {
        if (!(approvedBy instanceof HR_Manager) || !((HR_Manager) approvedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can approve shift manager");
        }
        if (this.isFired()) {
            throw new IllegalStateException("Fired employee cannot be approved as shift manager");
        }
        this.canManageShift = true;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(authorizedRoles);
    }

    public int submitWeeklyAvailability(
        List<Constraint> constraints,
        List<Preference> preferences,
        int vacationDaysToUse,
        List<DayOfWeek> selectedVacationDays,
        LocalDate activeDeadline,
        WeeklyAvailabilityRules weeklyAvailabilityRules
    ) {
        if (weeklyAvailabilityRules == null) {
            throw new IllegalArgumentException("Weekly availability rules are required");
        }

        weeklyAvailabilityRules.validateVacationUsage(this, vacationDaysToUse, selectedVacationDays);
        List<Constraint> mergedConstraints = weeklyAvailabilityRules.mergeConstraintsWithVacationDays(
            constraints,
            selectedVacationDays
        );

        consumeVacationDays(vacationDaysToUse);

        WeeklyAvailabilityRequest request = getOrCreateWeeklyAvailabilityRequest();
        request.applySubmission(mergedConstraints, preferences, activeDeadline);

        return getVacationDaysBalance();
    }

    private WeeklyAvailabilityRequest getOrCreateWeeklyAvailabilityRequest() {
        if (weeklyAvailabilityRequest == null) {
            weeklyAvailabilityRequest = new WeeklyAvailabilityRequest();
        }
        return weeklyAvailabilityRequest;
    }
}
