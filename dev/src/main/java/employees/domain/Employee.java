package employees.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    public Employee() {
    }

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
        this.bankAccount = bankAccount;
        this.name = name;
        this.salary = salary;
        this.employmentType = employmentType;
        setEmploymentTerms(Objects.requireNonNull(employmentTerms, "employmentTerms must not be null"));
        setAuthorizedRoles(authorizedRoles);
        this.canManageShift = canManageShift;
        this.isFired = isFired;
        this.fixedDayOff = fixedDayOff;
        this.weeklyAvailabilityRequest = weeklyAvailabilityRequest;
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
        this.name = name;
    }

    public Salary getSalary() {
        return salary;
    }

    public void setSalary(Salary salary) {
        this.salary = salary;
        if (salary != null && employmentTerms != null) {
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
                this.salary != null ? this.salary.getWorkedHours() : 0
            );
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
        this.fixedDayOff = fixedDayOff;
    }

    public WeeklyAvailabilityRequest getWeeklyAvailabilityRequest() {
        return weeklyAvailabilityRequest;
    }

    public void setWeeklyAvailabilityRequest(WeeklyAvailabilityRequest weeklyAvailabilityRequest) {
        this.weeklyAvailabilityRequest = weeklyAvailabilityRequest;
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
}
