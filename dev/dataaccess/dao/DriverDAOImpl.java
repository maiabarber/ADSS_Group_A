package dataaccess.dao;

import dataaccess.dto.DriverDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverDAOImpl implements DaoInterface<DriverDto> {
    private final Connection connection;

    public DriverDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DriverDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO drivers (employee_id, driver_name) VALUES (?, ?) ON CONFLICT(employee_id) DO UPDATE SET driver_name = excluded.driver_name
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getEmployeeId());
            stmt.setString(2, dto.getDriverName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save drivers row", e);
        }
    }

    @Override
    public DriverDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT employee_id, driver_name
                FROM drivers
                WHERE employee_id = ?
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
            throw new RepositoryException("Failed to find drivers row", e);
        }
    }

    @Override
    public void update(DriverDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM drivers WHERE employee_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setString(1, parts[0]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete drivers row", e);
        }
    }

    @Override
    public List<DriverDto> findAll() throws RepositoryException {
        String sql = """
                SELECT employee_id, driver_name
                FROM drivers
                """;
        List<DriverDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load drivers rows", e);
        }
    }

    private DriverDto mapRow(ResultSet rs) throws SQLException {
        return new DriverDto(
                rs.getString("employee_id"),
                rs.getString("driver_name")
        );
    }
}
