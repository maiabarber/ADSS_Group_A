package dataaccess.dao;

import dataaccess.dto.ShiftAssignmentDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShiftAssignmentDaoImpl implements DaoInterface<ShiftAssignmentDto> {
    private final Connection connection;

    public ShiftAssignmentDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(ShiftAssignmentDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO shift_assignments (assignment_id, shift_id, employee_id, role_name, status) VALUES (?, ?, ?, ?, ?) ON CONFLICT(assignment_id) DO UPDATE SET shift_id = excluded.shift_id, employee_id = excluded.employee_id, role_name = excluded.role_name, status = excluded.status
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getAssignmentId());
            stmt.setInt(2, dto.getShiftId());
            stmt.setString(3, dto.getEmployeeId());
            stmt.setString(4, dto.getRoleName());
            stmt.setString(5, dto.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save shift_assignments row", e);
        }
    }

    @Override
    public ShiftAssignmentDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT assignment_id, shift_id, employee_id, role_name, status
                FROM shift_assignments
                WHERE assignment_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shift_assignments row", e);
        }
    }

    @Override
    public void update(ShiftAssignmentDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM shift_assignments WHERE assignment_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete shift_assignments row", e);
        }
    }

    @Override
    public List<ShiftAssignmentDto> findAll() throws RepositoryException {
        String sql = """
                SELECT assignment_id, shift_id, employee_id, role_name, status
                FROM shift_assignments
                """;
        List<ShiftAssignmentDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load shift_assignments rows", e);
        }
    }

    private ShiftAssignmentDto mapRow(ResultSet rs) throws SQLException {
        return new ShiftAssignmentDto(
                rs.getInt("assignment_id"),
                rs.getInt("shift_id"),
                rs.getString("employee_id"),
                rs.getString("role_name"),
                rs.getString("status")
        );
    }
}
