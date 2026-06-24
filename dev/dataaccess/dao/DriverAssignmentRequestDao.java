package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.DriverAssignmentRequestDto;

import employee.domain.ShiftType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

public class DriverAssignmentRequestDao {

    public List<DriverAssignmentRequestDto> findAll() throws SQLException {
        String sql = """
                SELECT driver_id, delivery_id, delivery_date_time, shift_type, handled, status_message
                FROM driverassignmentrequests
                ORDER BY request_id
                """;

        List<DriverAssignmentRequestDto> requests = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                requests.add(new DriverAssignmentRequestDto(
                        resultSet.getString("driver_id"),
                        resultSet.getInt("delivery_id"),
                        LocalDateTime.parse(resultSet.getString("delivery_date_time")),
                        ShiftType.valueOf(resultSet.getString("shift_type")),
                        resultSet.getInt("handled") == 1,
                        resultSet.getString("status_message")
                ));
            }
        }

        return requests;
    }

    public Optional<DriverAssignmentRequestDto> findByRequestId(int requestId) throws SQLException {
        String sql = """
                SELECT driver_id, delivery_id, delivery_date_time, shift_type, handled, status_message
                FROM driverassignmentrequests
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new DriverAssignmentRequestDto(
                            resultSet.getString("driver_id"),
                            resultSet.getInt("delivery_id"),
                            LocalDateTime.parse(resultSet.getString("delivery_date_time")),
                            ShiftType.valueOf(resultSet.getString("shift_type")),
                            resultSet.getInt("handled") == 1,
                            resultSet.getString("status_message")
                    ));
                }
            }
        }

        return Optional.empty();
    }

    public void insertRequest(String driverId,
                              int deliveryId,
                              LocalDateTime deliveryDateTime,
                              ShiftType shiftType,
                              boolean handled,
                              String statusMessage) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO driverassignmentrequests (
                    driver_id,
                    delivery_id,
                    delivery_date_time,
                    shift_type,
                    handled,
                    status_message
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, driverId);
            statement.setInt(2, deliveryId);
            statement.setString(3, deliveryDateTime == null ? null : deliveryDateTime.toString());
            statement.setString(4, shiftType == null ? null : shiftType.name());
            statement.setInt(5, handled ? 1 : 0);
            statement.setString(6, statusMessage);
            statement.executeUpdate();
        }
    }

    public void deleteByRequestId(int requestId) throws SQLException {
        String sql = "DELETE FROM driverassignmentrequests WHERE request_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.executeUpdate();
        }
    }
}