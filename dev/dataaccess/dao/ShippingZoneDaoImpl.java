package dataaccess.dao;

import dataaccess.dto.ShippingZoneDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShippingZoneDaoImpl implements DaoInterface<ShippingZoneDto> {
    private final Connection connection;

    public ShippingZoneDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(ShippingZoneDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO shipping_zones (zone_code, zone_name) VALUES (?, ?) ON CONFLICT(zone_code) DO UPDATE SET zone_name = excluded.zone_name
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getZoneCode());
            stmt.setString(2, dto.getZoneName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save shipping_zones row", e);
        }
    }

    @Override
    public ShippingZoneDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT zone_code, zone_name
                FROM shipping_zones
                WHERE zone_code = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setString(1, parts[0]);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shipping_zones row", e);
        }
    }

    @Override
    public void update(ShippingZoneDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM shipping_zones WHERE zone_code = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setString(1, parts[0]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete shipping_zones row", e);
        }
    }

    @Override
    public List<ShippingZoneDto> findAll() throws RepositoryException {
        String sql = """
                SELECT zone_code, zone_name
                FROM shipping_zones
                """;
        List<ShippingZoneDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load shipping_zones rows", e);
        }
    }

    private ShippingZoneDto mapRow(ResultSet rs) throws SQLException {
        return new ShippingZoneDto(
                rs.getString("zone_code"),
                rs.getString("zone_name")
        );
    }
}
