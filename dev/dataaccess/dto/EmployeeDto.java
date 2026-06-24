package dataaccess.dto;

import employee.domain.EmploymentType;
import employee.domain.Role;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class EmployeeDto {
    private final String employeeId;
    private final String name;
    private final BankAccountDto bankAccount;
    private final SalaryDto salary;
    private final EmploymentType employmentType;
    private final EmploymentTermsDto employmentTerms;
    private final Set<Role> roles;
    private final boolean canManageShift;
    private final boolean fired;
    private final DayOfWeek fixedDayOff;
    private final WeeklyAvailabilityRequestDto weeklyAvailabilityRequest;
    private final BranchDto branch;

    public EmployeeDto(
            String employeeId,
            String name,
            BankAccountDto bankAccount,
            SalaryDto salary,
            EmploymentType employmentType,
            EmploymentTermsDto employmentTerms,
            Set<Role> roles,
            boolean canManageShift,
            boolean fired,
            DayOfWeek fixedDayOff,
            WeeklyAvailabilityRequestDto weeklyAvailabilityRequest,
            BranchDto branch) {
        this.employeeId = employeeId;
        this.name = name;
        this.bankAccount = bankAccount;
        this.salary = salary;
        this.employmentType = employmentType;
        this.employmentTerms = employmentTerms;
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
        this.canManageShift = canManageShift;
        this.fired = fired;
        this.fixedDayOff = fixedDayOff;
        this.weeklyAvailabilityRequest = weeklyAvailabilityRequest;
        this.branch = branch;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public BankAccountDto getBankAccount() {
        return bankAccount;
    }

    public SalaryDto getSalary() {
        return salary;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public EmploymentTermsDto getEmploymentTerms() {
        return employmentTerms;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean isCanManageShift() {
        return canManageShift;
    }

    public boolean isFired() {
        return fired;
    }

    public DayOfWeek getFixedDayOff() {
        return fixedDayOff;
    }

    public WeeklyAvailabilityRequestDto getWeeklyAvailabilityRequest() {
        return weeklyAvailabilityRequest;
    }

    public BranchDto getBranch() {
        return branch;
    }
}