package employees.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Employee extends User {
    private BankAccount bankAccount;
    private String name;
    private Salary salary;
    private EmploymentType employmentType;
    private LocalDate startDate;
    private Set<Role> authorizedRoles = new HashSet<>();
    private boolean canManageShift;
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
        LocalDate startDate,
        Set<Role> authorizedRoles,
        boolean canManageShift,
        DayOfWeek fixedDayOff,
        WeeklyAvailabilityRequest weeklyAvailabilityRequest
    ) {
        super(id, password);
        this.bankAccount = bankAccount;
        this.name = name;
        this.salary = salary;
        this.employmentType = employmentType;
        this.startDate = startDate;
        setAuthorizedRoles(authorizedRoles);
        this.canManageShift = canManageShift;
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

    public boolean isCanManageShift() {
        return canManageShift;
    }

    public void setCanManageShift(boolean canManageShift) {
        this.canManageShift = canManageShift;
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
}
