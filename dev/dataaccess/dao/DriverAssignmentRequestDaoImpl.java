package dataaccess.dao;

import dataaccess.dto.DriverAssignmentRequestDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverAssignmentRequestDaoImpl implements DaoInterface<DriverAssignmentRequestDto> {
    private final Connection connection;

    public DriverAssignmentRequestDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DriverAssignmentRequestDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO driverassignmentrequests (request_id, driver_id, delivery_id, delivery_date_time, shift_type, handled, status_message) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT(request_id) DO UPDATE SET driver_id = excluded.driver_id, delivery_id = excluded.delivery_id, delivery_date_time = excluded.delivery_date_time, shift_type = excluded.shift_type, handled = excluded.handled, status_message = excluded.status_message
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getRequestId());
            stmt.setString(2, dto.getDriverId());
            stmt.setInt(3, dto.getDeliveryId());
            stmt.setString(4, dto.getDeliveryDateTime());
            stmt.setString(5, dto.getShiftType());
            stmt.setInt(6, dto.isHandled() ? 1 : 0);
            stmt.setString(7, dto.getStatusMessage());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save driverassignmentrequests row", e);
        }
    }

    @Override
    public DriverAssignmentRequestDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT request_id, driver_id, delivery_id, delivery_date_time, shift_type, handled, status_message
                FROM driverassignmentrequests
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
            throw new RepositoryException("Failed to find driverassignmentrequests row", e);
        }
    }

    @Override
    public void update(DriverAssignmentRequestDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM driverassignmentrequests WHERE request_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete driverassignmentrequests row", e);
        }
    }

    @Override
    public List<DriverAssignmentRequestDto> findAll() throws RepositoryException {
        String sql = """
                SELECT request_id, driver_id, delivery_id, delivery_date_time, shift_type, handled, status_message
                FROM driverassignmentrequests
                """;
        List<DriverAssignmentRequestDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load driverassignmentrequests rows", e);
        }
    }

    private DriverAssignmentRequestDto mapRow(ResultSet rs) throws SQLException {
        return new DriverAssignmentRequestDto(
                rs.getInt("request_id"),
                rs.getString("driver_id"),
                rs.getInt("delivery_id"),
                rs.getString("delivery_date_time"),
                rs.getString("shift_type"),
                rs.getInt("handled") == 1,
                rs.getString("status_message")
        );
    }
}
