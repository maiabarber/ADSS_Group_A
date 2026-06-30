package dataaccess.dao;

import dataaccess.dto.DeliveryFormMeasurementDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryFormMeasurementDaoImpl implements DaoInterface<DeliveryFormMeasurementDto> {
    private final Connection connection;

    public DeliveryFormMeasurementDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DeliveryFormMeasurementDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO delivery_form_measurements (measurement_id, delivery_id, measured_weight) VALUES (?, ?, ?) ON CONFLICT(measurement_id) DO UPDATE SET delivery_id = excluded.delivery_id, measured_weight = excluded.measured_weight
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getMeasurementId());
            stmt.setInt(2, dto.getDeliveryId());
            stmt.setDouble(3, dto.getMeasuredWeight());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save delivery_form_measurements row", e);
        }
    }

    @Override
    public DeliveryFormMeasurementDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT measurement_id, delivery_id, measured_weight
                FROM delivery_form_measurements
                WHERE measurement_id = ?
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
            throw new RepositoryException("Failed to find delivery_form_measurements row", e);
        }
    }

    @Override
    public void update(DeliveryFormMeasurementDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM delivery_form_measurements WHERE measurement_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete delivery_form_measurements row", e);
        }
    }

    @Override
    public List<DeliveryFormMeasurementDto> findAll() throws RepositoryException {
        String sql = """
                SELECT measurement_id, delivery_id, measured_weight
                FROM delivery_form_measurements
                """;
        List<DeliveryFormMeasurementDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load delivery_form_measurements rows", e);
        }
    }

    private DeliveryFormMeasurementDto mapRow(ResultSet rs) throws SQLException {
        return new DeliveryFormMeasurementDto(
                rs.getInt("measurement_id"),
                rs.getInt("delivery_id"),
                rs.getDouble("measured_weight")
        );
    }
}
