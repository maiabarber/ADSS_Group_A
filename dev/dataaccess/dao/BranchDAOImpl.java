package dataaccess.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import employee.domain.Branch;

public class BranchDAOImpl implements DaoInterface<Branch> {
	private Connection connection;

    public BranchDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(Branch b) {
        String sql = "INSERT OR REPLACE INTO branches (branch_id, name, location) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, b.getBranchId());
            pstmt.setString(2, b.getBranchName());
            pstmt.setString(3, b.getLocation());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace(); 
        }
    }

	@Override
	public Branch findbyId(String id) {
		String sql = "SELECT * FROM branches WHERE branch_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return new Branch(rs.getString("branch_id"), rs.getString("name"), rs.getString("location"));
			}
		} catch (SQLException ex) {
			ex.printStackTrace(); 
		}
		return null; 
	}

	@Override
	public void update(Branch b) {
		String sql = "UPDATE branches SET name = ?, location = ? WHERE branch_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, b.getBranchName());
			pstmt.setString(2, b.getLocation());
			pstmt.setString(3, b.getBranchId());
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace(); 
		}
	}

	@Override
	public void delete(String id) {
		String sql = "DELETE FROM branches WHERE branch_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, id);
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace(); 
		}
	}

	@Override
	public List<Branch> findAll() {
		List<Branch> branches = new ArrayList<>();
		String sql = "SELECT * FROM branches";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				branches.add(new Branch(rs.getString("branch_id"), rs.getString("name"), rs.getString("location")));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return branches;
	}

	
}