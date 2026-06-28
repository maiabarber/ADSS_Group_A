package dataaccess.dao;

import dataaccess.dto.WeeklyAvailabilityPreferenceDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WeeklyAvailabilityPreferenceDaoImpl implements DaoInterface<WeeklyAvailabilityPreferenceDto> {
    private final Connection connection;

    public WeeklyAvailabilityPreferenceDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(WeeklyAvailabilityPreferenceDto dto) throws RepositoryException {
        String sql = """
                INSERT OR REPLACE INTO weekly_availability_preferences (request_id, day_of_week, shift_type) VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getRequestId());
            stmt.setString(2, dto.getDayOfWeek());
            stmt.setString(3, dto.getShiftType());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save weekly_availability_preferences row", e);
        }
    }

    @Override
    public WeeklyAvailabilityPreferenceDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT request_id, day_of_week, shift_type
                FROM weekly_availability_preferences
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
            throw new RepositoryException("Failed to find weekly_availability_preferences row", e);
        }
    }

    @Override
    public void update(WeeklyAvailabilityPreferenceDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM weekly_availability_preferences WHERE request_id = ? AND day_of_week = ? AND shift_type = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 3);
            if (parts.length != 3) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.setString(2, parts[1]);
            stmt.setString(3, parts[2]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete weekly_availability_preferences row", e);
        }
    }

    @Override
    public List<WeeklyAvailabilityPreferenceDto> findAll() throws RepositoryException {
        String sql = """
                SELECT request_id, day_of_week, shift_type
                FROM weekly_availability_preferences
                """;
        List<WeeklyAvailabilityPreferenceDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load weekly_availability_preferences rows", e);
        }
    }

    private WeeklyAvailabilityPreferenceDto mapRow(ResultSet rs) throws SQLException {
        return new WeeklyAvailabilityPreferenceDto(
                rs.getInt("request_id"),
                rs.getString("day_of_week"),
                rs.getString("shift_type")
        );
    }
}
