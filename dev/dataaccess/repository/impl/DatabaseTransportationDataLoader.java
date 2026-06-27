package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.DeliveryDAOImpl;
import dataaccess.dao.DriverDAOImpl;
import dataaccess.dao.SiteDAOImpl;
import dataaccess.dao.TruckDAO;
import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DriverDto;
import dataaccess.dto.ShippingZoneDto;
import dataaccess.dto.SiteDto;
import dataaccess.dto.TruckDto;
import transportation.domain.Delivery;
import transportation.domain.DeliveryForm;
import transportation.domain.Driver;
import transportation.domain.LicenseType;
import transportation.domain.ShippingZone;
import transportation.domain.Site;
import transportation.domain.SiteType;
import transportation.domain.Truck;
import transportation.domain.DeliveryManager;
import transportation.domain.DeliveryStop;
import transportation.domain.StopType;
import dataaccess.mapper.SiteMapper;
import dataaccess.repository.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseTransportationDataLoader {
    private final Connection connection;
    private final TruckDAO truckDAO;
    private final SiteDAOImpl siteDAO;
    private final DriverDAOImpl driverDAO;
    private final DeliveryDAOImpl deliveryDAO;

    public DatabaseTransportationDataLoader() throws SQLException {
        ensureSchema();
        this.connection = DatabaseConnection.getConnection();
        this.truckDAO = new TruckDAO(connection);
        this.siteDAO = new SiteDAOImpl(connection);
        this.driverDAO = new DriverDAOImpl(connection);
        this.deliveryDAO = new DeliveryDAOImpl(connection);
    }


    public void loadInto(DeliveryManager deliveryManager) throws SQLException {
        Map<String, ShippingZone> zones = loadShippingZones(deliveryManager);
        Map<String, Site> sites = loadSites(deliveryManager, zones);
        Map<String, Truck> trucks = loadTrucks(deliveryManager);
        Map<String, Driver> drivers = loadDrivers(deliveryManager);
        loadDeliveries(deliveryManager, sites, trucks, drivers, zones);
    }

    private Map<String, ShippingZone> loadShippingZones(DeliveryManager deliveryManager) throws SQLException {
        Map<String, ShippingZone> zones = new HashMap<>();
        String sql = "SELECT zone_code, zone_name FROM shipping_zones ORDER BY zone_code";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                ShippingZone zone = new ShippingZone(
                        resultSet.getString("zone_code"),
                        resultSet.getString("zone_name"));
                deliveryManager.addShippingZone(zone);
                zones.put(zone.getZoneCode(), zone);
            }
        }
        return zones;
    }

    private Map<String, Site> loadSites(DeliveryManager deliveryManager, Map<String, ShippingZone> zones) throws SQLException {
        Map<String, Site> sites = new HashMap<>();
        for (SiteDto siteDto : siteDAO.findAll()) {
            ShippingZone zone = siteDto.getShippingZone() == null ? null : zones.get(siteDto.getShippingZone().getZoneCode());
            if (zone == null) {
                zone = zones.values().stream().findFirst().orElse(new ShippingZone("UNKNOWN", "Unknown"));
            }
            Site site = SiteMapper.toDomain(siteDto);
            deliveryManager.addSite(site);
            sites.put(site.getSiteName(), site);
        }
        return sites;
    }

    private Map<String, Truck> loadTrucks(DeliveryManager deliveryManager) throws SQLException {
        Map<String, Truck> trucks = new HashMap<>();
        for (TruckDto dto : truckDAO.listTrucks()) {
            Truck truck = new Truck(
                    dto.getLicenseNumber(),
                    dto.getModel(),
                    dto.getNetWeight(),
                    dto.getMaxAllowedWeight(),
                    dto.getRequiredLicenseType());
            deliveryManager.addTruck(truck);
            trucks.put(truck.getLicenseNumber(), truck);
        }
        return trucks;
    }

    private Map<String, Driver> loadDrivers(DeliveryManager deliveryManager) throws SQLException {
        Map<String, Driver> drivers = new HashMap<>();
        try {
    for (DriverDto dto : driverDAO.findAll()) {
        Driver driver = new Driver(
                dto.getEmployeeId(),
                dto.getDriverName(),
                loadLicenseTypes(dto.getEmployeeId())
                );
                deliveryManager.addDriver(driver);
                drivers.put(driver.getEmployeeId(), driver);
            }
        } catch (RepositoryException e) {
            throw new SQLException("Failed to load drivers", e);
        }
        return drivers;
    }

    private void loadDeliveries(
            DeliveryManager deliveryManager,
            Map<String, Site> sites,
            Map<String, Truck> trucks,
            Map<String, Driver> drivers,
            Map<String, ShippingZone> zones) throws SQLException {
                try {
        for (DeliveryDto dto : deliveryDAO.findAll()) {
            Site source = sites.get(dto.getSource().getSiteName());
            Truck truck = trucks.get(dto.getTruck().getLicenseNumber());
            Driver driver = drivers.get(dto.getDriver().getEmployeeId());
            ShippingZone zone = zones.get(dto.getShippingZone().getZoneCode());
            if (source == null || truck == null || driver == null || zone == null) {
                continue;
            }
            List<DeliveryStop> stops = loadStops(dto.getDeliveryId(), sites, dto.getDeliveryDate().atTime(dto.getDepartureTime()));
            if (stops.isEmpty()) {
                continue;
            }
            Delivery delivery = new Delivery(
                    dto.getDeliveryId(),
                    dto.getDeliveryDate(),
                    source,
                    stops,
                    dto.getDepartureTime(),
                    dto.getFinalMeasuredWeightBeforeDeparture(),
                    truck,
                    driver,
                    zone,
                    dto.getStatus(),
                    new DeliveryForm());
            deliveryManager.addDelivery(delivery);
        }} catch (RepositoryException e) {
            throw new SQLException("Failed to load deliveries", e);
        }
    }

    private List<DeliveryStop> loadStops(int deliveryId, Map<String, Site> sites, LocalDateTime fallbackArrival) throws SQLException {
        List<DeliveryStop> stops = new ArrayList<>();
        String sql = """
                SELECT ds.stop_order, ds.stop_type, ds.planned_arrival, s.site_name
                FROM delivery_stops ds
                JOIN sites s ON s.site_id = ds.site_id
                WHERE ds.delivery_id = ?
                ORDER BY ds.stop_order
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, deliveryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Site site = sites.get(resultSet.getString("site_name"));
                    if (site == null) {
                        continue;
                    }
                    String rawArrival = resultSet.getString("planned_arrival");
                    LocalDateTime arrival = rawArrival == null || rawArrival.isBlank()
                            ? fallbackArrival
                            : LocalDateTime.parse(rawArrival);
                    stops.add(new DeliveryStop(
                            resultSet.getInt("stop_order"),
                            StopType.valueOf(resultSet.getString("stop_type")),
                            site,
                            arrival));
                }
            }
        }
        return stops;
    }

    private Set<LicenseType> loadLicenseTypes(String employeeId) throws SQLException {
        Set<LicenseType> licenseTypes = new HashSet<>();
        String sql = "SELECT license_type FROM driver_license_types WHERE employee_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    licenseTypes.add(LicenseType.valueOf(resultSet.getString("license_type")));
                }
            }
        }
        return licenseTypes;
    }

    private void ensureSchema() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        }
    }
}
