package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.BankAccountDto;
import dataaccess.dto.BranchDto;
import dataaccess.dto.BranchManagerDto;
import dataaccess.dto.ConstraintDto;
import dataaccess.dto.DriverAssignmentRequestDto;
import dataaccess.dto.EmployeeDto;
import dataaccess.dto.EmployeeRoleDto;
import dataaccess.dto.EmploymentTermsDto;
import dataaccess.dto.HrManagerDto;
import dataaccess.dto.PreferenceDto;
import dataaccess.dto.SalaryDto;
import dataaccess.dto.ShiftAssignmentDto;
import dataaccess.dto.ShiftDto;
import dataaccess.dto.UserDto;
import dataaccess.dto.WeeklyAvailabilityRequestDto;

import employee.domain.EmploymentScope;
import employee.domain.EmploymentType;
import employee.domain.Role;
import employee.domain.ShiftType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class reads employee data from the SQLite database.
public class EmployeeDataAccess {

    public List<UserDto> listUsers() throws SQLException {
        String sql = """
                SELECT user_id, password, is_hr_manager
                FROM users
                ORDER BY user_id
                """;

        List<UserDto> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                String password = resultSet.getString("password");
                boolean isHrManager = resultSet.getInt("is_hr_manager") == 1;
                if (isHrManager) {
                    users.add(new HrManagerDto(userId, password, true));
                } else {
                    users.add(new UserDto(userId, password));
                }
            }
        }

        return users;
    }

    public List<BranchDto> listBranches() throws SQLException {
        String sql = """
                SELECT branch_id, branch_name, address
                FROM branches
                ORDER BY branch_id
                """;

        List<BranchDto> branches = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                branches.add(new BranchDto(
                        String.valueOf(resultSet.getInt("branch_id")),
                        resultSet.getString("branch_name"),
                        resultSet.getString("address"),
                        null
                ));
            }
        }

        return branches;
    }

    public BranchManagerDto getBranchManagerView() throws SQLException {
        return new BranchManagerDto(listBranches());
    }

    public List<EmployeeRoleDto> listEmployeeRoles() throws SQLException {
        String sql = """
                SELECT employee_id, role_name
                FROM employee_roles
                ORDER BY employee_id, role_name
                """;

        List<EmployeeRoleDto> roles = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                roles.add(new EmployeeRoleDto(
                        resultSet.getString("employee_id"),
                        Role.valueOf(resultSet.getString("role_name"))
                ));
            }
        }

        return roles;
    }

    public List<EmployeeDto> listEmployees() throws SQLException {
        String sql = """
                SELECT
                    e.employee_id,
                    e.name,
                    e.bank_account,
                    e.employment_type,
                    e.employment_scope,
                    e.hourly_salary,
                    e.global_salary,
                    e.start_date,
                    e.is_fired,
                    e.vacation_days,
                    e.branch_id,
                    b.branch_name,
                    b.address
                FROM employees e
                LEFT JOIN branches b ON e.branch_id = b.branch_id
                ORDER BY e.employee_id
                """;

        Map<String, List<Role>> rolesByEmployee = new HashMap<>();
        for (EmployeeRoleDto roleDto : listEmployeeRoles()) {
            rolesByEmployee.computeIfAbsent(roleDto.getEmployeeId(), key -> new ArrayList<>()).add(roleDto.getRole());
        }

        List<EmployeeDto> employees = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String employeeId = resultSet.getString("employee_id");
                employees.add(new EmployeeDto(
                        employeeId,
                        resultSet.getString("name"),
                        parseBankAccount(resultSet.getString("bank_account")),
                        new SalaryDto(
                                resultSet.getDouble("global_salary"),
                                resultSet.getDouble("hourly_salary"),
                                0,
                                parseEmploymentScope(resultSet.getString("employment_scope"))
                        ),
                        parseEmploymentType(resultSet.getString("employment_type")),
                        new EmploymentTermsDto(
                                parseLocalDate(resultSet.getString("start_date")),
                                parseEmploymentScope(resultSet.getString("employment_scope")),
                                resultSet.getDouble("global_salary"),
                                resultSet.getDouble("hourly_salary"),
                                resultSet.getInt("vacation_days")
                        ),
                        new java.util.HashSet<>(rolesByEmployee.getOrDefault(employeeId, Collections.emptyList())),
                        false,
                        resultSet.getInt("is_fired") == 1,
                        null,
                        new WeeklyAvailabilityRequestDto(null, Collections.<ConstraintDto>emptyList(), Collections.<PreferenceDto>emptyList()),
                        resultSet.getString("branch_id") == null ? null : new BranchDto(
                                resultSet.getString("branch_id"),
                                resultSet.getString("branch_name"),
                                resultSet.getString("address"),
                                null
                        )
                ));
            }
        }

        return employees;
    }

    public List<ShiftDto> listShifts() throws SQLException {
        String sql = """
                SELECT
                    s.shift_id,
                    s.shift_date,
                    s.shift_type,
                    s.branch_id,
                    b.branch_name,
                    b.address
                FROM shifts s
                LEFT JOIN branches b ON s.branch_id = b.branch_id
                ORDER BY s.shift_id
                """;

        List<ShiftDto> shifts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                shifts.add(new ShiftDto(
                        parseLocalDate(resultSet.getString("shift_date")),
                        ShiftType.valueOf(resultSet.getString("shift_type")),
                        null,
                        resultSet.getString("branch_id") == null ? null : new BranchDto(
                                resultSet.getString("branch_id"),
                                resultSet.getString("branch_name"),
                                resultSet.getString("address"),
                                null
                        ),
                        new EnumMap<>(Role.class),
                        Collections.emptyList(),
                        false,
                        null
                ));
            }
        }

        return shifts;
    }

    public List<ShiftAssignmentDto> listShiftAssignments() throws SQLException {
        String sql = """
                SELECT
                    sa.assignment_id,
                    sa.shift_id,
                    s.shift_date,
                    s.shift_type,
                    sa.employee_id,
                    sa.role_name,
                    sa.status
                FROM shift_assignments sa
                JOIN shifts s ON sa.shift_id = s.shift_id
                ORDER BY sa.assignment_id
                """;

        List<ShiftAssignmentDto> assignments = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                boolean approved = "APPROVED".equalsIgnoreCase(resultSet.getString("status"));
                boolean requiresApproval = !approved;
                assignments.add(new ShiftAssignmentDto(
                        resultSet.getString("employee_id"),
                        parseLocalDate(resultSet.getString("shift_date")),
                        ShiftType.valueOf(resultSet.getString("shift_type")),
                        Role.valueOf(resultSet.getString("role_name")),
                        approved,
                        requiresApproval,
                        false,
                        false
                ));
            }
        }

        return assignments;
    }

    public List<DriverAssignmentRequestDto> listDriverAssignmentRequests() throws SQLException {
        List<DriverAssignmentRequestDto> requests = new ArrayList<>();
        for (ShiftAssignmentDto assignment : listShiftAssignments()) {
            if (assignment.getRole() == Role.DRIVER) {
                requests.add(new DriverAssignmentRequestDto(
                        assignment.getEmployeeId(),
                        0,
                        assignment.getShiftDate().atStartOfDay(),
                        assignment.getShiftType(),
                        assignment.isApproved(),
                        assignment.isApproved() ? "Driver was assigned to the required shift" : "Waiting for HR manager to assign driver to shift"
                ));
            }
        }
        return requests;
    }

    public void printAllUsers() throws SQLException {
        System.out.println("Users from database:");
        for (UserDto user : listUsers()) {
            boolean isHrManager = user instanceof HrManagerDto;
            System.out.println(user.getId() + " | HR manager: " + isHrManager);
        }
    }

    public void printAllBranches() throws SQLException {
        System.out.println("Branches from database:");
        for (BranchDto branch : listBranches()) {
            System.out.println(branch.getBranchId() + " | " + branch.getBranchName() + " | " + branch.getLocation());
        }
    }

    public void printAllEmployees() throws SQLException {
        System.out.println("Employees from database:");
        for (EmployeeDto employee : listEmployees()) {
            String branchName = employee.getBranch() == null ? "No branch / global employee" : employee.getBranch().getBranchName();
            System.out.println(
                    employee.getEmployeeId() +
                    " | " + employee.getName() +
                    " | type: " + employee.getEmploymentType() +
                    " | scope: " + employee.getEmploymentTerms().getEmploymentScope() +
                    " | hourly salary: " + employee.getSalary().getHourlySalary() +
                    " | global salary: " + employee.getSalary().getGlobalSalary() +
                    " | start date: " + employee.getEmploymentTerms().getStartDate() +
                    " | vacation days: " + employee.getEmploymentTerms().getVacationDays() +
                    " | fired: " + employee.isFired() +
                    " | branch: " + branchName
            );
        }
    }

    public void printAllEmployeeRoles() throws SQLException {
        System.out.println("Employee roles from database:");
        for (EmployeeRoleDto role : listEmployeeRoles()) {
            System.out.println(role.getEmployeeId() + " | role: " + role.getRole());
        }
    }

    public void printAllShifts() throws SQLException {
        System.out.println("Shifts from database:");
        int index = 1;
        for (ShiftDto shift : listShifts()) {
            String branchName = shift.getBranch() == null ? null : shift.getBranch().getBranchName();
            System.out.println(
                    "Shift #" + index++ +
                    " | date: " + shift.getDate() +
                    " | type: " + shift.getShiftType() +
                    " | branch: " + branchName
            );
        }
    }

    public void printAllShiftAssignments() throws SQLException {
        System.out.println("Shift assignments from database:");
        int index = 1;
        for (ShiftAssignmentDto assignment : listShiftAssignments()) {
            System.out.println(
                    "Assignment #" + index++ +
                    " | shift: " + assignment.getShiftDate() + " " + assignment.getShiftType() +
                    " | employee: " + assignment.getEmployeeId() +
                    " | role: " + assignment.getRole() +
                    " | status: " + (assignment.isApproved() ? "APPROVED" : "PENDING")
            );
        }
    }

    public int countEmployees() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM employees";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

            return 0;
        }
    }

    private static BankAccountDto parseBankAccount(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return new BankAccountDto(null, null, null);
        }

        String[] parts = rawValue.split("-");
        if (parts.length == 3) {
            return new BankAccountDto(parts[0], parts[1], parts[2]);
        }

        return new BankAccountDto(rawValue, null, null);
    }

    private static EmploymentScope parseEmploymentScope(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return EmploymentScope.FULL_TIME;
        }
        return EmploymentScope.valueOf(rawValue);
    }

    private static EmploymentType parseEmploymentType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return EmploymentType.REGULAR;
        }
        return EmploymentType.valueOf(rawValue);
    }

    private static LocalDate parseLocalDate(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return LocalDate.parse(rawValue);
    }
}