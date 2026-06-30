package dataaccess.dao;

import dataaccess.dto.DeliveryStopDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryStopDaoImpl implements DaoInterface<DeliveryStopDto> {
    private final Connection connection;

    public DeliveryStopDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DeliveryStopDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO delivery_stops (stop_id, delivery_id, stop_order, stop_type, site_id, planned_arrival) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT(stop_id) DO UPDATE SET delivery_id = excluded.delivery_id, stop_order = excluded.stop_order, stop_type = excluded.stop_type, site_id = excluded.site_id, planned_arrival = excluded.planned_arrival
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getStopId());
            stmt.setInt(2, dto.getDeliveryId());
            stmt.setInt(3, dto.getStopOrder());
            stmt.setString(4, dto.getStopType());
            stmt.setInt(5, dto.getSiteId());
            stmt.setString(6, dto.getPlannedArrival());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save delivery_stops row", e);
        }
    }

    @Override
    public DeliveryStopDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT stop_id, delivery_id, stop_order, stop_type, site_id, planned_arrival
                FROM delivery_stops
                WHERE stop_id = ?
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
            throw new RepositoryException("Failed to find delivery_stops row", e);
        }
    }

    @Override
    public void update(DeliveryStopDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM delivery_stops WHERE stop_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete delivery_stops row", e);
        }
    }

    @Override
    public List<DeliveryStopDto> findAll() throws RepositoryException {
        String sql = """
                SELECT stop_id, delivery_id, stop_order, stop_type, site_id, planned_arrival
                FROM delivery_stops
                """;
        List<DeliveryStopDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load delivery_stops rows", e);
        }
    }

    private DeliveryStopDto mapRow(ResultSet rs) throws SQLException {
        return new DeliveryStopDto(
                rs.getInt("stop_id"),
                rs.getInt("delivery_id"),
                rs.getInt("stop_order"),
                rs.getString("stop_type"),
                rs.getInt("site_id"),
                rs.getString("planned_arrival")
        );
    }
}
