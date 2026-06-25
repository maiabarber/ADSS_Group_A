package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.BranchDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

public class BranchDAO {
	public List<BranchDto> listBranches() throws SQLException {
		String sql = """
				SELECT branch_id, branch_name, address
				FROM branches
				ORDER BY branch_id
				""";

		List<BranchDto> branches = new ArrayList<>();

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				branches.add(new BranchDto(
						String.valueOf(resultSet.getInt("branch_id")),
						resultSet.getString("branch_name"),
						resultSet.getString("address"),
						null
				));
			}
		}

		return branches;
	}

	public Optional<BranchDto> findBranchById(int branchId) throws SQLException {
		String sql = """
				SELECT branch_id, branch_name, address
				FROM branches
				WHERE branch_id = ?
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, branchId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(new BranchDto(
						String.valueOf(resultSet.getInt("branch_id")),
						resultSet.getString("branch_name"),
						resultSet.getString("address"),
						null
					));
				}
			}
		}

		return Optional.empty();
	}

	public void insertBranch(int branchId, String branchName, String address) throws SQLException {
		String sql = """
				INSERT OR IGNORE INTO branches (branch_id, branch_name, address)
				VALUES (?, ?, ?)
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, branchId);
			statement.setString(2, branchName);
			statement.setString(3, address);
			statement.executeUpdate();
		}
	}

	public void deleteBranchById(int branchId) throws SQLException {
		String sql = "DELETE FROM branches WHERE branch_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, branchId);
			statement.executeUpdate();
		}
	}

	
}