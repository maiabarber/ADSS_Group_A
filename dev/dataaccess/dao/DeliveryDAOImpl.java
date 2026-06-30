package dataaccess.dao;

import dataaccess.dto.DeliveryDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryDAOImpl implements DaoInterface<DeliveryDto> {
    private final Connection connection;

    public DeliveryDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DeliveryDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO deliveries (delivery_id, delivery_date, source_site_id, departure_time, final_measured_weight, truck_license_number, driver_employee_id, zone_code, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(delivery_id) DO UPDATE SET delivery_date = excluded.delivery_date, source_site_id = excluded.source_site_id, departure_time = excluded.departure_time, final_measured_weight = excluded.final_measured_weight, truck_license_number = excluded.truck_license_number, driver_employee_id = excluded.driver_employee_id, zone_code = excluded.zone_code, status = excluded.status
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getDeliveryId());
            stmt.setString(2, dto.getDeliveryDate());
            stmt.setInt(3, dto.getSourceSiteId());
            stmt.setString(4, dto.getDepartureTime());
            stmt.setDouble(5, dto.getFinalMeasuredWeight());
            stmt.setString(6, dto.getTruckLicenseNumber());
            stmt.setString(7, dto.getDriverEmployeeId());
            stmt.setString(8, dto.getZoneCode());
            stmt.setString(9, dto.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save deliveries row", e);
        }
    }

    @Override
    public DeliveryDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT delivery_id, delivery_date, source_site_id, departure_time, final_measured_weight, truck_license_number, driver_employee_id, zone_code, status
                FROM deliveries
                WHERE delivery_id = ?
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
            throw new RepositoryException("Failed to find deliveries row", e);
        }
    }

    @Override
    public void update(DeliveryDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM deliveries WHERE delivery_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete deliveries row", e);
        }
    }

    @Override
    public List<DeliveryDto> findAll() throws RepositoryException {
        String sql = """
                SELECT delivery_id, delivery_date, source_site_id, departure_time, final_measured_weight, truck_license_number, driver_employee_id, zone_code, status
                FROM deliveries
                """;
        List<DeliveryDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load deliveries rows", e);
        }
    }

    private DeliveryDto mapRow(ResultSet rs) throws SQLException {
        return new DeliveryDto(
                rs.getInt("delivery_id"),
                rs.getString("delivery_date"),
                rs.getInt("source_site_id"),
                rs.getString("departure_time"),
                rs.getDouble("final_measured_weight"),
                rs.getString("truck_license_number"),
                rs.getString("driver_employee_id"),
                rs.getString("zone_code"),
                rs.getString("status")
        );
    }
}
