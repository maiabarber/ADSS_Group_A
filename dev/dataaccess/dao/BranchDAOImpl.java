package dataaccess.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dataaccess.dto.BranchDto;
import dataaccess.dto.SiteDto;
import dataaccess.mapper.SiteMapper;
import dataaccess.repository.RepositoryException;

public class BranchDAOImpl implements DaoInterface<BranchDto> {

    private final Connection connection;

    public BranchDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(BranchDto b) {
        String sql = """
                INSERT OR REPLACE INTO branches (
                    branch_id,
                    branch_name,
                    address,
                    delivery_stop_site_id
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(b.getBranchId()));
            pstmt.setString(2, b.getBranchName());
            pstmt.setString(3, b.getLocation());

            if (b.getSite() == null) {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(4, b.getSite().getId());
            }

            pstmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
public BranchDto findbyId(String id) throws RepositoryException {
    String sql = """
            SELECT branch_id, branch_name, address, delivery_stop_site_id
            FROM branches
            WHERE branch_id = ?
            """;

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, Integer.parseInt(id));

        try (ResultSet rs = pstmt.executeQuery()) {
            if (!rs.next()) {
                return null;
            }

            Integer siteId = rs.getObject("delivery_stop_site_id") == null
                    ? null
                    : rs.getInt("delivery_stop_site_id");

            SiteDto stopSite = null;

            if (siteId != null) {
                SiteDAOImpl siteDao = new SiteDAOImpl(connection);
                stopSite = siteDao.findbyId(String.valueOf(siteId));
            }

            return new BranchDto(
					rs.getString("branch_id"),
					rs.getString("branch_name"),
					rs.getString("address"),
					stopSite
			);
        }

    } catch (SQLException ex) {
        throw new RepositoryException("Failed to find branch " + id, ex);
    }
}
    @Override
    public void update(BranchDto b) {
        String sql = """
                UPDATE branches
                SET branch_name = ?,
                    address = ?,
                    delivery_stop_site_id = ?
                WHERE branch_id = ?
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, b.getBranchName());
            pstmt.setString(2, b.getLocation());

            if (b.getSite() == null) {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(3, b.getSite().getId());
            }

            pstmt.setInt(4, Integer.parseInt(b.getBranchId()));
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM branches WHERE branch_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(id));
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<BranchDto> findAll() {
        List<BranchDto> branches = new ArrayList<>();

        String sql = """
                SELECT
                    branch_id,
                    branch_name,
                    address,
                    delivery_stop_site_id
                FROM branches
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String siteId = rs.getString("delivery_stop_site_id");

                branches.add(new BranchDto(
                        rs.getString("branch_id"),
                        rs.getString("branch_name"),
                        rs.getString("address"),
                        siteId == null ? null : new SiteDAOImpl(connection).findbyId(siteId)
                ));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return branches;
    }
}