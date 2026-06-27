package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.repository.RepositoryException;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.Role;
import dataaccess.repository.EmployeeRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EmployeeRepositoryImpl implements EmployeeRepository {
    private static final Map<String, Employee> IDENTITY_CACHE = new ConcurrentHashMap<>();

    public EmployeeRepositoryImpl() {
        ensureSchema();
    }

    @Override
    public Employee save(Employee employee) throws RepositoryException {
        if (employee == null) {
            throw new RepositoryException("Employee cannot be null");
        }

        String userSql = """
                INSERT INTO users (user_id, password, is_hr_manager)
                VALUES (?, ?, 0)
                ON CONFLICT(user_id) DO UPDATE SET password = excluded.password
                """;
        String employeeSql = """
                INSERT INTO employees (
                    employee_id, name, bank_account, employment_type, employment_scope,
                    hourly_salary, global_salary, start_date, is_fired, vacation_days, branch_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(employee_id) DO UPDATE SET
                    name = excluded.name,
                    bank_account = excluded.bank_account,
                    employment_type = excluded.employment_type,
                    employment_scope = excluded.employment_scope,
                    hourly_salary = excluded.hourly_salary,
                    global_salary = excluded.global_salary,
                    start_date = excluded.start_date,
                    is_fired = excluded.is_fired,
                    vacation_days = excluded.vacation_days,
                    branch_id = excluded.branch_id
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStatement = connection.prepareStatement(userSql);
                 PreparedStatement employeeStatement = connection.prepareStatement(employeeSql)) {
                userStatement.setString(1, employee.getId());
                userStatement.setString(2, employee.getPassword());
                userStatement.executeUpdate();

                employeeStatement.setString(1, employee.getId());
                employeeStatement.setString(2, employee.getName());
                employeeStatement.setString(3, DomainMapper.formatBankAccount(employee.getBankAccount()));
                employeeStatement.setString(4, employee.getEmploymentType().name());
                employeeStatement.setString(5, employee.getEmploymentTerms().getEmploymentScope().name());
                employeeStatement.setDouble(6, employee.getEmploymentTerms().getHourlySalary());
                employeeStatement.setDouble(7, employee.getEmploymentTerms().getGlobalSalary());
                employeeStatement.setString(8, employee.getEmploymentTerms().getStartDate().toString());
                employeeStatement.setInt(9, employee.isFired() ? 1 : 0);
                employeeStatement.setInt(10, employee.getVacationDaysBalance());
                Integer branchId = DomainMapper.parseBranchId(employee.getBranch());
                if (branchId == null) {
                    employeeStatement.setNull(11, java.sql.Types.INTEGER);
                } else {
                    employeeStatement.setInt(11, branchId);
                }
                employeeStatement.executeUpdate();
                replaceRoles(connection, employee);
                connection.commit();
                IDENTITY_CACHE.put(cacheKey(employee.getId()), employee);
                return employee;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save employee", e);
        }
    }

    @Override
    public Optional<Employee> findById(String id) throws RepositoryException {
        String sql = """
                SELECT e.*, u.password, b.branch_id, b.branch_name, b.address
                FROM employees e
                LEFT JOIN users u ON u.user_id = e.employee_id
                LEFT JOIN branches b ON b.branch_id = e.branch_id
                WHERE e.employee_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                Employee cached = IDENTITY_CACHE.get(cacheKey(id));
                if (cached != null) {
                    return Optional.of(cached);
                }
                Branch branch = DomainMapper.mapBranch(resultSet);
                Employee employee = DomainMapper.mapEmployee(resultSet, loadRoles(connection, id), branch);
                IDENTITY_CACHE.put(cacheKey(id), employee);
                return Optional.of(employee);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find employee", e);
        }
    }

    @Override
    public List<Employee> findAll() throws RepositoryException {
        String sql = """
                SELECT e.*, u.password, b.branch_id, b.branch_name, b.address
                FROM employees e
                LEFT JOIN users u ON u.user_id = e.employee_id
                LEFT JOIN branches b ON b.branch_id = e.branch_id
                ORDER BY e.employee_id
                """;
        List<Employee> employees = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String employeeId = resultSet.getString("employee_id");
                Employee cached = IDENTITY_CACHE.get(cacheKey(employeeId));
                if (cached != null) {
                    employees.add(cached);
                } else {
                    Employee employee = DomainMapper.mapEmployee(
                            resultSet,
                            loadRoles(connection, employeeId),
                            DomainMapper.mapBranch(resultSet));
                    IDENTITY_CACHE.put(cacheKey(employeeId), employee);
                    employees.add(employee);
                }
            }
            return employees;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to list employees", e);
        }
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteRoles = connection.prepareStatement("DELETE FROM employee_roles WHERE employee_id = ?");
                 PreparedStatement deleteEmployee = connection.prepareStatement("DELETE FROM employees WHERE employee_id = ?");
                 PreparedStatement deleteUser = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                deleteRoles.setString(1, id);
                deleteRoles.executeUpdate();
                deleteEmployee.setString(1, id);
                deleteEmployee.executeUpdate();
                deleteUser.setString(1, id);
                deleteUser.executeUpdate();
                connection.commit();
                IDENTITY_CACHE.remove(cacheKey(id));
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete employee", e);
        }
    }

    private Set<Role> loadRoles(Connection connection, String employeeId) throws SQLException {
        Set<Role> roles = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT role_name FROM employee_roles WHERE employee_id = ?")) {
            statement.setString(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Role role = parseRole(resultSet.getString("role_name"));
                    if (role != null) {
                        roles.add(role);
                    }
                }
            }
        }
        return roles;
    }

    private void replaceRoles(Connection connection, Employee employee) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement(
                "DELETE FROM employee_roles WHERE employee_id = ?");
             PreparedStatement insertStatement = connection.prepareStatement(
                     "INSERT INTO employee_roles (employee_id, role_name) VALUES (?, ?)")) {
            deleteStatement.setString(1, employee.getId());
            deleteStatement.executeUpdate();
            for (Role role : employee.getAuthorizedRoles()) {
                insertStatement.setString(1, employee.getId());
                insertStatement.setString(2, role.name());
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    private Role parseRole(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(rawValue);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void ensureSchema() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        }
    }

    private String cacheKey(String employeeId) {
        return DatabaseConnection.getDatabasePath() + ":" + employeeId;
    }
}
