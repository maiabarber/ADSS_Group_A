package dataaccess.dao;

import dataaccess.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

// This class writes employee data into the SQLite database.
public class EmployeeWriteDataAccess {

    public void insertUser(String userId, String password, boolean isHrManager) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO users (user_id, password, is_hr_manager)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, userId);
            statement.setString(2, password);
            statement.setInt(3, isHrManager ? 1 : 0);
            statement.executeUpdate();
        }
    }

    public void insertBranch(int branchId, String branchName, String address) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO branches (branch_id, branch_name, address)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, branchId);
            statement.setString(2, branchName);
            statement.setString(3, address);
            statement.executeUpdate();
        }
    }

    public void insertEmployee(String employeeId,
                               String name,
                               String bankAccount,
                               String employmentType,
                               String employmentScope,
                               double hourlySalary,
                               double globalSalary,
                               String startDate,
                               boolean isFired,
                               int vacationDays,
                               Integer branchId) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO employees (
                    employee_id,
                    name,
                    bank_account,
                    employment_type,
                    employment_scope,
                    hourly_salary,
                    global_salary,
                    start_date,
                    is_fired,
                    vacation_days,
                    branch_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId);
            statement.setString(2, name);
            statement.setString(3, bankAccount);
            statement.setString(4, employmentType);
            statement.setString(5, employmentScope);
            statement.setDouble(6, hourlySalary);
            statement.setDouble(7, globalSalary);
            statement.setString(8, startDate);
            statement.setInt(9, isFired ? 1 : 0);
            statement.setInt(10, vacationDays);

            if (branchId == null) {
                statement.setNull(11, Types.INTEGER);
            } else {
                statement.setInt(11, branchId);
            }

            statement.executeUpdate();
        }
    }

    public void insertEmployeeRole(String employeeId, String roleName) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO employee_roles (employee_id, role_name)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId);
            statement.setString(2, roleName);
            statement.executeUpdate();
        }
    }

    public void insertShift(int shiftId,
                            String shiftDate,
                            String shiftType,
                            Integer branchId) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO shifts (
                    shift_id,
                    shift_date,
                    shift_type,
                    branch_id
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, shiftId);
            statement.setString(2, shiftDate);
            statement.setString(3, shiftType);

            if (branchId == null) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, branchId);
            }

            statement.executeUpdate();
        }
    }

    public void insertShiftAssignment(int assignmentId,
                                      int shiftId,
                                      String employeeId,
                                      String roleName,
                                      String status) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO shift_assignments (
                    assignment_id,
                    shift_id,
                    employee_id,
                    role_name,
                    status
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assignmentId);
            statement.setInt(2, shiftId);
            statement.setString(3, employeeId);
            statement.setString(4, roleName);
            statement.setString(5, status);
            statement.executeUpdate();
        }
    }
}