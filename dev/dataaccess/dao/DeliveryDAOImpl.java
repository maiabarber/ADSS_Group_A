package dataaccess.dao;

import dataaccess.dto.*;
import dataaccess.repository.RepositoryException;
import transportation.domain.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class DeliveryDAOImpl implements DaoInterface<DeliveryDto> {

    private final Connection connection;

    public DeliveryDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DeliveryDto delivery) throws RepositoryException {
        try {
            connection.setAutoCommit(false);

            upsertDelivery(delivery);
            deleteDeliveryChildren(delivery.getDeliveryId());
            insertStopsDocumentsItems(delivery);
            insertMeasurements(delivery);

            connection.commit();

        } catch (SQLException e) {
            rollback();
            throw new RepositoryException("Failed to save delivery", e);
        } finally {
            restoreAutoCommit();
        }
    }

    private void upsertDelivery(DeliveryDto delivery) throws SQLException {
        String sql = """
                INSERT INTO deliveries (
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
                ON CONFLICT(delivery_id) DO UPDATE SET
                    delivery_date = excluded.delivery_date,
                    source_site_id = excluded.source_site_id,
                    departure_time = excluded.departure_time,
                    final_measured_weight = excluded.final_measured_weight,
                    truck_license_number = excluded.truck_license_number,
                    driver_employee_id = excluded.driver_employee_id,
                    zone_code = excluded.zone_code,
                    status = excluded.status
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, delivery.getDeliveryId());
            stmt.setString(2, delivery.getDeliveryDate().toString());
            stmt.setInt(3, getSiteIdByName(delivery.getSource().getSiteName()));
            stmt.setString(4, delivery.getDepartureTime().toString());
            stmt.setDouble(5, delivery.getFinalMeasuredWeightBeforeDeparture());
            stmt.setString(6, delivery.getTruck().getLicenseNumber());
            stmt.setString(7, delivery.getDriver().getEmployeeId());
            stmt.setString(8, delivery.getShippingZone().getZoneCode());
            stmt.setString(9, delivery.getStatus().name());

            stmt.executeUpdate();
        }
    }

    private void deleteDeliveryChildren(int deliveryId) throws SQLException {
        String deleteItems = """
                DELETE FROM delivery_items
                WHERE document_number IN (
                    SELECT dd.document_number
                    FROM delivery_documents dd
                    JOIN delivery_stops ds ON dd.stop_id = ds.stop_id
                    WHERE ds.delivery_id = ?
                )
                """;

        String deleteDocuments = """
                DELETE FROM delivery_documents
                WHERE stop_id IN (
                    SELECT stop_id
                    FROM delivery_stops
                    WHERE delivery_id = ?
                )
                """;

        String deleteStops =
                "DELETE FROM delivery_stops WHERE delivery_id = ?";

        String deleteMeasurements =
                "DELETE FROM delivery_form_measurements WHERE delivery_id = ?";

        executeDelete(deleteItems, deliveryId);
        executeDelete(deleteDocuments, deliveryId);
        executeDelete(deleteStops, deliveryId);
        executeDelete(deleteMeasurements, deliveryId);
    }

    private void executeDelete(String sql, int deliveryId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deliveryId);
            stmt.executeUpdate();
        }
    }

    private void insertStopsDocumentsItems(DeliveryDto delivery) throws SQLException {
        String insertStop = """
                INSERT INTO delivery_stops (
                    delivery_id,
                    stop_order,
                    stop_type,
                    site_id,
                    planned_arrival
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stopStmt =
                     connection.prepareStatement(insertStop, Statement.RETURN_GENERATED_KEYS)) {

            for (DeliveryStopDto stop : delivery.getStops()) {
                stopStmt.setInt(1, delivery.getDeliveryId());
                stopStmt.setInt(2, stop.getStopOrder());
                stopStmt.setString(3, stop.getStopType().name());
                stopStmt.setInt(4, getSiteIdByName(stop.getSite().getSiteName()));
                stmtSetNullableString(stopStmt, 5,
                        stop.getPlannedArrival() == null ? null : stop.getPlannedArrival().toString());

                stopStmt.executeUpdate();

                int stopId;
                try (ResultSet keys = stopStmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Failed to get generated stop_id");
                    }
                    stopId = keys.getInt(1);
                }

                if (stop.getDocument() != null) {
                    insertDocument(stopId, stop.getDocument());
                }
            }
        }
    }

    private void insertDocument(int stopId, DeliveryDocumentDto document) throws SQLException {
        String sql = """
                INSERT OR REPLACE INTO delivery_documents (
                    document_number,
                    stop_id
                )
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, document.getDocumentNumber());
            stmt.setInt(2, stopId);
            stmt.executeUpdate();
        }

        insertItems(document);
    }

    private void insertItems(DeliveryDocumentDto document) throws SQLException {
        String sql = """
                INSERT OR REPLACE INTO delivery_items (
                    item_id,
                    document_number,
                    item_name,
                    quantity
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (DeliveryItemDto item : document.getItems()) {
                stmt.setString(1, item.getItemId());
                stmt.setInt(2, document.getDocumentNumber());
                stmt.setString(3, item.getItemName());
                stmt.setInt(4, item.getQuantity());
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    private void insertMeasurements(DeliveryDto delivery) throws SQLException {
        if (delivery.getDeliveryForm() == null) {
            return;
        }

        String sql = """
                INSERT INTO delivery_form_measurements (
                    delivery_id,
                    measured_weight
                )
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Double weight : delivery.getDeliveryForm().getWeightMeasurements()) {
                stmt.setInt(1, delivery.getDeliveryId());
                stmt.setDouble(2, weight);
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    @Override
    public DeliveryDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT *
                FROM deliveries
                WHERE delivery_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int deliveryId = rs.getInt("delivery_id");

                return new DeliveryDto(
                        deliveryId,
                        LocalDate.parse(rs.getString("delivery_date")),
                        findSiteById(rs.getInt("source_site_id")),
                        findStops(deliveryId),
                        LocalTime.parse(rs.getString("departure_time")),
                        rs.getDouble("final_measured_weight"),
                        findTruckByLicense(rs.getString("truck_license_number")),
                        findDriverById(rs.getString("driver_employee_id")),
                        findShippingZoneByCode(rs.getString("zone_code")),
                        DeliveryStatus.valueOf(rs.getString("status")),
                        findDeliveryForm(deliveryId)
                );
            }

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find delivery " + id, e);
        }
    }

    @Override
    public void update(DeliveryDto delivery) throws RepositoryException {
        createOrUpdate(delivery);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        int deliveryId = Integer.parseInt(id);

        try {
            connection.setAutoCommit(false);

            deleteDeliveryChildren(deliveryId);

            try (PreparedStatement stmt =
                         connection.prepareStatement("DELETE FROM deliveries WHERE delivery_id = ?")) {
                stmt.setInt(1, deliveryId);
                stmt.executeUpdate();
            }

            connection.commit();

        } catch (SQLException e) {
            rollback();
            throw new RepositoryException("Failed to delete delivery " + id, e);
        } finally {
            restoreAutoCommit();
        }
    }

    @Override
    public List<DeliveryDto> findAll() throws RepositoryException {
        List<DeliveryDto> deliveries = new ArrayList<>();

        String sql = "SELECT delivery_id FROM deliveries ORDER BY delivery_id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DeliveryDto delivery = findbyId(String.valueOf(rs.getInt("delivery_id")));
                if (delivery != null) {
                    deliveries.add(delivery);
                }
            }

            return deliveries;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to load deliveries", e);
        }
    }

    private List<DeliveryStopDto> findStops(int deliveryId) throws SQLException {
        String sql = """
                SELECT *
                FROM delivery_stops
                WHERE delivery_id = ?
                ORDER BY stop_order
                """;

        List<DeliveryStopDto> stops = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deliveryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int stopId = rs.getInt("stop_id");

                    String plannedArrival = rs.getString("planned_arrival");

                    stops.add(new DeliveryStopDto(
                            rs.getInt("stop_order"),
                            StopType.valueOf(rs.getString("stop_type")),
                            findSiteById(rs.getInt("site_id")),
                            plannedArrival == null ? null : LocalDateTime.parse(plannedArrival),
                            findDocumentByStopId(stopId)
                    ));
                }
            }
        }

        return stops;
    }

    private DeliveryDocumentDto findDocumentByStopId(int stopId) throws SQLException {
        String sql = """
                SELECT document_number
                FROM delivery_documents
                WHERE stop_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, stopId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int documentNumber = rs.getInt("document_number");

                return new DeliveryDocumentDto(
                        documentNumber,
                        findItemsByDocumentNumber(documentNumber)
                );
            }
        }
    }

    private List<DeliveryItemDto> findItemsByDocumentNumber(int documentNumber) throws SQLException {
        String sql = """
                SELECT *
                FROM delivery_items
                WHERE document_number = ?
                """;

        List<DeliveryItemDto> items = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, documentNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new DeliveryItemDto(
                            rs.getString("item_id"),
                            rs.getInt("document_number"),
                            rs.getString("item_name"),
                            rs.getInt("quantity")
                    ));
                }
            }
        }

        return items;
    }

    private DeliveryFormDto findDeliveryForm(int deliveryId) throws SQLException {
        String sql = """
                SELECT measured_weight
                FROM delivery_form_measurements
                WHERE delivery_id = ?
                ORDER BY measurement_id
                """;

        List<Double> measurements = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deliveryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    measurements.add(rs.getDouble("measured_weight"));
                }
            }
        }

        return new DeliveryFormDto(measurements);
    }

    private SiteDto findSiteById(int siteId) throws SQLException {
        String sql = """
                SELECT s.*, z.zone_name
                FROM sites s
                JOIN shipping_zones z ON s.zone_code = z.zone_code
                WHERE s.site_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, siteId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Site not found: " + siteId);
                }

                ShippingZoneDto zone = new ShippingZoneDto(
                        rs.getString("zone_code"),
                        rs.getString("zone_name")
                );

                return new SiteDto(
                        rs.getInt("site_id"),
                        rs.getString("site_name"),
                        rs.getString("address"),
                        rs.getString("contact_name"),
                        rs.getString("phone_number"),
                        zone,
                        parseSiteType(rs.getString("site_type")),
                        null
                );
            }
        }
    }

    private int getSiteIdByName(String siteName) throws SQLException {
        String sql = "SELECT site_id FROM sites WHERE site_name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, siteName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Site not found: " + siteName);
                }

                return rs.getInt("site_id");
            }
        }
    }

    private TruckDto findTruckByLicense(String licenseNumber) throws SQLException {
        String sql = "SELECT * FROM trucks WHERE license_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, licenseNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Truck not found: " + licenseNumber);
                }

                return new TruckDto(
                        rs.getString("license_number"),
                        rs.getString("model"),
                        rs.getDouble("net_weight"),
                        rs.getDouble("max_allowed_weight"),
                        LicenseType.valueOf(rs.getString("required_license_type"))
                );
            }
        }
    }

    private DriverDto findDriverById(String employeeId) throws SQLException {
        String sql = """
                SELECT d.driver_name, l.license_type
                FROM drivers d
                LEFT JOIN driver_license_types l
                    ON d.employee_id = l.employee_id
                WHERE d.employee_id = ?
                """;

        Set<LicenseType> licenses = new HashSet<>();
        String driverName = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (driverName == null) {
                        driverName = rs.getString("driver_name");
                    }

                    String license = rs.getString("license_type");
                    if (license != null) {
                        licenses.add(LicenseType.valueOf(license));
                    }
                }
            }
        }

        if (driverName == null) {
            throw new SQLException("Driver not found: " + employeeId);
        }

        return new DriverDto(employeeId, driverName, licenses);
    }

    private ShippingZoneDto findShippingZoneByCode(String zoneCode) throws SQLException {
        String sql = "SELECT * FROM shipping_zones WHERE zone_code = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, zoneCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Shipping zone not found: " + zoneCode);
                }

                return new ShippingZoneDto(
                        rs.getString("zone_code"),
                        rs.getString("zone_name")
                );
            }
        }
    }

    private SiteType parseSiteType(String value) {
        if (value == null || value.isBlank()) {
            return SiteType.REGULAR;
        }

        return SiteType.valueOf(value);
    }

    private void stmtSetNullableString(
            PreparedStatement stmt,
            int index,
            String value) throws SQLException {

        if (value == null) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value);
        }
    }

    private void rollback() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restoreAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}