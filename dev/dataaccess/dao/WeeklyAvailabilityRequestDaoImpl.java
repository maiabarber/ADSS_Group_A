package dataaccess.dao;

import dataaccess.DatabaseConnection;
import employee.domain.WeeklyAvailabilityRequest;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WeeklyAvailabilityRequestDaoImpl implements DaoInterface<WeeklyAvailabilityRequest> {

    @Override
    public void createOrUpdate(WeeklyAvailabilityRequest request) {
        String sql = """
                INSERT OR REPLACE INTO weeklyavailabilityrequests (
                    request_id,
                    employee_id,
                    week_start_date,
                    submission_deadline
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setNull(1, Types.INTEGER);
            stmt.setString(2, "UNKNOWN");
            stmt.setString(3, request.getWeekStartDate().toString());
            stmt.setString(4, request.getSubmissionDeadline().toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public WeeklyAvailabilityRequest findbyId(String id) {
        String sql = """
                SELECT week_start_date, submission_deadline
                FROM weeklyavailabilityrequests
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWeeklyAvailabilityRequest(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void update(WeeklyAvailabilityRequest request) {
        String sql = """
                UPDATE weeklyavailabilityrequests
                SET week_start_date = ?,
                    submission_deadline = ?
                WHERE week_start_date = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, request.getWeekStartDate().toString());
            stmt.setString(2, request.getSubmissionDeadline().toString());
            stmt.setString(3, request.getWeekStartDate().toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM weeklyavailabilityrequests WHERE request_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<WeeklyAvailabilityRequest> findAll() {
        String sql = """
                SELECT week_start_date, submission_deadline
                FROM weeklyavailabilityrequests
                ORDER BY request_id
                """;

        List<WeeklyAvailabilityRequest> requests = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                requests.add(mapResultSetToWeeklyAvailabilityRequest(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    private WeeklyAvailabilityRequest mapResultSetToWeeklyAvailabilityRequest(ResultSet rs) throws SQLException {
        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();

        request.setWeekStartDate(LocalDate.parse(rs.getString("week_start_date")));
        request.setSubmissionDeadline(LocalDate.parse(rs.getString("submission_deadline")));

        return request;
    }
}