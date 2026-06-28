package dataaccess.dao;

import dataaccess.dto.BranchDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchDAOImpl implements DaoInterface<BranchDto> {
    private final Connection connection;

    public BranchDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(BranchDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO branches (branch_id, branch_name, address) VALUES (?, ?, ?) ON CONFLICT(branch_id) DO UPDATE SET branch_name = excluded.branch_name, address = excluded.address
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getBranchId());
            stmt.setString(2, dto.getBranchName());
            stmt.setString(3, dto.getAddress());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save branches row", e);
        }
    }

    @Override
    public BranchDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT branch_id, branch_name, address
                FROM branches
                WHERE branch_id = ?
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
            throw new RepositoryException("Failed to find branches row", e);
        }
    }

    @Override
    public void update(BranchDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM branches WHERE branch_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete branches row", e);
        }
    }

    @Override
    public List<BranchDto> findAll() throws RepositoryException {
        String sql = """
                SELECT branch_id, branch_name, address
                FROM branches
                """;
        List<BranchDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load branches rows", e);
        }
    }

    private BranchDto mapRow(ResultSet rs) throws SQLException {
        return new BranchDto(
                rs.getInt("branch_id"),
                rs.getString("branch_name"),
                rs.getString("address")
        );
    }
}
