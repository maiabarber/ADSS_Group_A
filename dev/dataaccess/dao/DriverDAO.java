package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.DriverDto;
import transportation.domain.LicenseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.List;

public class DriverDAO {
	public List<DriverDto> listDrivers() throws SQLException {
		String sql = """
				SELECT employee_id, driver_name
				FROM drivers
				ORDER BY employee_id
				""";

		List<DriverDto> drivers = new ArrayList<>();

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				drivers.add(new DriverDto(
						resultSet.getString("employee_id"),
						resultSet.getString("driver_name"),
						java.util.Collections.emptySet()
				));
			}
		}

		return drivers;
	}

	public Optional<DriverDto> findDriverById(String employeeId) throws SQLException {
		String sql = """
				SELECT employee_id, driver_name
				FROM drivers
				WHERE employee_id = ?
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, employeeId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(new DriverDto(
						resultSet.getString("employee_id"),
						resultSet.getString("driver_name"),
						new HashSet<LicenseType>()
					));
				}
			}
		}

		return Optional.empty();
	}

	public void insertDriver(String employeeId, String driverName) throws SQLException {
		String sql = """
				INSERT OR IGNORE INTO drivers (employee_id, driver_name)
				VALUES (?, ?)
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, employeeId);
			statement.setString(2, driverName);
			statement.executeUpdate();
		}
	}

	public void deleteDriverById(String employeeId) throws SQLException {
		String sql = "DELETE FROM drivers WHERE employee_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, employeeId);
			statement.executeUpdate();
		}
	}
}