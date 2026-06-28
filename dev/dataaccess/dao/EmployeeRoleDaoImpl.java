package dataaccess.dao;

import dataaccess.dto.EmployeeRoleDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRoleDaoImpl implements DaoInterface<EmployeeRoleDto> {
    private final Connection connection;

    public EmployeeRoleDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(EmployeeRoleDto dto) throws RepositoryException {
        String sql = """
                INSERT OR REPLACE INTO employee_roles (employee_id, role_name) VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getEmployeeId());
            stmt.setString(2, dto.getRoleName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save employee_roles row", e);
        }
    }

    @Override
    public EmployeeRoleDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT employee_id, role_name
                FROM employee_roles
                WHERE employee_id = ? AND role_name = ?
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
            throw new RepositoryException("Failed to find employee_roles row", e);
        }
    }

    @Override
    public void update(EmployeeRoleDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM employee_roles WHERE employee_id = ? AND role_name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 2);
            if (parts.length != 2) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete employee_roles row", e);
        }
    }

    @Override
    public List<EmployeeRoleDto> findAll() throws RepositoryException {
        String sql = """
                SELECT employee_id, role_name
                FROM employee_roles
                """;
        List<EmployeeRoleDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load employee_roles rows", e);
        }
    }

    private EmployeeRoleDto mapRow(ResultSet rs) throws SQLException {
        return new EmployeeRoleDto(
                rs.getString("employee_id"),
                rs.getString("role_name")
        );
    }
}
