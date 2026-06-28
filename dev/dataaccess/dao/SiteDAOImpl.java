package dataaccess.dao;

import dataaccess.dto.SiteDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SiteDAOImpl implements DaoInterface<SiteDto> {
    private final Connection connection;

    public SiteDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(SiteDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO sites (site_id, site_name, address, contact_name, phone_number, zone_code, site_type) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT(site_id) DO UPDATE SET site_name = excluded.site_name, address = excluded.address, contact_name = excluded.contact_name, phone_number = excluded.phone_number, zone_code = excluded.zone_code, site_type = excluded.site_type
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getSiteId());
            stmt.setString(2, dto.getSiteName());
            stmt.setString(3, dto.getAddress());
            stmt.setString(4, dto.getContactName());
            stmt.setString(5, dto.getPhoneNumber());
            stmt.setString(6, dto.getZoneCode());
            stmt.setString(7, dto.getSiteType());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save sites row", e);
        }
    }

    @Override
    public SiteDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT site_id, site_name, address, contact_name, phone_number, zone_code, site_type
                FROM sites
                WHERE site_id = ?
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
            throw new RepositoryException("Failed to find sites row", e);
        }
    }

    @Override
    public void update(SiteDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM sites WHERE site_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete sites row", e);
        }
    }

    @Override
    public List<SiteDto> findAll() throws RepositoryException {
        String sql = """
                SELECT site_id, site_name, address, contact_name, phone_number, zone_code, site_type
                FROM sites
                """;
        List<SiteDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load sites rows", e);
        }
    }

    private SiteDto mapRow(ResultSet rs) throws SQLException {
        return new SiteDto(
                rs.getInt("site_id"),
                rs.getString("site_name"),
                rs.getString("address"),
                rs.getString("contact_name"),
                rs.getString("phone_number"),
                rs.getString("zone_code"),
                rs.getString("site_type")
        );
    }
}
