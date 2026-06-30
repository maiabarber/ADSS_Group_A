package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.DeliveryDAOImpl;
import dataaccess.dao.DriverDAOImpl;
import dataaccess.dao.SiteDAOImpl;
import dataaccess.dao.TruckDAO;
import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DriverDto;
import dataaccess.dto.SiteDto;
import dataaccess.dto.TruckDto;
import dataaccess.mapper.SiteMapper;
import dataaccess.repository.RepositoryException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import transportation.domain.Delivery;
import transportation.domain.DeliveryDocument;
import transportation.domain.DeliveryForm;
import transportation.domain.DeliveryItem;
import transportation.domain.DeliveryManager;
import transportation.domain.DeliveryStatus;
import transportation.domain.DeliveryStop;
import transportation.domain.Driver;
import transportation.domain.LicenseType;
import transportation.domain.ShippingZone;
import transportation.domain.Site;
import transportation.domain.StopType;
import transportation.domain.Truck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTransportationDataLoader {
    public DatabaseTransportationDataLoader() throws SQLException {
        DatabaseInitializer.initializeDatabase();
    }

    public void loadInto(DeliveryManager deliveryManager) throws SQLException {
        Map<String, ShippingZone> zones = loadShippingZones(deliveryManager);
        Map<Integer, Site> sites = loadSites(deliveryManager);
        Map<String, Truck> trucks = loadTrucks(deliveryManager);
        Map<String, Driver> drivers = loadDrivers(deliveryManager);
        loadDeliveries(deliveryManager, sites, trucks, drivers, zones);
        deliveryManager.synchronizeNextDocumentNumber(loadNextDocumentNumber());
    }

    private Map<String, ShippingZone> loadShippingZones(DeliveryManager deliveryManager) throws SQLException {
        Map<String, ShippingZone> zones = new HashMap<>();
        String sql = "SELECT zone_code, zone_name FROM shipping_zones";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                ShippingZone zone = new ShippingZone(
                        rs.getString("zone_code"),
                        rs.getString("zone_name")
                );
                deliveryManager.addShippingZone(zone);
                zones.put(zone.getZoneCode(), zone);
            }
        }

        return zones;
    }

    private Map<Integer, Site> loadSites(DeliveryManager deliveryManager) throws SQLException {
        Map<Integer, Site> sites = new HashMap<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            SiteDAOImpl siteDAO = new SiteDAOImpl(connection);
            for (SiteDto dto : siteDAO.findAll()) {
                Site site = SiteMapper.toDomain(dto);
                deliveryManager.addSite(site);
                sites.put(dto.getSiteId(), site);
            }
        } catch (RepositoryException e) {
            throw new SQLException("Failed to load sites", e);
        }

        return sites;
    }

    private Map<String, Truck> loadTrucks(DeliveryManager deliveryManager) throws SQLException {
    Map<String, Truck> trucks = new HashMap<>();

    try (Connection connection = DatabaseConnection.getConnection()) {
        TruckDAO truckDAO = new TruckDAO(connection);
        for (TruckDto dto : truckDAO.findAll()) {
            Truck truck = new Truck(
                    dto.getLicenseNumber(),
                    dto.getModel(),
                    dto.getNetWeight(),
                    dto.getMaxAllowedWeight(),
                    LicenseType.valueOf(dto.getRequiredLicenseType())
            );

            deliveryManager.addTruck(truck);
            trucks.put(dto.getLicenseNumber(), truck);
        }
    } catch (RepositoryException e) {
        throw new SQLException("Failed to load trucks", e);
    }

    return trucks;
}

    private Map<String, Driver> loadDrivers(DeliveryManager deliveryManager) throws SQLException {
        Map<String, Driver> drivers = new HashMap<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            DriverDAOImpl driverDAO = new DriverDAOImpl(connection);
            for (DriverDto dto : driverDAO.findAll()) {
                Driver driver = new Driver(
                        dto.getEmployeeId(),
                        dto.getDriverName(),
                        loadLicenseTypes(dto.getEmployeeId())
                );
                deliveryManager.addDriver(driver);
                drivers.put(dto.getEmployeeId(), driver);
            }
        } catch (RepositoryException e) {
            throw new SQLException("Failed to load drivers", e);
        }

        return drivers;
    }

    private void loadDeliveries(
            DeliveryManager deliveryManager,
            Map<Integer, Site> sites,
            Map<String, Truck> trucks,
            Map<String, Driver> drivers,
            Map<String, ShippingZone> zones
    ) throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection()) {
            DeliveryDAOImpl deliveryDAO = new DeliveryDAOImpl(connection);
            for (DeliveryDto dto : deliveryDAO.findAll()) {
                Site source = sites.get(dto.getSourceSiteId());
                Truck truck = trucks.get(dto.getTruckLicenseNumber());
                Driver driver = drivers.get(dto.getDriverEmployeeId());
                ShippingZone zone = zones.get(dto.getZoneCode());

                if (source == null || truck == null || driver == null || zone == null) {
                    continue;
                }

                LocalDate deliveryDate = LocalDate.parse(dto.getDeliveryDate());
                LocalTime departureTime = LocalTime.parse(dto.getDepartureTime());

                List<DeliveryStop> stops = loadStops(
                        dto.getDeliveryId(),
                        sites,
                        deliveryDate.atTime(departureTime)
                );

                if (stops.isEmpty()) {
                    continue;
                }

                Delivery delivery = new Delivery(
                        dto.getDeliveryId(),
                        deliveryDate,
                        source,
                        stops,
                        departureTime,
                        dto.getFinalMeasuredWeight(),
                        truck,
                        driver,
                        zone,
                        DeliveryStatus.valueOf(dto.getStatus()),
                        new DeliveryForm(loadMeasurements(dto.getDeliveryId()))
                );

                deliveryManager.addDelivery(delivery);
            }
        } catch (RepositoryException e) {
            throw new SQLException("Failed to load deliveries", e);
        }
    }

    private List<DeliveryStop> loadStops(
            int deliveryId,
            Map<Integer, Site> sites,
            LocalDateTime fallbackArrival
    ) throws SQLException {

        List<DeliveryStop> stops = new ArrayList<>();

        String sql = """
                SELECT stop_id, stop_order, stop_type, site_id, planned_arrival
                FROM delivery_stops
                WHERE delivery_id = ?
                ORDER BY stop_order
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, deliveryId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Site site = sites.get(rs.getInt("site_id"));

                    if (site == null) {
                        continue;
                    }

                    String rawArrival = rs.getString("planned_arrival");

                    LocalDateTime arrival = rawArrival == null || rawArrival.isBlank()
                            ? fallbackArrival
                            : LocalDateTime.parse(rawArrival);

                    DeliveryDocument document = loadDocument(rs.getInt("stop_id"));

                    if (document == null) {
                        stops.add(new DeliveryStop(
                                rs.getInt("stop_order"),
                                StopType.valueOf(rs.getString("stop_type")),
                                site,
                                arrival
                        ));
                    } else {
                        stops.add(new DeliveryStop(
                                rs.getInt("stop_order"),
                                StopType.valueOf(rs.getString("stop_type")),
                                site,
                                document,
                                arrival
                        ));
                    }
                }
            }
        }

        return stops;
    }

    private DeliveryDocument loadDocument(int stopId) throws SQLException {
        String sql = """
                SELECT document_number
                FROM delivery_documents
                WHERE stop_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, stopId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int documentNumber = rs.getInt("document_number");
                return new DeliveryDocument(documentNumber, loadItems(documentNumber));
            }
        }
    }

    private List<DeliveryItem> loadItems(int documentNumber) throws SQLException {
        List<DeliveryItem> items = new ArrayList<>();
        String sql = """
                SELECT item_id, item_name, quantity
                FROM delivery_items
                WHERE document_number = ?
                ORDER BY item_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, documentNumber);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(new DeliveryItem(
                            rs.getString("item_id"),
                            rs.getString("item_name"),
                            rs.getInt("quantity")
                    ));
                }
            }
        }

        return items;
    }

    private List<Double> loadMeasurements(int deliveryId) throws SQLException {
        List<Double> measurements = new ArrayList<>();
        String sql = """
                SELECT measured_weight
                FROM delivery_form_measurements
                WHERE delivery_id = ?
                ORDER BY measurement_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, deliveryId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    measurements.add(rs.getDouble("measured_weight"));
                }
            }
        }

        return measurements;
    }

    private Set<LicenseType> loadLicenseTypes(String employeeId) throws SQLException {
        Set<LicenseType> licenseTypes = new HashSet<>();

        String sql = """
                SELECT license_type
                FROM driver_license_types
                WHERE employee_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    licenseTypes.add(LicenseType.valueOf(rs.getString("license_type")));
                }
            }
        }

        return licenseTypes;
    }

    private int loadNextDocumentNumber() throws SQLException {
        String sql = "SELECT COALESCE(MAX(document_number), 0) + 1 FROM delivery_documents";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }
}
