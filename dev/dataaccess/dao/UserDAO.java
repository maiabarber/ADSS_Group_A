package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.UserDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class UserDAO {
	public List<UserDto> listUsers() throws SQLException {
		String sql = """
				SELECT user_id, password, is_hr_manager
				FROM users
				ORDER BY user_id
				""";

		List<UserDto> users = new ArrayList<>();

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				users.add(new UserDto(
						resultSet.getString("user_id"),
						resultSet.getString("password"),
						resultSet.getInt("is_hr_manager")  == 1
				));
			}
		}

		return users;
	}

	public Optional<UserDto> findUserById(String userId) throws SQLException {
		String sql = """
				SELECT user_id, password, is_hr_manager
				FROM users
				WHERE user_id = ?
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, userId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(new UserDto(
						resultSet.getString("user_id"),
						resultSet.getString("password"),
						resultSet.getInt("is_hr_manager") == 1
					));
				}
			}
		}

		return Optional.empty();
	}

	public void insertUser(String userId, String password, boolean isHrManager) throws SQLException {
		String sql = """
				INSERT OR IGNORE INTO users (user_id, password, is_hr_manager)
				VALUES (?, ?, ?)
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setInt(3, isHrManager ? 1 : 0);
			statement.executeUpdate();
		}
	}

	public void deleteUserById(String userId) throws SQLException {
		String sql = "DELETE FROM users WHERE user_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, userId);
			statement.executeUpdate();
		}
	}
}