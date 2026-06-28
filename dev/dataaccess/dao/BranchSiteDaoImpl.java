package dataaccess.dao;

import dataaccess.dto.BranchSiteDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchSiteDaoImpl implements DaoInterface<BranchSiteDto> {
    private final Connection connection;

    public BranchSiteDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(BranchSiteDto dto) throws RepositoryException {
        String sql = """
                INSERT OR REPLACE INTO branch_sites (branch_id, site_id) VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getBranchId());
            stmt.setInt(2, dto.getSiteId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save branch_sites row", e);
        }
    }

    @Override
    public BranchSiteDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT branch_id, site_id
                FROM branch_sites
                WHERE branch_id = ? AND site_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 2);
            if (parts.length != 2) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.setInt(2, Integer.parseInt(parts[1]));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find branch_sites row", e);
        }
    }

    @Override
    public void update(BranchSiteDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM branch_sites WHERE branch_id = ? AND site_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 2);
            if (parts.length != 2) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.setInt(2, Integer.parseInt(parts[1]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete branch_sites row", e);
        }
    }

    @Override
    public List<BranchSiteDto> findAll() throws RepositoryException {
        String sql = """
                SELECT branch_id, site_id
                FROM branch_sites
                """;
        List<BranchSiteDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load branch_sites rows", e);
        }
    }

    private BranchSiteDto mapRow(ResultSet rs) throws SQLException {
        return new BranchSiteDto(
                rs.getInt("branch_id"),
                rs.getInt("site_id")
        );
    }
}
