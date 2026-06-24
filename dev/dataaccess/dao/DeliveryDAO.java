package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DriverDto;
import dataaccess.dto.ShippingZoneDto;
import dataaccess.dto.SiteDto;
import dataaccess.dto.TruckDto;

import transportation.domain.DeliveryStatus;
import transportation.domain.LicenseType;
import transportation.domain.SiteType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeliveryDAO {
	public List<DeliveryDto> listDeliveries() throws SQLException {
		String sql = """
				SELECT d.delivery_id,
				       d.delivery_date,
				       d.departure_time,
				       d.final_measured_weight,
				       d.status,
				       d.source_site_id,
				       d.truck_license_number,
				       d.driver_employee_id,
				       d.zone_code,
				       s.site_name,
				       s.address,
				       s.contact_name,
				       s.phone_number,
				       s.site_type,
				       z.zone_name,
				       t.model,
				       t.net_weight,
				       t.max_allowed_weight,
				       t.required_license_type,
				       dr.driver_name
				FROM deliveries d
				JOIN sites s ON d.source_site_id = s.site_id
				JOIN shipping_zones z ON d.zone_code = z.zone_code
				JOIN trucks t ON d.truck_license_number = t.license_number
				JOIN drivers dr ON d.driver_employee_id = dr.employee_id
				ORDER BY d.delivery_id
				""";

		List<DeliveryDto> deliveries = new ArrayList<>();

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				deliveries.add(new DeliveryDto(
						resultSet.getInt("delivery_id"),
						LocalDate.parse(resultSet.getString("delivery_date")),
						new SiteDto(
								resultSet.getString("site_name"),
								resultSet.getString("address"),
								resultSet.getString("contact_name"),
								resultSet.getString("phone_number"),
								new ShippingZoneDto(resultSet.getString("zone_code"), resultSet.getString("zone_name")),
								SiteType.valueOf(resultSet.getString("site_type"))
						),
						Collections.emptyList(),
						LocalTime.parse(resultSet.getString("departure_time")),
						resultSet.getDouble("final_measured_weight"),
						new TruckDto(
								resultSet.getString("truck_license_number"),
								resultSet.getString("model"),
								resultSet.getDouble("net_weight"),
								resultSet.getDouble("max_allowed_weight"),
								LicenseType.valueOf(resultSet.getString("required_license_type"))
						),
						new DriverDto(resultSet.getString("driver_employee_id"), resultSet.getString("driver_name"), Collections.emptySet()),
						new ShippingZoneDto(resultSet.getString("zone_code"), resultSet.getString("zone_name")),
						DeliveryStatus.valueOf(resultSet.getString("status")),
						null
				));
			}
		}

		return deliveries;
	}

	public int countDeliveries() throws SQLException {
		String sql = "SELECT COUNT(*) AS total FROM deliveries";

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {
			return resultSet.next() ? resultSet.getInt("total") : 0;
		}
	}
}