package dataaccess.repository.impl;

import employee.domain.BankAccount;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.EmploymentScope;
import employee.domain.EmploymentTerms;
import employee.domain.EmploymentType;
import employee.domain.HR_Manager;
import employee.domain.Role;
import employee.domain.Salary;
import employee.domain.Shift;
import employee.domain.ShiftAssignment;
import employee.domain.ShiftType;
import employee.domain.User;
import employee.domain.WeeklyAvailabilityRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

final class DomainMapper {
    private DomainMapper() {
    }

    static User mapUser(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("user_id");
        String password = resultSet.getString("password");
        if (resultSet.getInt("is_hr_manager") == 1) {
            return new HR_Manager(id, password);
        }
        return new User(id, password);
    }

    static Employee mapEmployee(ResultSet resultSet, Set<Role> roles, Branch branch) throws SQLException {
        String id = resultSet.getString("employee_id");
        String password = resultSet.getString("password");
        EmploymentScope scope = parseScope(resultSet.getString("employment_scope"));
        double globalSalary = resultSet.getDouble("global_salary");
        double hourlySalary = resultSet.getDouble("hourly_salary");
        int vacationDays = resultSet.getInt("vacation_days");
        LocalDate startDate = parseStartDate(resultSet.getString("start_date"));

        Branch effectiveBranch = branch;
        if (effectiveBranch == null && hasBranchSpecificRole(roles)) {
            effectiveBranch = new Branch("0", "Unassigned", "Unassigned");
        }

        return new Employee(
                id,
                password == null || password.isBlank() ? "1234" : password,
                parseBankAccount(resultSet.getString("bank_account")),
                resultSet.getString("name"),
                new Salary(globalSalary, hourlySalary, 0, scope),
                parseEmploymentType(resultSet.getString("employment_type")),
                new EmploymentTerms(startDate, scope, globalSalary, hourlySalary, vacationDays),
                roles == null ? new HashSet<>() : roles,
                roles != null && roles.contains(Role.CASHIER),
                resultSet.getInt("is_fired") == 1,
                null,
                new WeeklyAvailabilityRequest(),
                effectiveBranch
        );
    }

    static Shift mapShift(ResultSet resultSet, Employee shiftManager, Branch branch) throws SQLException {
        Shift shift = new Shift(
                LocalDate.parse(resultSet.getString("shift_date")),
                ShiftType.valueOf(resultSet.getString("shift_type")),
                shiftManager,
                1,
                1,
                branch
        );
        return shift;
    }

    static ShiftAssignment mapShiftAssignment(Employee employee, Shift shift, Role role, String status) {
        ShiftAssignment assignment = new ShiftAssignment(employee, shift, role);
        assignment.setApproved("APPROVED".equalsIgnoreCase(status));
        assignment.setRequiresApproval(!assignment.isApproved());
        return assignment;
    }

    static String formatBankAccount(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }
        return bankAccount.getBankNumber() + "-" + bankAccount.getBranchNumber() + "-" + bankAccount.getAccountNumber();
    }

    static Integer parseBranchId(Branch branch) {
        if (branch == null || branch.getBranchId() == null) {
            return null;
        }
        String digits = branch.getBranchId().replaceAll("\\D+", "");
        if (digits.isBlank()) {
            return null;
        }
        return Integer.parseInt(digits);
    }

    static Branch mapBranch(ResultSet resultSet) throws SQLException {
        int branchId = resultSet.getInt("branch_id");
        if (resultSet.wasNull()) {
            return null;
        }
        String branchName = resultSet.getString("branch_name");
        String address = resultSet.getString("address");
        if (branchName == null || branchName.isBlank()) {
            branchName = "Branch " + branchId;
        }
        return new Branch(String.valueOf(branchId), branchName, address == null || address.isBlank() ? branchName : address);
    }

    private static BankAccount parseBankAccount(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String[] parts = rawValue.split("-");
        if (parts.length == 3) {
            try {
                return new BankAccount(parts[0], parts[1], parts[2]);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        try {
            return new BankAccount(rawValue, "1", "1");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean hasBranchSpecificRole(Set<Role> roles) {
        return roles != null && (roles.contains(Role.CASHIER) || roles.contains(Role.STOREKEEPER));
    }

    private static EmploymentScope parseScope(String rawValue) {
        return rawValue == null || rawValue.isBlank() ? EmploymentScope.FULL_TIME : EmploymentScope.valueOf(rawValue);
    }

    private static EmploymentType parseEmploymentType(String rawValue) {
        return rawValue == null || rawValue.isBlank() ? EmploymentType.REGULAR : EmploymentType.valueOf(rawValue);
    }

    private static LocalDate parseStartDate(String rawValue) {
        LocalDate startDate = rawValue == null || rawValue.isBlank() ? LocalDate.now() : LocalDate.parse(rawValue);
        return startDate.isBefore(LocalDate.now()) ? LocalDate.now() : startDate;
    }
}
