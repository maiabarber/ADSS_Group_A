package dataaccess.dao;

import dataaccess.dto.DriverAssignmentRequestDto;
import dataaccess.repository.RepositoryException;
import employee.domain.ShiftType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DriverAssignmentRequestDaoImpl implements DaoInterface<DriverAssignmentRequestDto> {
    private Connection connection;

    public DriverAssignmentRequestDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DriverAssignmentRequestDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO driverassignmentrequests (
                    driver_id,
                    delivery_id,
                    delivery_date_time,
                    shift_type,
                    handled,
                    status_message
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getDriverId());
            stmt.setInt(2, dto.getDeliveryId());
            stmt.setString(3, dto.getDeliveryDateTime().toString());
            stmt.setString(4, dto.getShiftType().name());
            stmt.setInt(5, dto.isHandled() ? 1 : 0);
            stmt.setString(6, dto.getStatusMessage());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save driver assignment request", e);
        }
    }

    private DriverAssignmentRequestDto mapRow(ResultSet rs) throws SQLException {
        return new DriverAssignmentRequestDto(
                rs.getString("driver_id"),
                rs.getInt("delivery_id"),
                LocalDateTime.parse(rs.getString("delivery_date_time")),
                ShiftType.valueOf(rs.getString("shift_type")),
                rs.getInt("handled") == 1,
                rs.getString("status_message")
        );
    }

    @Override
    public List<DriverAssignmentRequestDto> findAll() throws RepositoryException {
        String sql = "SELECT * FROM driverassignmentrequests";

        List<DriverAssignmentRequestDto> requests = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                requests.add(mapRow(rs));
            }

            return requests;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load driver assignment requests", e);
        }
    }

    

    @Override
    public DriverAssignmentRequestDto findbyId(String id) throws RepositoryException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findbyId'");
    }

    @Override
    public void update(DriverAssignmentRequestDto e) throws RepositoryException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(String id) throws RepositoryException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    
}