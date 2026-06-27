package dataaccess.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataaccess.dto.BranchDto;
import dataaccess.mapper.BankAccountMapper;
import dataaccess.mapper.BranchMapper;
import dataaccess.mapper.EmploymentTermsMapper;
import dataaccess.mapper.SalaryMapper;
import dataaccess.repository.RepositoryException;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.EmploymentType;
import employee.domain.Role;
import employee.domain.WeeklyAvailabilityRequest;

public class EmployeeDAOImpl implements DaoInterface<Employee> {
    private final Connection connection;

    public EmployeeDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(Employee e) {
        String sql = """
            INSERT OR REPLACE INTO employees (
                employee_id,
                name,
                bank_number,
                bank_branch_number,
                bank_account_number,
                employment_type,
                employment_scope,
                hourly_salary,
                global_salary,
                start_date,
                is_fired,
                vacation_days,
                branch_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

            if (e.getBranch() != null) {
                pstmt.setInt(13, Integer.parseInt(e.getBranch().getBranchId()));
            } else {
                pstmt.setNull(13, java.sql.Types.INTEGER);
            }

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
        public Employee findbyId(String id) {
            String sql = """
                    SELECT e.*,
                        u.password,
                        b.branch_name,
                        b.address AS branch_address
                    FROM employees e
                    JOIN users u ON e.employee_id = u.user_id
                    LEFT JOIN branches b ON e.branch_id = b.branch_id
                    WHERE e.employee_id = ?
                    """;

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Set<Role> roles = loadRoles(id);
                        BranchDto branch = null;

                        String branchId = rs.getString("branch_id");

                        if (branchId != null) {
                            BranchDAOImpl branchDao = new BranchDAOImpl(connection);
                            try {
                                branch = branchDao.findbyId(branchId);
                            } catch (RepositoryException e) {
                                e.printStackTrace();
                            }
                            Branch branchDomain = BranchMapper.toDomain(branch);
                            return new Employee(
                                rs.getString("employee_id"),
                                rs.getString("password"),
                                BankAccountMapper.mapResultSetToBankAccount(rs),
                                rs.getString("name"),
                                SalaryMapper.mapResultSetToSalary(rs),
                                EmploymentType.valueOf(rs.getString("employment_type")),
                                EmploymentTermsMapper.mapResultSetToEmploymentTerms(rs),
                                roles,
                                rs.getBoolean("can_manage_shift"),
                                rs.getInt("is_fired") == 1,
                                DayOfWeek.SUNDAY,
                                new WeeklyAvailabilityRequest(),
                                branchDomain
                        );
                        } else {
                            return new Employee(
                                rs.getString("employee_id"),
                                rs.getString("password"),
                                BankAccountMapper.mapResultSetToBankAccount(rs),
                                rs.getString("name"),
                                SalaryMapper.mapResultSetToSalary(rs),
                                EmploymentType.valueOf(rs.getString("employment_type")),
                                EmploymentTermsMapper.mapResultSetToEmploymentTerms(rs),
                                roles,
                                rs.getBoolean("can_manage_shift"),
                                rs.getInt("is_fired") == 1,
                                DayOfWeek.SUNDAY,
                                new WeeklyAvailabilityRequest(),
                                null
                            );
                        }                       
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(Employee e) {
        String sql = """
            UPDATE employees SET
                name = ?,
                bank_number = ?,
                bank_branch_number = ?,
                bank_account_number = ?,
                employment_type = ?,
                employment_scope = ?,
                hourly_salary = ?,
                global_salary = ?,
                start_date = ?,
                is_fired = ?,
                vacation_days = ?,
                branch_id = ?
            WHERE employee_id = ?
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, e.getName());
            pstmt.setString(2, e.getBankAccount().getBankNumber());
            pstmt.setString(3, e.getBankAccount().getBranchNumber());
            pstmt.setString(4, e.getBankAccount().getAccountNumber());
            pstmt.setString(5, e.getEmploymentType().name());
            pstmt.setString(6, e.getEmploymentTerms().getEmploymentScope().name());
            pstmt.setDouble(7, e.getEmploymentTerms().getHourlySalary());
            pstmt.setDouble(8, e.getEmploymentTerms().getGlobalSalary());
            pstmt.setString(9, e.getEmploymentTerms().getStartDate().toString());
            pstmt.setInt(10, e.isFired() ? 1 : 0);
            pstmt.setInt(11, e.getEmploymentTerms().getVacationDays());

            if (e.getBranch() != null) {
                pstmt.setInt(12, Integer.parseInt(e.getBranch().getBranchId()));
            } else {
                pstmt.setNull(12, java.sql.Types.INTEGER);
            }

            pstmt.setString(13, e.getId());

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
        public List<Employee> findAll() {
            List<Employee> employees = new ArrayList<>();

            String sql = """
                    SELECT e.*,
                        u.password,
                        b.branch_name,
                        b.address AS branch_address
                    FROM employees e
                    JOIN users u ON e.employee_id = u.user_id
                    LEFT JOIN branches b ON e.branch_id = b.branch_id
                    ORDER BY e.employee_id
                    """;

            try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    Set<Role> roles = loadRoles(rs.getString("employee_id"));

                    Branch branchDomain = null;

                    String branchId = rs.getString("branch_id");

                    if (branchId != null) {
                        BranchDAOImpl branchDao = new BranchDAOImpl(connection);

                        try {
                            BranchDto branchDto = branchDao.findbyId(branchId);
                            branchDomain = BranchMapper.toDomain(branchDto);
                        } catch (RepositoryException e) {
                            e.printStackTrace();
                        }
                    }

                    employees.add(
                            new Employee(
                                    rs.getString("employee_id"),
                                    rs.getString("password"),
                                    BankAccountMapper.mapResultSetToBankAccount(rs),
                                    rs.getString("name"),
                                    SalaryMapper.mapResultSetToSalary(rs),
                                    EmploymentType.valueOf(rs.getString("employment_type")),
                                    EmploymentTermsMapper.mapResultSetToEmploymentTerms(rs),
                                    roles,
                                    rs.getBoolean("can_manage_shift"),
                                    rs.getInt("is_fired") == 1,
                                    DayOfWeek.SUNDAY,
                                    new WeeklyAvailabilityRequest(),
                                    branchDomain
                            )
                    );
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return employees;
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