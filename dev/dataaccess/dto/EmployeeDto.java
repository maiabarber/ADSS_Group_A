package dataaccess.dto;


import employee.domain.EmploymentType;
import employee.domain.Role;


import java.time.DayOfWeek;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EmployeeDto {
        private final String id;
        private final String password;
        private final BankAccountDto bankAccount;
        private final String name;
        private final SalaryDto salary;
        private final EmploymentType employmentType;
        private final EmploymentTermsDto employmentTerms;
        private final Set<Role> authorizedRoles;
        private final boolean canManageShift;
        private final boolean isFired;
        private final DayOfWeek fixedDayOff;
        private final WeeklyAvailabilityRequestDto weeklyAvailabilityRequest;
        private final BranchDto branch;

    public EmployeeDto(
        String id,
        String password,
        BankAccountDto bankAccount,
        String name,
        SalaryDto salary,
        EmploymentType employmentType,
        EmploymentTermsDto employmentTerms,
        Set<Role> authorizedRoles,
        boolean canManageShift,
        boolean isFired,
        DayOfWeek fixedDayOff,
        WeeklyAvailabilityRequestDto weeklyAvailabilityRequest,
        BranchDto branch) {
        this.id = id;
        this.password = password;
        this.bankAccount = bankAccount;
        this.name = name;
        this.salary = salary;
        this.employmentType = employmentType;
        this.employmentTerms = employmentTerms;
        this.authorizedRoles = new HashSet<>(authorizedRoles);
        this.canManageShift = canManageShift;
        this.isFired = isFired;
        this.fixedDayOff = fixedDayOff;
        this.weeklyAvailabilityRequest = weeklyAvailabilityRequest;
        this.branch = branch;
    }

    public String getEmployeeId() {
        return id;
    }

    public String getPassword() {
        return password;
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
        return Collections.unmodifiableSet(authorizedRoles);
    }

    public boolean canManageShift() {
        return canManageShift;
    }

    public boolean isFired() {
        return isFired ;
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

    public String getName() {
        return name;
    }

}