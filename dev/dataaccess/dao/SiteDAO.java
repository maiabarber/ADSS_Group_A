package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.SiteDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

public class SiteDAO {
	public List<SiteDto> listSites() throws SQLException {
		String sql = """
				SELECT site_name, address, contact_name, phone_number, zone_code, site_type
				FROM sites
				ORDER BY site_id
				""";

		List<SiteDto> sites = new ArrayList<>();

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				sites.add(new SiteDto(
						resultSet.getString("site_name"),
						resultSet.getString("address"),
						resultSet.getString("contact_name"),
						resultSet.getString("phone_number"),
						null,
						null
				));
			}
		}

		return sites;
	}

	public Optional<SiteDto> findSiteById(int siteId) throws SQLException {
		String sql = """
				SELECT site_id, site_name, address, contact_name, phone_number, zone_code, site_type
				FROM sites
				WHERE site_id = ?
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, siteId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(new SiteDto(
						resultSet.getString("site_name"),
						resultSet.getString("address"),
						resultSet.getString("contact_name"),
						resultSet.getString("phone_number"),
						null,
						null
					));
				}
			}
		}

		return Optional.empty();
	}

	public void insertSite(int siteId,
	                       String siteName,
	                       String address,
	                       String contactName,
	                       String phoneNumber,
	                       String zoneCode,
	                       String siteType) throws SQLException {
		String sql = """
				INSERT OR IGNORE INTO sites (
				    site_id,
				    site_name,
				    address,
				    contact_name,
				    phone_number,
				    zone_code,
				    site_type
				)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, siteId);
			statement.setString(2, siteName);
			statement.setString(3, address);
			statement.setString(4, contactName);
			statement.setString(5, phoneNumber);
			statement.setString(6, zoneCode);
			statement.setString(7, siteType);
			statement.executeUpdate();
		}
	}

	public void deleteSiteById(int siteId) throws SQLException {
		String sql = "DELETE FROM sites WHERE site_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, siteId);
			statement.executeUpdate();
		}
	}
}