package dataaccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// This class reads employee data from the SQLite database.
public class EmployeeDataAccess {

    public void printAllUsers() throws SQLException {
        String sql = """
                SELECT user_id, is_hr_manager
                FROM users
                ORDER BY user_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Users from database:");
            while (resultSet.next()) {
                System.out.println(
                        resultSet.getString("user_id") +
                        " | HR manager: " + (resultSet.getInt("is_hr_manager") == 1)
                );
            }
        }
    }

    public void printAllBranches() throws SQLException {
        String sql = """
                SELECT branch_id, branch_name, address
                FROM branches
                ORDER BY branch_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Branches from database:");
            while (resultSet.next()) {
                System.out.println(
                        resultSet.getInt("branch_id") +
                        " | " + resultSet.getString("branch_name") +
                        " | " + resultSet.getString("address")
                );
            }
        }
    }

    public void printAllEmployees() throws SQLException {
        String sql = """
                SELECT
                    e.employee_id,
                    e.name,
                    e.employment_type,
                    e.employment_scope,
                    e.hourly_salary,
                    e.global_salary,
                    e.start_date,
                    e.is_fired,
                    e.vacation_days,
                    b.branch_name
                FROM employees e
                LEFT JOIN branches b ON e.branch_id = b.branch_id
                ORDER BY e.employee_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Employees from database:");
            while (resultSet.next()) {
                String branchName = resultSet.getString("branch_name");
                if (branchName == null) {
                    branchName = "No branch / global employee";
                }

                System.out.println(
                        resultSet.getString("employee_id") +
                        " | " + resultSet.getString("name") +
                        " | type: " + resultSet.getString("employment_type") +
                        " | scope: " + resultSet.getString("employment_scope") +
                        " | hourly salary: " + resultSet.getDouble("hourly_salary") +
                        " | global salary: " + resultSet.getDouble("global_salary") +
                        " | start date: " + resultSet.getString("start_date") +
                        " | vacation days: " + resultSet.getInt("vacation_days") +
                        " | fired: " + (resultSet.getInt("is_fired") == 1) +
                        " | branch: " + branchName
                );
            }
        }
    }

    public void printAllEmployeeRoles() throws SQLException {
        String sql = """
                SELECT employee_id, role_name
                FROM employee_roles
                ORDER BY employee_id, role_name
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Employee roles from database:");
            while (resultSet.next()) {
                System.out.println(
                        resultSet.getString("employee_id") +
                        " | role: " + resultSet.getString("role_name")
                );
            }
        }
    }

    public void printAllShifts() throws SQLException {
        String sql = """
                SELECT
                    s.shift_id,
                    s.shift_date,
                    s.shift_type,
                    b.branch_name
                FROM shifts s
                LEFT JOIN branches b ON s.branch_id = b.branch_id
                ORDER BY s.shift_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Shifts from database:");
            while (resultSet.next()) {
                System.out.println(
                        "Shift #" + resultSet.getInt("shift_id") +
                        " | date: " + resultSet.getString("shift_date") +
                        " | type: " + resultSet.getString("shift_type") +
                        " | branch: " + resultSet.getString("branch_name")
                );
            }
        }
    }

    public void printAllShiftAssignments() throws SQLException {
        String sql = """
                SELECT
                    sa.assignment_id,
                    sa.shift_id,
                    sa.employee_id,
                    e.name,
                    sa.role_name,
                    sa.status
                FROM shift_assignments sa
                JOIN employees e ON sa.employee_id = e.employee_id
                ORDER BY sa.assignment_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Shift assignments from database:");
            while (resultSet.next()) {
                System.out.println(
                        "Assignment #" + resultSet.getInt("assignment_id") +
                        " | shift: " + resultSet.getInt("shift_id") +
                        " | employee: " + resultSet.getString("employee_id") +
                        " - " + resultSet.getString("name") +
                        " | role: " + resultSet.getString("role_name") +
                        " | status: " + resultSet.getString("status")
                );
            }
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
}