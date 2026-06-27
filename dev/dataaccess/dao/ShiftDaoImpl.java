package dataaccess.dao;

import dataaccess.dto.BranchDto;
import dataaccess.dto.ShiftAssignmentDto;
import dataaccess.dto.ShiftDto;
import dataaccess.repository.RepositoryException;
import employee.domain.Role;
import employee.domain.ShiftType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class ShiftDaoImpl implements DaoInterface<ShiftDto> {

    private final Connection connection;

    public ShiftDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(ShiftDto shift) throws RepositoryException {
        int shiftId = findShiftId(shift);

        if (shiftId == -1) {
            shiftId = insertShift(shift);
        } else {
            updateShiftById(shiftId, shift);
        }

        deleteAssignments(shiftId);
        insertAssignments(shiftId, shift);
    }

    private int findShiftId(ShiftDto shift) throws RepositoryException {
        String sql = """
                SELECT shift_id
                FROM shifts
                WHERE shift_date = ?
                  AND shift_type = ?
                  AND branch_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, shift.getDate().toString());
            stmt.setString(2, shift.getShiftType().name());
            stmt.setInt(3, Integer.parseInt(shift.getBranch().getBranchId()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shift_id");
                }
            }

            return -1;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shift id", e);
        }
    }

    private int insertShift(ShiftDto shift) throws RepositoryException {
        String sql = """
                INSERT INTO shifts (
                    shift_date,
                    shift_type,
                    branch_id
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, shift.getDate().toString());
            stmt.setString(2, shift.getShiftType().name());
            stmt.setInt(3, Integer.parseInt(shift.getBranch().getBranchId()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new RepositoryException("Failed to create shift id");

        } catch (SQLException e) {
            throw new RepositoryException("Failed to insert shift", e);
        }
    }

    private void updateShiftById(int shiftId, ShiftDto shift) throws RepositoryException {
        String sql = """
                UPDATE shifts
                SET shift_date = ?,
                    shift_type = ?,
                    branch_id = ?
                WHERE shift_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, shift.getDate().toString());
            stmt.setString(2, shift.getShiftType().name());
            stmt.setInt(3, Integer.parseInt(shift.getBranch().getBranchId()));
            stmt.setInt(4, shiftId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to update shift", e);
        }
    }

    private void deleteAssignments(int shiftId) throws RepositoryException {
        String sql = "DELETE FROM shift_assignments WHERE shift_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, shiftId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete shift assignments", e);
        }
    }

    private void insertAssignments(int shiftId, ShiftDto shift) throws RepositoryException {
        String sql = """
                INSERT INTO shift_assignments (
                    shift_id,
                    employee_id,
                    role_name,
                    status
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (ShiftAssignmentDto assignment : shift.getAssignments()) {
                stmt.setInt(1, shiftId);
                stmt.setString(2, assignment.getEmployeeId());
                stmt.setString(3, assignment.getRole().name());
                stmt.setString(4, assignment.isApproved() ? "APPROVED" : "PENDING");
                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to insert shift assignments", e);
        }
    }

    @Override
    public ShiftDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT s.*, b.branch_name, b.address, b.delivery_stop_site_id
                FROM shifts s
                LEFT JOIN branches b ON s.branch_id = b.branch_id
                WHERE s.shift_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int shiftId = rs.getInt("shift_id");

                BranchDto branch = new BranchDto(
                        rs.getString("branch_id"),
                        rs.getString("branch_name"),
                        rs.getString("address"),
                        null
                );

                return new ShiftDto(
                        LocalDate.parse(rs.getString("shift_date")),
                        ShiftType.valueOf(rs.getString("shift_type")),
                        null,
                        branch,
                        new EnumMap<>(Role.class),
                        findAssignments(shiftId),
                        false,
                        null
                );
            }

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shift " + id, e);
        }
    }

    private List<ShiftAssignmentDto> findAssignments(int shiftId) throws RepositoryException {
    String sql = """
            SELECT
                sa.employee_id,
                sa.role_name,
                sa.status,
                s.shift_date,
                s.shift_type
            FROM shift_assignments sa
            JOIN shifts s ON sa.shift_id = s.shift_id
            WHERE sa.shift_id = ?
            ORDER BY sa.assignment_id
            """;

    List<ShiftAssignmentDto> assignments = new ArrayList<>();

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, shiftId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                boolean approved = "APPROVED".equals(rs.getString("status"));

                assignments.add(new ShiftAssignmentDto(
                        rs.getString("employee_id"),
                        LocalDate.parse(rs.getString("shift_date")),
                        ShiftType.valueOf(rs.getString("shift_type")),
                        Role.valueOf(rs.getString("role_name")),
                        approved,
                        !approved,
                        false,
                        false
                ));
            }
        }

        return assignments;

    } catch (SQLException e) {
        throw new RepositoryException("Failed to load shift assignments", e);
    }
}

    @Override
    public void update(ShiftDto shift) throws RepositoryException {
        createOrUpdate(shift);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        int shiftId = Integer.parseInt(id);

        try {
            deleteAssignments(shiftId);

            try (PreparedStatement stmt =
                         connection.prepareStatement("DELETE FROM shifts WHERE shift_id = ?")) {
                stmt.setInt(1, shiftId);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete shift " + id, e);
        }
    }

    @Override
    public List<ShiftDto> findAll() throws RepositoryException {
        List<ShiftDto> shifts = new ArrayList<>();

        String sql = "SELECT shift_id FROM shifts ORDER BY shift_date, shift_type";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ShiftDto shift = findbyId(String.valueOf(rs.getInt("shift_id")));
                if (shift != null) {
                    shifts.add(shift);
                }
            }

            return shifts;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to load shifts", e);
        }
    }
}