package dataaccess.dao;

import dataaccess.dto.BranchDto;
import dataaccess.dto.ConstraintDto;
import dataaccess.dto.EmployeeDto;
import dataaccess.dto.PreferenceDto;
import dataaccess.dto.SalaryDto;
import dataaccess.dto.WeeklyAvailabilityRequestDto;
import dataaccess.mapper.BankAccountMapper;
import dataaccess.mapper.EmploymentTermsMapper;
import dataaccess.mapper.SalaryMapper;
import dataaccess.repository.RepositoryException;
import employee.domain.EmploymentScope;
import employee.domain.EmploymentType;
import employee.domain.Role;
import employee.domain.ShiftType;
import employee.domain.WeeklyAvailabilityRequest;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.*;

public class EmployeeDAOImpl implements DaoInterface<EmployeeDto> {

    private final Connection connection;

    public EmployeeDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(EmployeeDto e) {
        String sql = """
                INSERT OR REPLACE INTO employees (
                    employee_id, name,
                    bank_number, bank_branch_number, bank_account_number,
                    employment_type, employment_scope,
                    hourly_salary, global_salary,
                    start_date, is_fired, vacation_days,
                    branch_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            fillEmployeeStatement(pstmt, e);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public EmployeeDto findbyId(String id) {
        String sql = """
                SELECT e.*, u.password
                FROM employees e
                JOIN users u ON e.employee_id = u.user_id
                WHERE e.employee_id = ?
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return buildEmployeeDto(rs);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void update(EmployeeDto e) {
        createOrUpdate(e);
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<EmployeeDto> findAll() {
        List<EmployeeDto> employees = new ArrayList<>();

        String sql = """
                SELECT e.*, u.password
                FROM employees e
                JOIN users u ON e.employee_id = u.user_id
                ORDER BY e.employee_id
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                employees.add(buildEmployeeDto(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return employees;
    }

    private EmployeeDto buildEmployeeDto(ResultSet rs) throws SQLException {
        String employeeId = rs.getString("employee_id");

        return new EmployeeDto(
            employeeId,
            rs.getString("password"),
            BankAccountMapper.mapResultSetToBankAccount(rs),
            rs.getString("name"),
            new SalaryDto(rs.getDouble("global_salary"),
                        rs.getDouble("hourly_salary"),
                        rs.getDouble("worked_hours"),
                        rs.getDouble("overtime_hours"),
                        rs.getDouble("final_salary"),
                        EmploymentScope.valueOf(rs.getString("employment_scope"))
                ),
            EmploymentType.valueOf(rs.getString("employment_type")),
            EmploymentTermsMapper.toDto(EmploymentTermsMapper.mapResultSetToEmploymentTerms(rs)),
            loadRoles(employeeId),
            rs.getBoolean("can_manage_shift"),
            rs.getInt("is_fired") == 1,
            DayOfWeek.SUNDAY,
            new WeeklyAvailabilityRequestDto(
                    rs.getDate("week_start_date") == null ? null : rs.getDate("week_start_date").toLocalDate(),
                    rs.getDate("submission_deadline") == null ? null : rs.getDate("submission_deadline").toLocalDate(),
                    loadConstraints(employeeId),
                    loadPreferences(employeeId)
            ),                            
            loadBranch(rs)
    );
    }

    private List<PreferenceDto> loadPreferences(String employeeId) throws SQLException {
            String sql = """
                    SELECT p.day_of_week, p.shift_type
                    FROM weekly_availability_preferences p
                    JOIN weeklyavailabilityrequests r ON p.request_id = r.request_id
                    WHERE r.employee_id = ?
                    """;

            List<PreferenceDto> preferences = new ArrayList<>();

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, employeeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        preferences.add(new PreferenceDto(
                                DayOfWeek.valueOf(rs.getString("day_of_week")),
                                ShiftType.valueOf(rs.getString("shift_type"))
                        ));
                    }
                }
            }

            return preferences;
        }

    private List<ConstraintDto> loadConstraints(String employeeId) throws SQLException {
            String sql = """
                    SELECT c.day_of_week, c.shift_type
                    FROM weekly_availability_constraints c
                    JOIN weeklyavailabilityrequests r ON c.request_id = r.request_id
                    WHERE r.employee_id = ?
                    """;

            List<ConstraintDto> constraints = new ArrayList<>();

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, employeeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        constraints.add(new ConstraintDto(
                                DayOfWeek.valueOf(rs.getString("day_of_week")),
                                ShiftType.valueOf(rs.getString("shift_type"))
                        ));
                    }
                }
            }

            return constraints;
        }

            private BranchDto loadBranch(ResultSet rs) throws SQLException {
                String branchId = rs.getString("branch_id");

                if (branchId == null) {
                    return null;
                }

                try {
                    return new BranchDAOImpl(connection).findbyId(branchId);
                } catch (RepositoryException ex) {
                    throw new SQLException("Failed to load branch " + branchId, ex);
                }
            }

    private void fillEmployeeStatement(PreparedStatement pstmt, EmployeeDto e)
            throws SQLException {

        pstmt.setString(1, e.getId());
        pstmt.setString(2, e.getName());
        pstmt.setString(3, e.getBankAccount().getBankNumber());
        pstmt.setString(4, e.getBankAccount().getBranchNumber());
        pstmt.setString(5, e.getBankAccount().getAccountNumber());
        pstmt.setString(6, e.getEmploymentType().name());
        pstmt.setString(7, e.getEmploymentTerms().getEmploymentScope().name());
        pstmt.setDouble(8, e.getEmploymentTerms().getHourlySalary());
        pstmt.setDouble(9, e.getEmploymentTerms().getGlobalSalary());
        pstmt.setString(10, e.getEmploymentTerms().getStartDate().toString());
        pstmt.setInt(11, e.isFired() ? 1 : 0);
        pstmt.setInt(12, e.getEmploymentTerms().getVacationDays());

        if (e.getBranch() == null) {
            pstmt.setNull(13, Types.INTEGER);
        } else {
            pstmt.setInt(13, Integer.parseInt(e.getBranch().getBranchId()));
        }
    }

    private Set<Role> loadRoles(String employeeId) throws SQLException {
        String sql = "SELECT role_name FROM employee_roles WHERE employee_id = ?";
        Set<Role> roles = new HashSet<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, employeeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(Role.valueOf(rs.getString("role_name")));
                }
            }
        }

        return roles;
    }
    
}