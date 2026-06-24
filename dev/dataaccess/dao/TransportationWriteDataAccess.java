package dataaccess.dao;

import dataaccess.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// This class writes transportation data into the SQLite database.
public class TransportationWriteDataAccess {

    public void insertShippingZone(String zoneCode, String zoneName) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO shipping_zones (zone_code, zone_name)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, zoneCode);
            statement.setString(2, zoneName);
            statement.executeUpdate();
        }
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

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, licenseNumber);
            statement.setString(2, model);
            statement.setDouble(3, netWeight);
            statement.setDouble(4, maxAllowedWeight);
            statement.setString(5, requiredLicenseType);
            statement.executeUpdate();
        }
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

    public void insertDriverLicenseType(String employeeId, String licenseType) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO driver_license_types (employee_id, license_type)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId);
            statement.setString(2, licenseType);
            statement.executeUpdate();
        }
    }

    public void insertDelivery(int deliveryId,
                               String deliveryDate,
                               int sourceSiteId,
                               String departureTime,
                               double finalMeasuredWeight,
                               String truckLicenseNumber,
                               String driverEmployeeId,
                               String zoneCode,
                               String status) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO deliveries (
                    delivery_id,
                    delivery_date,
                    source_site_id,
                    departure_time,
                    final_measured_weight,
                    truck_license_number,
                    driver_employee_id,
                    zone_code,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, deliveryId);
            statement.setString(2, deliveryDate);
            statement.setInt(3, sourceSiteId);
            statement.setString(4, departureTime);
            statement.setDouble(5, finalMeasuredWeight);
            statement.setString(6, truckLicenseNumber);
            statement.setString(7, driverEmployeeId);
            statement.setString(8, zoneCode);
            statement.setString(9, status);
            statement.executeUpdate();
        }
    }

    public void insertDeliveryStop(int stopId,
                                   int deliveryId,
                                   int stopOrder,
                                   String stopType,
                                   int siteId,
                                   String plannedArrival) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO delivery_stops (
                    stop_id,
                    delivery_id,
                    stop_order,
                    stop_type,
                    site_id,
                    planned_arrival
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, stopId);
            statement.setInt(2, deliveryId);
            statement.setInt(3, stopOrder);
            statement.setString(4, stopType);
            statement.setInt(5, siteId);
            statement.setString(6, plannedArrival);
            statement.executeUpdate();
        }
    }

    public void insertDeliveryDocument(int documentNumber, int stopId) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO delivery_documents (document_number, stop_id)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, documentNumber);
            statement.setInt(2, stopId);
            statement.executeUpdate();
        }
    }

    public void insertDeliveryItem(String itemId,
                                   int documentNumber,
                                   String itemName,
                                   int quantity) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO delivery_items (
                    item_id,
                    document_number,
                    item_name,
                    quantity
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, itemId);
            statement.setInt(2, documentNumber);
            statement.setString(3, itemName);
            statement.setInt(4, quantity);
            statement.executeUpdate();
        }
    }

    public void insertDeliveryFormMeasurement(int deliveryId, double measuredWeight) throws SQLException {
        String sql = """
                INSERT INTO delivery_form_measurements (delivery_id, measured_weight)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, deliveryId);
            statement.setDouble(2, measuredWeight);
            statement.executeUpdate();
        }
    }
}