package dataaccess.dao;

import dataaccess.dto.BranchDeliveryStopSiteDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchDeliveryStopSiteDaoImpl implements DaoInterface<BranchDeliveryStopSiteDto> {
    private final Connection connection;

    public BranchDeliveryStopSiteDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(BranchDeliveryStopSiteDto dto) throws RepositoryException {
        String sql = """
                INSERT OR REPLACE INTO branch_delivery_stop_sites (branch_id, site_id) VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getBranchId());
            stmt.setInt(2, dto.getSiteId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save branch_delivery_stop_sites row", e);
        }
    }

    @Override
    public BranchDeliveryStopSiteDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT branch_id, site_id
                FROM branch_delivery_stop_sites
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
            throw new RepositoryException("Failed to find branch_delivery_stop_sites row", e);
        }
    }

    @Override
    public void update(BranchDeliveryStopSiteDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM branch_delivery_stop_sites WHERE branch_id = ? AND site_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 2);
            if (parts.length != 2) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.setInt(2, Integer.parseInt(parts[1]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete branch_delivery_stop_sites row", e);
        }
    }

    @Override
    public List<BranchDeliveryStopSiteDto> findAll() throws RepositoryException {
        String sql = """
                SELECT branch_id, site_id
                FROM branch_delivery_stop_sites
                """;
        List<BranchDeliveryStopSiteDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load branch_delivery_stop_sites rows", e);
        }
    }

    private BranchDeliveryStopSiteDto mapRow(ResultSet rs) throws SQLException {
        return new BranchDeliveryStopSiteDto(
                rs.getInt("branch_id"),
                rs.getInt("site_id")
        );
    }
}
