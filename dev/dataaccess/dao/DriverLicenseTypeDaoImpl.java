package dataaccess.dao;

import dataaccess.dto.DriverLicenseTypeDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverLicenseTypeDaoImpl implements DaoInterface<DriverLicenseTypeDto> {
    private final Connection connection;

    public DriverLicenseTypeDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DriverLicenseTypeDto dto) throws RepositoryException {
        String sql = """
                INSERT OR REPLACE INTO driver_license_types (employee_id, license_type) VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getEmployeeId());
            stmt.setString(2, dto.getLicenseType());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save driver_license_types row", e);
        }
    }

    @Override
    public DriverLicenseTypeDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT employee_id, license_type
                FROM driver_license_types
                WHERE employee_id = ? AND license_type = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 2);
            if (parts.length != 2) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1]);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find driver_license_types row", e);
        }
    }

    @Override
    public void update(DriverLicenseTypeDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM driver_license_types WHERE employee_id = ? AND license_type = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 2);
            if (parts.length != 2) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete driver_license_types row", e);
        }
    }

    @Override
    public List<DriverLicenseTypeDto> findAll() throws RepositoryException {
        String sql = """
                SELECT employee_id, license_type
                FROM driver_license_types
                """;
        List<DriverLicenseTypeDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load driver_license_types rows", e);
        }
    }

    private DriverLicenseTypeDto mapRow(ResultSet rs) throws SQLException {
        return new DriverLicenseTypeDto(
                rs.getString("employee_id"),
                rs.getString("license_type")
        );
    }
}
