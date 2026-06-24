package dataaccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// This class reads transportation data from the SQLite database.
public class TransportationDataAccess {

    public void printAllDrivers() throws SQLException {
        String sql = """
                SELECT employee_id, driver_name
                FROM drivers
                ORDER BY employee_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Drivers from database:");
            while (resultSet.next()) {
                System.out.println(
                        resultSet.getString("employee_id") + " - " +
                        resultSet.getString("driver_name")
                );
            }
        }
    }

    public void printAllTrucks() throws SQLException {
        String sql = """
                SELECT license_number, model, net_weight, max_allowed_weight, required_license_type
                FROM trucks
                ORDER BY license_number
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Trucks from database:");
            while (resultSet.next()) {
                System.out.println(
                        resultSet.getString("license_number") + " - " +
                        resultSet.getString("model") +
                        ", net weight: " + resultSet.getDouble("net_weight") +
                        ", max weight: " + resultSet.getDouble("max_allowed_weight") +
                        ", license: " + resultSet.getString("required_license_type")
                );
            }
        }
    }

    public void printAllSites() throws SQLException {
        String sql = """
                SELECT site_name, address, contact_name, phone_number, zone_code, site_type
                FROM sites
                ORDER BY site_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Sites from database:");
            while (resultSet.next()) {
                System.out.println(
                        resultSet.getString("site_name") +
                        " | " + resultSet.getString("address") +
                        " | zone: " + resultSet.getString("zone_code") +
                        " | type: " + resultSet.getString("site_type")
                );
            }
        }
    }

    public void printAllDeliveries() throws SQLException {
        String sql = """
                SELECT
                    d.delivery_id,
                    d.delivery_date,
                    d.departure_time,
                    d.final_measured_weight,
                    d.status,
                    s.site_name AS source_name,
                    t.license_number AS truck_license,
                    dr.driver_name,
                    z.zone_name
                FROM deliveries d
                JOIN sites s ON d.source_site_id = s.site_id
                JOIN trucks t ON d.truck_license_number = t.license_number
                JOIN drivers dr ON d.driver_employee_id = dr.employee_id
                JOIN shipping_zones z ON d.zone_code = z.zone_code
                ORDER BY d.delivery_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Deliveries from database:");
            while (resultSet.next()) {
                System.out.println(
                        "Delivery #" + resultSet.getInt("delivery_id") +
                        " | date: " + resultSet.getString("delivery_date") +
                        " | departure: " + resultSet.getString("departure_time") +
                        " | source: " + resultSet.getString("source_name") +
                        " | truck: " + resultSet.getString("truck_license") +
                        " | driver: " + resultSet.getString("driver_name") +
                        " | zone: " + resultSet.getString("zone_name") +
                        " | weight: " + resultSet.getDouble("final_measured_weight") +
                        " | status: " + resultSet.getString("status")
                );
            }
        }
    }

    public int countDeliveries() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM deliveries";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

            return 0;
        }
    }
}