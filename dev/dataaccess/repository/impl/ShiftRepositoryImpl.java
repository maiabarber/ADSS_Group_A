package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.EmployeeDAOImpl;
import dataaccess.dao.ShiftDaoImpl;
import dataaccess.dto.ShiftDto;
import dataaccess.mapper.ShiftMapper;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.ShiftRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShiftRepositoryImpl implements ShiftRepository {
    private final ShiftDaoImpl shiftDao;
    private final Map<String, Shift> savedShifts = new LinkedHashMap<>();

    public ShiftRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize ShiftRepository", e);
        }
        this.shiftDao = null;
    }

    public ShiftRepositoryImpl(ShiftDaoImpl shiftDao) {
        this.shiftDao = shiftDao;
    }

    @Override
    public Shift save(Shift shift) throws RepositoryException {
        if (shift == null) {
            return null;
        }

        savedShifts.put(shiftKey(shift.getDate(), shift.getShiftType()), shift);

        if (shiftDao != null) {
            shiftDao.createOrUpdate(ShiftMapper.toDto(shift));
            return shift;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            int shiftId = findOrCreateShiftId(connection, shift);
            replaceAssignments(connection, shiftId, shift);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save shift", e);
        }

        return shift;
    }

    @Override
    public Optional<Shift> findById(Integer id) throws RepositoryException {
        if (shiftDao != null) {
            return Optional.ofNullable(ShiftMapper.toDomain(shiftDao.findbyId(String.valueOf(id))));
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT shift_id, shift_date, shift_type, branch_id
                     FROM shifts
                     WHERE shift_id = ?
                     """)) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapShift(connection, rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shift", e);
        }
    }

    @Override
    public List<Shift> findAll() throws RepositoryException {
        if (!savedShifts.isEmpty()) {
            return new ArrayList<>(savedShifts.values());
        }

        List<Shift> shifts = new ArrayList<>();
        if (shiftDao != null) {
            for (ShiftDto dto : shiftDao.findAll()) {
                shifts.add(ShiftMapper.toDomain(dto));
            }
            return shifts;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("""
                     SELECT shift_id, shift_date, shift_type, branch_id
                     FROM shifts
                     """)) {
            while (rs.next()) {
                shifts.add(mapShift(connection, rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load shifts", e);
        }

        return shifts;
    }

    @Override
    public void deleteById(Integer id) throws RepositoryException {
        if (shiftDao != null) {
            shiftDao.delete(String.valueOf(id));
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement deleteAssignments = connection.prepareStatement(
                     "DELETE FROM shift_assignments WHERE shift_id = ?");
             PreparedStatement deleteShift = connection.prepareStatement(
                     "DELETE FROM shifts WHERE shift_id = ?")) {
            deleteAssignments.setInt(1, id);
            deleteAssignments.executeUpdate();

            deleteShift.setInt(1, id);
            deleteShift.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete shift", e);
        }
    }

    public Optional<Shift> findByDateAndType(LocalDate date, ShiftType shiftType) throws RepositoryException {
        Shift savedShift = savedShifts.get(shiftKey(date, shiftType));
        if (savedShift != null) {
            return Optional.of(savedShift);
        }

        if (shiftDao == null) {
            return findByDateAndTypeFromDatabase(date, shiftType);
        }

        for (ShiftDto dto : shiftDao.findAll()) {
            if (date.toString().equals(dto.getShiftDate()) && shiftType.name().equals(dto.getShiftType())) {
                return Optional.of(ShiftMapper.toDomain(dto));
            }
        }
        return Optional.empty();
    }

    private Optional<Shift> findByDateAndTypeFromDatabase(LocalDate date, ShiftType shiftType)
            throws RepositoryException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT shift_id, shift_date, shift_type, branch_id
                     FROM shifts
                     WHERE shift_date = ? AND shift_type = ?
                     """)) {
            statement.setString(1, date.toString());
            statement.setString(2, shiftType.name());

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapShift(connection, rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shift by date and type", e);
        }
    }

    private int findOrCreateShiftId(Connection connection, Shift shift) throws SQLException {
        try (PreparedStatement find = connection.prepareStatement("""
                SELECT shift_id
                FROM shifts
                WHERE shift_date = ? AND shift_type = ?
                """)) {
            find.setString(1, shift.getDate().toString());
            find.setString(2, shift.getShiftType().name());

            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    int shiftId = rs.getInt("shift_id");
                    updateShift(connection, shiftId, shift);
                    return shiftId;
                }
            }
        }

        try (PreparedStatement insert = connection.prepareStatement("""
                INSERT INTO shifts (shift_date, shift_type, branch_id)
                VALUES (?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, shift.getDate().toString());
            insert.setString(2, shift.getShiftType().name());
            if (getBranchId(shift) == null) {
                insert.setNull(3, java.sql.Types.INTEGER);
            } else {
                insert.setInt(3, getBranchId(shift));
            }
            insert.executeUpdate();

            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Could not create shift row");
    }

    private void updateShift(Connection connection, int shiftId, Shift shift) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement("""
                UPDATE shifts
                SET shift_date = ?, shift_type = ?, branch_id = ?
                WHERE shift_id = ?
                """)) {
            update.setString(1, shift.getDate().toString());
            update.setString(2, shift.getShiftType().name());
            if (getBranchId(shift) == null) {
                update.setNull(3, java.sql.Types.INTEGER);
            } else {
                update.setInt(3, getBranchId(shift));
            }
            update.setInt(4, shiftId);
            update.executeUpdate();
        }
    }

    private void replaceAssignments(Connection connection, int shiftId, Shift shift) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM shift_assignments WHERE shift_id = ?")) {
            delete.setInt(1, shiftId);
            delete.executeUpdate();
        }

        try (PreparedStatement insert = connection.prepareStatement("""
                INSERT INTO shift_assignments (shift_id, employee_id, role_name, status)
                VALUES (?, ?, ?, ?)
                """)) {
            for (ShiftAssignment assignment : shift.getAssignments()) {
                if (assignment == null || assignment.getEmployee() == null || assignment.getRole() == null) {
                    continue;
                }

                insert.setInt(1, shiftId);
                insert.setString(2, assignment.getEmployee().getId());
                insert.setString(3, assignment.getRole().name());
                insert.setString(4, assignment.isApproved() ? "APPROVED" : "PENDING");
                insert.addBatch();
            }

            insert.executeBatch();
        }
    }

    private Shift mapShift(Connection connection, ResultSet rs) throws SQLException, RepositoryException {
        Shift shift = ShiftMapper.toDomain(new ShiftDto(
                rs.getInt("shift_id"),
                rs.getString("shift_date"),
                rs.getString("shift_type"),
                getNullableInt(rs, "branch_id")));

        shift.setAssignments(loadAssignments(connection, rs.getInt("shift_id"), shift));
        return shift;
    }

    private List<ShiftAssignment> loadAssignments(Connection connection, int shiftId, Shift shift)
            throws SQLException, RepositoryException {
        List<ShiftAssignment> assignments = new ArrayList<>();
        EmployeeRepositoryImpl employeeRepository = new EmployeeRepositoryImpl(new EmployeeDAOImpl(connection));

        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT employee_id, role_name, status
                FROM shift_assignments
                WHERE shift_id = ?
                """)) {
            statement.setInt(1, shiftId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Optional<Employee> employee = employeeRepository.findById(rs.getString("employee_id"));
                    if (employee.isEmpty()) {
                        continue;
                    }

                    ShiftAssignment assignment = new ShiftAssignment(
                            employee.get(),
                            shift,
                            Role.valueOf(rs.getString("role_name")));
                    assignment.setApproved("APPROVED".equalsIgnoreCase(rs.getString("status")));
                    assignments.add(assignment);
                }
            }
        }

        return assignments;
    }

    private Integer getBranchId(Shift shift) {
        if (shift.getBranch() == null || shift.getBranch().getBranchId() == null) {
            return null;
        }

        try {
            return Integer.parseInt(shift.getBranch().getBranchId());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getNullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private String shiftKey(LocalDate date, ShiftType shiftType) {
        return date + "|" + shiftType;
    }
}
