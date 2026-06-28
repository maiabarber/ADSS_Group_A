package dataaccess.mapper;

import dataaccess.dto.EmployeeDto;
import employee.domain.BankAccount;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.EmploymentScope;
import employee.domain.EmploymentTerms;
import employee.domain.EmploymentType;
import employee.domain.Role;
import employee.domain.Salary;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public final class EmployeeMapper {
    private EmployeeMapper() {}

    public static EmployeeDto toDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        BankAccount bankAccount = employee.getBankAccount();
        EmploymentTerms terms = employee.getEmploymentTerms();
        Salary salary = employee.getSalary();

        Integer branchId = null;
        Branch branch = employee.getBranch();
        if (branch != null) {
            try {
                branchId = Integer.parseInt(branch.getBranchId());
            } catch (NumberFormatException ignored) {
                branchId = null;
            }
        }

        return new EmployeeDto(
                employee.getId(),
                employee.getName(),
                bankAccount == null ? null : bankAccount.getBankNumber(),
                bankAccount == null ? null : bankAccount.getBranchNumber(),
                bankAccount == null ? null : bankAccount.getAccountNumber(),
                employee.getEmploymentType() == null ? null : employee.getEmploymentType().name(),
                terms == null || terms.getEmploymentScope() == null ? null : terms.getEmploymentScope().name(),
                salary == null ? 0 : salary.getHourlySalary(),
                salary == null ? 0 : salary.getGlobalSalary(),
                terms == null || terms.getStartDate() == null ? null : terms.getStartDate().toString(),
                employee.isFired(),
                terms == null ? 0 : terms.getVacationDays(),
                branchId,
                employee.canManageShift()
        );
    }

    public static Employee toDomain(EmployeeDto dto, String password, Set<Role> roles, Branch branch) {
        if (dto == null) {
            return null;
        }

        EmploymentScope scope = dto.getEmploymentScope() == null || dto.getEmploymentScope().isBlank()
                ? EmploymentScope.FULL_TIME
                : EmploymentScope.valueOf(dto.getEmploymentScope());

        LocalDate startDate = dto.getStartDate() == null || dto.getStartDate().isBlank()
                ? LocalDate.now()
                : LocalDate.parse(dto.getStartDate());

        EmploymentTerms terms = new EmploymentTerms(
                startDate,
                scope,
                dto.getGlobalSalary(),
                dto.getHourlySalary(),
                dto.getVacationDays()
        );

        Salary salary = new Salary(dto.getGlobalSalary(), dto.getHourlySalary(), 0, scope);

        EmploymentType type = dto.getEmploymentType() == null || dto.getEmploymentType().isBlank()
                ? EmploymentType.REGULAR
                : EmploymentType.valueOf(dto.getEmploymentType());

        return new Employee(
                dto.getEmployeeId(),
                password == null ? "1234" : password,
                new BankAccount(
                        defaultString(dto.getBankNumber(), "1"),
                        defaultString(dto.getBankBranchNumber(), "1"),
                        defaultString(dto.getBankAccountNumber(), "1")
                ),
                dto.getName(),
                salary,
                type,
                terms,
                roles == null ? new HashSet<>() : roles,
                dto.canManageShift(),
                dto.isFired(),
                null,
                null,
                branch
        );
    }

    private static String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
