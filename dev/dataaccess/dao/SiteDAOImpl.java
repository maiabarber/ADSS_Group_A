package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.BranchDto;
import dataaccess.dto.SiteDto;
import transportation.domain.SiteType;
import dataaccess.dto.ShippingZoneDto;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SiteDAOImpl implements DaoInterface<SiteDto> {

    private final Connection connection;

    public SiteDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(SiteDto site) {
        String sql = """
                INSERT OR REPLACE INTO sites (
                    site_name,
                    address,
                    contact_name,
                    phone_number,
                    zone_code,
                    site_type,
                    branch_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, site.getSiteName());
            stmt.setString(2, site.getAddress());
            stmt.setString(3, site.getContactName());
            stmt.setString(4, site.getPhoneNumber());
            stmt.setString(5, site.getShippingZone().getZoneCode());
            stmt.setString(6, site.getSiteType().name());

            if (site.getBranch() != null) {
                stmt.setInt(7, Integer.parseInt(site.getBranch().getBranchId()));
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SiteDto findbyId(String id) {
        String sql = """
                SELECT s.site_name,
                       s.address,
                       s.contact_name,
                       s.phone_number,
                       s.zone_code,
                       s.site_type,
                       z.zone_name,
                       b.branch_id,
                       b.branch_name,
                       b.address AS branch_address
                FROM sites s
                JOIN shipping_zones z ON s.zone_code = z.zone_code
                LEFT JOIN branches b ON s.branch_id = b.branch_id
                WHERE s.site_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSite(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void update(SiteDto site) {
        String sql = """
                UPDATE sites SET
                    address = ?,
                    contact_name = ?,
                    phone_number = ?,
                    zone_code = ?,
                    site_type = ?,
                    branch_id = ?
                WHERE site_name = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, site.getAddress());
            stmt.setString(2, site.getContactName());
            stmt.setString(3, site.getPhoneNumber());
            stmt.setString(4, site.getShippingZone().getZoneCode());
            stmt.setString(5, site.getSiteType().name());

            if (site.getBranch() != null) {
                stmt.setInt(6, Integer.parseInt(site.getBranch().getBranchId()));
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setString(7, site.getSiteName());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM sites WHERE site_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SiteDto> findAll() {
        String sql = """
                SELECT s.site_name,
                       s.address,
                       s.contact_name,
                       s.phone_number,
                       s.zone_code,
                       s.site_type,
                       z.zone_name,
                       b.branch_id,
                       b.branch_name,
                       b.address AS branch_address
                FROM sites s
                JOIN shipping_zones z ON s.zone_code = z.zone_code
                LEFT JOIN branches b ON s.branch_id = b.branch_id
                ORDER BY s.site_id
                """;

        List<SiteDto> sites = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sites.add(mapResultSetToSite(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sites;
    }

    private SiteDto mapResultSetToSite(ResultSet rs) throws SQLException {
        ShippingZoneDto zone = new ShippingZoneDto(
                rs.getString("zone_code"),
                rs.getString("zone_name")
        );

        BranchDto branch = null;
        String branchId = rs.getString("branch_id");

        if (branchId != null) {
            branch = new BranchDto(
                    branchId,
                    rs.getString("branch_name"),
                    rs.getString("branch_address"),
                    null
            );
        }

        return new SiteDto(
                rs.getInt("site_id"),
                rs.getString("site_name"),
                rs.getString("address"),
                rs.getString("phone_number"),
                rs.getString("contact_name"),
                zone,
                parseSiteType(rs.getString("site_type")),
                branch
        );
    }

    private SiteType parseSiteType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return SiteType.REGULAR;
        }

        try {
            return SiteType.valueOf(rawValue);
        } catch (IllegalArgumentException e) {
            return SiteType.REGULAR;
        }
    }
}