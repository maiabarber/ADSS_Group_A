package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.DeliveryDocumentDto;
import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DeliveryFormDto;
import dataaccess.dto.DeliveryFormMeasurementDto;
import dataaccess.dto.DeliveryItemDto;
import dataaccess.dto.DeliveryStopDto;
import dataaccess.dto.DriverDto;
import dataaccess.dto.ShippingZoneDto;
import dataaccess.dto.SiteDto;
import dataaccess.dto.TruckDto;

import transportation.domain.DeliveryStatus;
import transportation.domain.LicenseType;
import transportation.domain.SiteType;
import transportation.domain.StopType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class reads transportation data from the SQLite database.
public class TransportationDataAccess {

    public List<ShippingZoneDto> listShippingZones() throws SQLException {
        String sql = """
                SELECT zone_code, zone_name
                FROM shipping_zones
                ORDER BY zone_code
                """;

        List<ShippingZoneDto> zones = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                zones.add(new ShippingZoneDto(
                        resultSet.getString("zone_code"),
                        resultSet.getString("zone_name")
                ));
            }
        }

        return zones;
    }

    public List<TruckDto> listTrucks() throws SQLException {
        String sql = """
                SELECT license_number, model, net_weight, max_allowed_weight, required_license_type
                FROM trucks
                ORDER BY license_number
                """;

        List<TruckDto> trucks = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
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

    public List<SiteDto> listSites() throws SQLException {
        String sql = """
                SELECT site_name, address, contact_name, phone_number, zone_code, zone_name, site_type
                FROM sites
                JOIN shipping_zones ON sites.zone_code = shipping_zones.zone_code
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
                        new ShippingZoneDto(
                                resultSet.getString("zone_code"),
                                resultSet.getString("zone_name")
                        ),
                        SiteType.valueOf(resultSet.getString("site_type"))
                ));
            }
        }

        return sites;
    }

    public List<DriverDto> listDrivers() throws SQLException {
        String sql = """
                SELECT d.employee_id, d.driver_name, l.license_type
                FROM drivers d
                LEFT JOIN driver_license_types l ON d.employee_id = l.employee_id
                ORDER BY d.employee_id, l.license_type
                """;

        Map<String, java.util.Set<LicenseType>> licensesByDriver = new HashMap<>();
        Map<String, String> driverNames = new HashMap<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String employeeId = resultSet.getString("employee_id");
                driverNames.put(employeeId, resultSet.getString("driver_name"));
                String licenseType = resultSet.getString("license_type");
                if (licenseType != null) {
                    licensesByDriver.computeIfAbsent(employeeId, key -> new java.util.HashSet<>())
                            .add(LicenseType.valueOf(licenseType));
                }
            }
        }

        List<DriverDto> drivers = new ArrayList<>();
        for (Map.Entry<String, String> entry : driverNames.entrySet()) {
            drivers.add(new DriverDto(
                    entry.getKey(),
                    entry.getValue(),
                    licensesByDriver.getOrDefault(entry.getKey(), Collections.emptySet())
            ));
        }

        return drivers;
    }

    public List<DeliveryDto> listDeliveries() throws SQLException {
        String sql = """
                SELECT
                    d.delivery_id,
                    d.delivery_date,
                    d.departure_time,
                    d.final_measured_weight,
                    d.status,
                    s.site_name AS source_name,
                    s.address AS source_address,
                    s.contact_name AS source_contact_name,
                    s.phone_number AS source_phone_number,
                    s.site_type AS source_site_type,
                    sz.zone_code,
                    sz.zone_name,
                    t.license_number,
                    t.model,
                    t.net_weight,
                    t.max_allowed_weight,
                    t.required_license_type,
                    dr.employee_id AS driver_employee_id,
                    dr.driver_name
                FROM deliveries d
                JOIN sites s ON d.source_site_id = s.site_id
                JOIN shipping_zones sz ON d.zone_code = sz.zone_code
                JOIN trucks t ON d.truck_license_number = t.license_number
                JOIN drivers dr ON d.driver_employee_id = dr.employee_id
                ORDER BY d.delivery_id
                """;

        List<DeliveryDto> deliveries = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int deliveryId = resultSet.getInt("delivery_id");
                deliveries.add(new DeliveryDto(
                        deliveryId,
                        LocalDate.parse(resultSet.getString("delivery_date")),
                        new SiteDto(
                                resultSet.getString("source_name"),
                                resultSet.getString("source_address"),
                                resultSet.getString("source_contact_name"),
                                resultSet.getString("source_phone_number"),
                                new ShippingZoneDto(resultSet.getString("zone_code"), resultSet.getString("zone_name")),
                                SiteType.valueOf(resultSet.getString("source_site_type"))
                        ),
                        listStopsForDelivery(deliveryId),
                        LocalTime.parse(resultSet.getString("departure_time")),
                        resultSet.getDouble("final_measured_weight"),
                        new TruckDto(
                                resultSet.getString("license_number"),
                                resultSet.getString("model"),
                                resultSet.getDouble("net_weight"),
                                resultSet.getDouble("max_allowed_weight"),
                                LicenseType.valueOf(resultSet.getString("required_license_type"))
                        ),
                        new DriverDto(
                                resultSet.getString("driver_employee_id"),
                                resultSet.getString("driver_name"),
                                Collections.singleton(LicenseType.valueOf(resultSet.getString("required_license_type")))
                        ),
                        new ShippingZoneDto(resultSet.getString("zone_code"), resultSet.getString("zone_name")),
                        DeliveryStatus.valueOf(resultSet.getString("status")),
                        new DeliveryFormDto(listMeasurementsForDelivery(deliveryId))
                ));
            }
        }

        return deliveries;
    }

    public List<DeliveryStopDto> listStopsForDelivery(int deliveryId) throws SQLException {
        String sql = """
                SELECT
                    ds.stop_order,
                    ds.stop_type,
                    ds.planned_arrival,
                    s.site_name,
                    s.address,
                    s.contact_name,
                    s.phone_number,
                    s.site_type,
                    sz.zone_code,
                    sz.zone_name,
                    dd.document_number
                FROM delivery_stops ds
                JOIN sites s ON ds.site_id = s.site_id
                JOIN shipping_zones sz ON s.zone_code = sz.zone_code
                LEFT JOIN delivery_documents dd ON dd.stop_id = ds.stop_id
                WHERE ds.delivery_id = ?
                ORDER BY ds.stop_order
                """;

        List<DeliveryStopDto> stops = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, deliveryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    DeliveryDocumentDto document = resultSet.getInt("document_number") == 0
                            ? null
                            : new DeliveryDocumentDto(resultSet.getInt("document_number"), listItemsForDocument(resultSet.getInt("document_number")));
                    stops.add(new DeliveryStopDto(
                            resultSet.getInt("stop_order"),
                            StopType.valueOf(resultSet.getString("stop_type")),
                            new SiteDto(
                                    resultSet.getString("site_name"),
                                    resultSet.getString("address"),
                                    resultSet.getString("contact_name"),
                                    resultSet.getString("phone_number"),
                                    new ShippingZoneDto(resultSet.getString("zone_code"), resultSet.getString("zone_name")),
                                    SiteType.valueOf(resultSet.getString("site_type"))
                            ),
                            resultSet.getString("planned_arrival") == null ? null : LocalDateTime.parse(resultSet.getString("planned_arrival")),
                            document
                    ));
                }
            }
        }

        return stops;
    }

    public List<DeliveryItemDto> listItemsForDocument(int documentNumber) throws SQLException {
        String sql = """
                SELECT item_id, document_number, item_name, quantity
                FROM delivery_items
                WHERE document_number = ?
                ORDER BY item_id
                """;

        List<DeliveryItemDto> items = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, documentNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new DeliveryItemDto(
                            resultSet.getString("item_id"),
                            resultSet.getInt("document_number"),
                            resultSet.getString("item_name"),
                            resultSet.getInt("quantity")
                    ));
                }
            }
        }

        return items;
    }

    public List<Double> listMeasurementsForDelivery(int deliveryId) throws SQLException {
        String sql = """
                SELECT measured_weight
                FROM delivery_form_measurements
                WHERE delivery_id = ?
                ORDER BY measurement_id
                """;

        List<Double> measurements = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, deliveryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    measurements.add(resultSet.getDouble("measured_weight"));
                }
            }
        }

        return measurements;
    }

    public List<DeliveryFormMeasurementDto> listDeliveryMeasurements() throws SQLException {
        String sql = """
                SELECT measurement_id, delivery_id, measured_weight
                FROM delivery_form_measurements
                ORDER BY measurement_id
                """;

        List<DeliveryFormMeasurementDto> measurements = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                measurements.add(new DeliveryFormMeasurementDto(
                        resultSet.getInt("measurement_id"),
                        resultSet.getInt("delivery_id"),
                        resultSet.getDouble("measured_weight")
                ));
            }
        }

        return measurements;
    }

    public void printAllDrivers() throws SQLException {
        System.out.println("Drivers from database:");
        for (DriverDto driver : listDrivers()) {
            System.out.println(driver.getEmployeeId() + " - " + driver.getDriverName());
        }
    }

    public void printAllTrucks() throws SQLException {
        System.out.println("Trucks from database:");
        for (TruckDto truck : listTrucks()) {
            System.out.println(
                    truck.getLicenseNumber() + " - " + truck.getModel() +
                    ", net weight: " + truck.getNetWeight() +
                    ", max weight: " + truck.getMaxAllowedWeight() +
                    ", license: " + truck.getRequiredLicenseType()
            );
        }
    }

    public void printAllSites() throws SQLException {
        System.out.println("Sites from database:");
        for (SiteDto site : listSites()) {
            System.out.println(
                    site.getSiteName() +
                    " | " + site.getAddress() +
                    " | zone: " + (site.getShippingZone() != null ? site.getShippingZone().getZoneCode() : null) +
                    " | type: " + site.getSiteType()
            );
        }
    }

    public void printAllDeliveries() throws SQLException {
        System.out.println("Deliveries from database:");
        for (DeliveryDto delivery : listDeliveries()) {
            System.out.println(
                    "Delivery #" + delivery.getDeliveryId() +
                    " | date: " + delivery.getDeliveryDate() +
                    " | departure: " + delivery.getDepartureTime() +
                    " | source: " + (delivery.getSource() != null ? delivery.getSource().getSiteName() : null) +
                    " | truck: " + (delivery.getTruck() != null ? delivery.getTruck().getLicenseNumber() : null) +
                    " | driver: " + (delivery.getDriver() != null ? delivery.getDriver().getDriverName() : null) +
                    " | zone: " + (delivery.getShippingZone() != null ? delivery.getShippingZone().getZoneName() : null) +
                    " | weight: " + delivery.getFinalMeasuredWeightBeforeDeparture() +
                    " | status: " + delivery.getStatus()
            );
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