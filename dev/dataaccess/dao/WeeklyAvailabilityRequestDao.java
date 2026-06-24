package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.ConstraintDto;
import dataaccess.dto.PreferenceDto;
import dataaccess.dto.WeeklyAvailabilityRequestDto;

import employee.domain.ShiftType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

public class WeeklyAvailabilityRequestDao {

    public List<WeeklyAvailabilityRequestDto> findAll() throws SQLException {
        String sql = """
                SELECT week_start_date, submission_deadline
                FROM weeklyavailabilityrequests
                ORDER BY request_id
                """;

        List<WeeklyAvailabilityRequestDto> requests = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                requests.add(new WeeklyAvailabilityRequestDto(
                        LocalDate.parse(resultSet.getString("week_start_date")),
                        List.of(new ConstraintDto(DayOfWeek.MONDAY, ShiftType.MORNING)),
                        List.of(new PreferenceDto(DayOfWeek.MONDAY, ShiftType.MORNING))
                ));
            }
        }

        return requests;
    }

    public Optional<WeeklyAvailabilityRequestDto> findByRequestId(int requestId) throws SQLException {
        String sql = """
                SELECT week_start_date, submission_deadline
                FROM weeklyavailabilityrequests
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new WeeklyAvailabilityRequestDto(
                            LocalDate.parse(resultSet.getString("week_start_date")),
                            List.of(new ConstraintDto(DayOfWeek.MONDAY, ShiftType.MORNING)),
                            List.of(new PreferenceDto(DayOfWeek.MONDAY, ShiftType.MORNING))
                    ));
                }
            }
        }

        return Optional.empty();
    }

    public void insertRequest(String employeeId, LocalDate weekStartDate, LocalDate submissionDeadline) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO weeklyavailabilityrequests (
                    employee_id,
                    week_start_date,
                    submission_deadline
                )
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employeeId);
            statement.setString(2, weekStartDate == null ? null : weekStartDate.toString());
            statement.setString(3, submissionDeadline == null ? null : submissionDeadline.toString());
            statement.executeUpdate();
        }
    }

    public void deleteByRequestId(int requestId) throws SQLException {
        String sql = "DELETE FROM weeklyavailabilityrequests WHERE request_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.executeUpdate();
        }
    }
}