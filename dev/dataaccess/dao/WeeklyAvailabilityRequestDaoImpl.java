package dataaccess.dao;

import dataaccess.dto.WeeklyAvailabilityRequestDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WeeklyAvailabilityRequestDaoImpl implements DaoInterface<WeeklyAvailabilityRequestDto> {
    private final Connection connection;

    public WeeklyAvailabilityRequestDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(WeeklyAvailabilityRequestDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO weeklyavailabilityrequests (request_id, employee_id, week_start_date, submission_deadline) VALUES (?, ?, ?, ?) ON CONFLICT(request_id) DO UPDATE SET employee_id = excluded.employee_id, week_start_date = excluded.week_start_date, submission_deadline = excluded.submission_deadline
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getRequestId());
            stmt.setString(2, dto.getEmployeeId());
            stmt.setString(3, dto.getWeekStartDate());
            stmt.setString(4, dto.getSubmissionDeadline());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save weeklyavailabilityrequests row", e);
        }
    }

    @Override
    public WeeklyAvailabilityRequestDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT request_id, employee_id, week_start_date, submission_deadline
                FROM weeklyavailabilityrequests
                WHERE request_id = ?
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
            throw new RepositoryException("Failed to find weeklyavailabilityrequests row", e);
        }
    }

    @Override
    public void update(WeeklyAvailabilityRequestDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM weeklyavailabilityrequests WHERE request_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete weeklyavailabilityrequests row", e);
        }
    }

    @Override
    public List<WeeklyAvailabilityRequestDto> findAll() throws RepositoryException {
        String sql = """
                SELECT request_id, employee_id, week_start_date, submission_deadline
                FROM weeklyavailabilityrequests
                """;
        List<WeeklyAvailabilityRequestDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load weeklyavailabilityrequests rows", e);
        }
    }

    private WeeklyAvailabilityRequestDto mapRow(ResultSet rs) throws SQLException {
        return new WeeklyAvailabilityRequestDto(
                rs.getInt("request_id"),
                rs.getString("employee_id"),
                rs.getString("week_start_date"),
                rs.getString("submission_deadline")
        );
    }
}
