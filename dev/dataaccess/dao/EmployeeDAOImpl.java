package dataaccess.dao;

import dataaccess.dto.EmployeeDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAOImpl implements DaoInterface<EmployeeDto> {
    private final Connection connection;

    public EmployeeDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(EmployeeDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO employees (employee_id, name, bank_number, bank_branch_number, bank_account_number, employment_type, employment_scope, hourly_salary, global_salary, start_date, is_fired, vacation_days, branch_id, can_manage_shift, fixed_day_off, weekly_week_start_date, weekly_submission_deadline, weekly_constraints, weekly_preferences) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(employee_id) DO UPDATE SET name = excluded.name, bank_number = excluded.bank_number, bank_branch_number = excluded.bank_branch_number, bank_account_number = excluded.bank_account_number, employment_type = excluded.employment_type, employment_scope = excluded.employment_scope, hourly_salary = excluded.hourly_salary, global_salary = excluded.global_salary, start_date = excluded.start_date, is_fired = excluded.is_fired, vacation_days = excluded.vacation_days, branch_id = excluded.branch_id, can_manage_shift = excluded.can_manage_shift, fixed_day_off = excluded.fixed_day_off, weekly_week_start_date = excluded.weekly_week_start_date, weekly_submission_deadline = excluded.weekly_submission_deadline, weekly_constraints = excluded.weekly_constraints, weekly_preferences = excluded.weekly_preferences
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getEmployeeId());
            stmt.setString(2, dto.getName());
            stmt.setString(3, dto.getBankNumber());
            stmt.setString(4, dto.getBankBranchNumber());
            stmt.setString(5, dto.getBankAccountNumber());
            stmt.setString(6, dto.getEmploymentType());
            stmt.setString(7, dto.getEmploymentScope());
            stmt.setDouble(8, dto.getHourlySalary());
            stmt.setDouble(9, dto.getGlobalSalary());
            stmt.setString(10, dto.getStartDate());
            stmt.setInt(11, dto.isFired() ? 1 : 0);
            stmt.setInt(12, dto.getVacationDays());
            if (dto.getBranchId() == null) { stmt.setNull(13, Types.INTEGER); } else { stmt.setInt(13, dto.getBranchId()); }
            stmt.setInt(14, dto.canManageShift() ? 1 : 0);
            stmt.setString(15, dto.getFixedDayOff());
            stmt.setString(16, dto.getWeeklyWeekStartDate());
            stmt.setString(17, dto.getWeeklySubmissionDeadline());
            stmt.setString(18, dto.getWeeklyConstraints());
            stmt.setString(19, dto.getWeeklyPreferences());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save employees row", e);
        }
    }

    @Override
    public EmployeeDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT employee_id, name, bank_number, bank_branch_number, bank_account_number, employment_type, employment_scope, hourly_salary, global_salary, start_date, is_fired, vacation_days, branch_id, can_manage_shift, fixed_day_off, weekly_week_start_date, weekly_submission_deadline, weekly_constraints, weekly_preferences
                FROM employees
                WHERE employee_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setString(1, parts[0]);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find employees row", e);
        }
    }

    @Override
    public void update(EmployeeDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setString(1, parts[0]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete employees row", e);
        }
    }

    @Override
    public List<EmployeeDto> findAll() throws RepositoryException {
        String sql = """
                SELECT employee_id, name, bank_number, bank_branch_number, bank_account_number, employment_type, employment_scope, hourly_salary, global_salary, start_date, is_fired, vacation_days, branch_id, can_manage_shift, fixed_day_off, weekly_week_start_date, weekly_submission_deadline, weekly_constraints, weekly_preferences
                FROM employees
                """;
        List<EmployeeDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load employees rows", e);
        }
    }

    private EmployeeDto mapRow(ResultSet rs) throws SQLException {
        return new EmployeeDto(
                rs.getString("employee_id"),
                rs.getString("name"),
                rs.getString("bank_number"),
                rs.getString("bank_branch_number"),
                rs.getString("bank_account_number"),
                rs.getString("employment_type"),
                rs.getString("employment_scope"),
                rs.getDouble("hourly_salary"),
                rs.getDouble("global_salary"),
                rs.getString("start_date"),
                rs.getInt("is_fired") == 1,
                rs.getInt("vacation_days"),
                getNullableInt(rs, "branch_id"),
                rs.getInt("can_manage_shift") == 1,
                rs.getString("fixed_day_off"),
                rs.getString("weekly_week_start_date"),
                rs.getString("weekly_submission_deadline"),
                rs.getString("weekly_constraints"),
                rs.getString("weekly_preferences")
        );
    }

    private Integer getNullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}
