package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.TruckDto;

import transportation.domain.LicenseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

public class TruckDAO {
	
	private final Connection connection;

	public TruckDAO(Connection connection) {
		this.connection = connection;
	}
	public List<TruckDto> listTrucks() throws SQLException {
		String sql = """
				SELECT license_number, model, net_weight, max_allowed_weight, required_license_type
				FROM trucks
				ORDER BY license_number
				""";

		List<TruckDto> trucks = new ArrayList<>();

		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				trucks.add(new TruckDto(
						resultSet.getString("license_number"),
						resultSet.getString("model"),
						resultSet.getDouble("net_weight"),
						resultSet.getDouble("max_allowed_weight"),
						LicenseType.valueOf(resultSet.getString("required_license_type"))
				));
			}
		}

		return trucks;
	}

	public Optional<TruckDto> findTruckByLicenseNumber(String licenseNumber) throws SQLException {
		String sql = """
				SELECT license_number, model, net_weight, max_allowed_weight, required_license_type
				FROM trucks
				WHERE license_number = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, licenseNumber);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(new TruckDto(
						resultSet.getString("license_number"),
						resultSet.getString("model"),
						resultSet.getDouble("net_weight"),
						resultSet.getDouble("max_allowed_weight"),
						LicenseType.valueOf(resultSet.getString("required_license_type"))
					));
				}
			}
		}

		return Optional.empty();
	}

	public void insertTruck(String licenseNumber,
	                        String model,
	                        double netWeight,
	                        double maxAllowedWeight,
	                        String requiredLicenseType) throws SQLException {
		String sql = """
				INSERT OR IGNORE INTO trucks (
				    license_number,
				    model,
				    net_weight,
				    max_allowed_weight,
				    required_license_type
				)
				VALUES (?, ?, ?, ?, ?)
				""";

		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, licenseNumber);
			statement.setString(2, model);
			statement.setDouble(3, netWeight);
			statement.setDouble(4, maxAllowedWeight);
			statement.setString(5, requiredLicenseType);
			statement.executeUpdate();
		}
	}

	public void deleteTruckByLicenseNumber(String licenseNumber) throws SQLException {
		String sql = "DELETE FROM trucks WHERE license_number = ?";

		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, licenseNumber);
			statement.executeUpdate();
		}
	}
}