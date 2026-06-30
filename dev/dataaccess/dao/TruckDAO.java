package dataaccess.dao;

import dataaccess.dto.TruckDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TruckDAO implements DaoInterface<TruckDto> {
    private final Connection connection;

    public TruckDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(TruckDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO trucks (license_number, model, net_weight, max_allowed_weight, required_license_type) VALUES (?, ?, ?, ?, ?) ON CONFLICT(license_number) DO UPDATE SET model = excluded.model, net_weight = excluded.net_weight, max_allowed_weight = excluded.max_allowed_weight, required_license_type = excluded.required_license_type
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getLicenseNumber());
            stmt.setString(2, dto.getModel());
            stmt.setDouble(3, dto.getNetWeight());
            stmt.setDouble(4, dto.getMaxAllowedWeight());
            stmt.setString(5, dto.getRequiredLicenseType());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save trucks row", e);
        }
    }

    @Override
    public TruckDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT license_number, model, net_weight, max_allowed_weight, required_license_type
                FROM trucks
                WHERE license_number = ?
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
            throw new RepositoryException("Failed to find trucks row", e);
        }
    }

    @Override
    public void update(TruckDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM trucks WHERE license_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setString(1, parts[0]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete trucks row", e);
        }
    }

    @Override
    public List<TruckDto> findAll() throws RepositoryException {
        String sql = """
                SELECT license_number, model, net_weight, max_allowed_weight, required_license_type
                FROM trucks
                """;
        List<TruckDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load trucks rows", e);
        }
    }

    private TruckDto mapRow(ResultSet rs) throws SQLException {
        return new TruckDto(
                rs.getString("license_number"),
                rs.getString("model"),
                rs.getDouble("net_weight"),
                rs.getDouble("max_allowed_weight"),
                rs.getString("required_license_type")
        );
    }
}
