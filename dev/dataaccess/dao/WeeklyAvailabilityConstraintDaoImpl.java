package dataaccess.dao;

import dataaccess.dto.WeeklyAvailabilityConstraintDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WeeklyAvailabilityConstraintDaoImpl implements DaoInterface<WeeklyAvailabilityConstraintDto> {
    private final Connection connection;

    public WeeklyAvailabilityConstraintDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(WeeklyAvailabilityConstraintDto dto) throws RepositoryException {
        String sql = """
                INSERT OR REPLACE INTO weekly_availability_constraints (request_id, day_of_week, shift_type) VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getRequestId());
            stmt.setString(2, dto.getDayOfWeek());
            stmt.setString(3, dto.getShiftType());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save weekly_availability_constraints row", e);
        }
    }

    @Override
    public WeeklyAvailabilityConstraintDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT request_id, day_of_week, shift_type
                FROM weekly_availability_constraints
                WHERE request_id = ? AND day_of_week = ? AND shift_type = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 3);
            if (parts.length != 3) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.setString(2, parts[1]);
            stmt.setString(3, parts[2]);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find weekly_availability_constraints row", e);
        }
    }

    @Override
    public void update(WeeklyAvailabilityConstraintDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM weekly_availability_constraints WHERE request_id = ? AND day_of_week = ? AND shift_type = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 3);
            if (parts.length != 3) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.setString(2, parts[1]);
            stmt.setString(3, parts[2]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete weekly_availability_constraints row", e);
        }
    }

    @Override
    public List<WeeklyAvailabilityConstraintDto> findAll() throws RepositoryException {
        String sql = """
                SELECT request_id, day_of_week, shift_type
                FROM weekly_availability_constraints
                """;
        List<WeeklyAvailabilityConstraintDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load weekly_availability_constraints rows", e);
        }
    }

    private WeeklyAvailabilityConstraintDto mapRow(ResultSet rs) throws SQLException {
        return new WeeklyAvailabilityConstraintDto(
                rs.getInt("request_id"),
                rs.getString("day_of_week"),
                rs.getString("shift_type")
        );
    }
}
