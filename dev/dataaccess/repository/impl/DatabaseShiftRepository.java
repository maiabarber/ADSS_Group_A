package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.ShiftRepository;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.Role;
import employee.domain.Shift;
import employee.domain.ShiftAssignment;
import employee.domain.ShiftType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseShiftRepository implements ShiftRepository {
    private final EmployeeRepositoryImpl employeeRepository = new EmployeeRepositoryImpl();

    public DatabaseShiftRepository() {
        ensureSchema();
    }

    @Override
    public Shift save(Shift shift) throws RepositoryException {
        if (shift == null || shift.getDate() == null || shift.getShiftType() == null) {
            throw new RepositoryException("Shift date and type are required");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int shiftId = findOrCreateShiftId(connection, shift);
                replaceAssignments(connection, shiftId, shift);
                connection.commit();
                return shift;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save shift", e);
        }
    }

    @Override
    public Optional<Shift> findById(String id) throws RepositoryException {
        try {
            return findByShiftId(Integer.parseInt(id));
        } catch (NumberFormatException e) {
            throw new RepositoryException("Shift id must be numeric", e);
        }
    }

    @Override
    public List<Shift> findAll() throws RepositoryException {
        String sql = """
                SELECT s.shift_id, s.shift_date, s.shift_type, b.branch_id, b.branch_name, b.address
                FROM shifts s
                LEFT JOIN branches b ON b.branch_id = s.branch_id
                ORDER BY s.shift_date, s.shift_type
                """;
        List<Shift> shifts = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                shifts.add(mapShiftWithAssignments(connection, resultSet));
            }
            return shifts;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to list shifts", e);
        }
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteAssignments = connection.prepareStatement("DELETE FROM shift_assignments WHERE shift_id = ?");
                 PreparedStatement deleteShift = connection.prepareStatement("DELETE FROM shifts WHERE shift_id = ?")) {
                int shiftId = Integer.parseInt(id);
                deleteAssignments.setInt(1, shiftId);
                deleteAssignments.executeUpdate();
                deleteShift.setInt(1, shiftId);
                deleteShift.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException | NumberFormatException e) {
            throw new RepositoryException("Failed to delete shift", e);
        }
    }

    @Override
    public Optional<Shift> findByDateAndType(LocalDate date, ShiftType shiftType) {
        String sql = """
                SELECT s.shift_id, s.shift_date, s.shift_type, b.branch_id, b.branch_name, b.address
                FROM shifts s
                LEFT JOIN branches b ON b.branch_id = s.branch_id
                WHERE s.shift_date = ? AND s.shift_type = ?
                ORDER BY s.shift_id
                LIMIT 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            statement.setString(2, shiftType.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapShiftWithAssignments(connection, resultSet));
            }
        } catch (SQLException | RepositoryException e) {
            return Optional.empty();
        }
    }

    private Optional<Shift> findByShiftId(int shiftId) throws RepositoryException {
        String sql = """
                SELECT s.shift_id, s.shift_date, s.shift_type, b.branch_id, b.branch_name, b.address
                FROM shifts s
                LEFT JOIN branches b ON b.branch_id = s.branch_id
                WHERE s.shift_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shiftId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapShiftWithAssignments(connection, resultSet));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shift", e);
        }
    }

    private Shift mapShiftWithAssignments(Connection connection, ResultSet resultSet) throws SQLException, RepositoryException {
        int shiftId = resultSet.getInt("shift_id");
        Branch branch = DomainMapper.mapBranch(resultSet);
        Shift shift = new Shift(
                LocalDate.parse(resultSet.getString("shift_date")),
                ShiftType.valueOf(resultSet.getString("shift_type")),
                null,
                1,
                1,
                branch);
        shift.setAssignments(loadAssignments(connection, shiftId, shift));
        return shift;
    }

    private List<ShiftAssignment> loadAssignments(Connection connection, int shiftId, Shift shift) throws SQLException, RepositoryException {
        List<ShiftAssignment> assignments = new ArrayList<>();
        String sql = "SELECT employee_id, role_name, status FROM shift_assignments WHERE shift_id = ? ORDER BY assignment_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shiftId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Optional<Employee> employee = employeeRepository.findById(resultSet.getString("employee_id"));
                    if (employee.isPresent()) {
                        assignments.add(DomainMapper.mapShiftAssignment(
                                employee.get(),
                                shift,
                                Role.valueOf(resultSet.getString("role_name")),
                                resultSet.getString("status")));
                    }
                }
            }
        }
        return assignments;
    }

    private int findOrCreateShiftId(Connection connection, Shift shift) throws SQLException {
        String findSql = "SELECT shift_id FROM shifts WHERE shift_date = ? AND shift_type = ? ORDER BY shift_id LIMIT 1";
        try (PreparedStatement findStatement = connection.prepareStatement(findSql)) {
            findStatement.setString(1, shift.getDate().toString());
            findStatement.setString(2, shift.getShiftType().name());
            try (ResultSet resultSet = findStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("shift_id");
                }
            }
        }

        String insertSql = "INSERT INTO shifts (shift_date, shift_type, branch_id) VALUES (?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, shift.getDate().toString());
            insertStatement.setString(2, shift.getShiftType().name());
            Integer branchId = DomainMapper.parseBranchId(shift.getBranch());
            if (branchId == null) {
                insertStatement.setNull(3, java.sql.Types.INTEGER);
            } else {
                insertStatement.setInt(3, branchId);
            }
            insertStatement.executeUpdate();
            try (ResultSet keys = insertStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not determine shift id");
    }

    private void replaceAssignments(Connection connection, int shiftId, Shift shift) throws SQLException, RepositoryException {
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM shift_assignments WHERE shift_id = ?");
             PreparedStatement insertStatement = connection.prepareStatement(
                     "INSERT INTO shift_assignments (shift_id, employee_id, role_name, status) VALUES (?, ?, ?, ?)")) {
            deleteStatement.setInt(1, shiftId);
            deleteStatement.executeUpdate();
            for (ShiftAssignment assignment : shift.getAssignments()) {
                insertStatement.setInt(1, shiftId);
                insertStatement.setString(2, assignment.getEmployee().getId());
                insertStatement.setString(3, assignment.getRole().name());
                insertStatement.setString(4, assignment.isApproved() ? "APPROVED" : "PENDING");
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    private void ensureSchema() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        }
    }
}
