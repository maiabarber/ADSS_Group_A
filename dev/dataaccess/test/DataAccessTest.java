package dataaccess.test;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.DatabaseSeeder;
import dataaccess.dao.EmployeeDataAccess;
import dataaccess.dao.EmployeeWriteDataAccess;
import dataaccess.dao.TransportationDataAccess;
import dataaccess.dao.TransportationWriteDataAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// These tests check the SQLite DataAccess layer.
// Each test uses a temporary database file, so the real database is not changed.
public class DataAccessTest {
    private Path testDatabasePath;

    @BeforeEach
    public void setUp() throws Exception {
        testDatabasePath = Path.of("data", "test_adss_" + System.nanoTime() + ".db");
        System.setProperty("adss.db.path", testDatabasePath.toString());

        Files.deleteIfExists(testDatabasePath);

        DatabaseInitializer.initializeDatabase();
        DatabaseSeeder.seedSampleData();
    }

    @AfterEach
    public void tearDown() throws Exception {
        System.clearProperty("adss.db.path");

        if (testDatabasePath != null) {
            Files.deleteIfExists(testDatabasePath);
            Files.deleteIfExists(Path.of(testDatabasePath.toString() + "-wal"));
            Files.deleteIfExists(Path.of(testDatabasePath.toString() + "-shm"));
        }
    }

    @Test
    public void databasePath_canBeOverriddenForTests() {
        assertEquals(testDatabasePath.toString(), DatabaseConnection.getDatabasePath());
    }

    @Test
    public void initializer_createsEmployeeAndTransportationTables() throws Exception {
        assertTrue(tableExists("employees"));
        assertTrue(tableExists("branches"));
        assertTrue(tableExists("shifts"));
        assertTrue(tableExists("sites"));
        assertTrue(tableExists("trucks"));
        assertTrue(tableExists("drivers"));
        assertTrue(tableExists("deliveries"));
        assertTrue(tableExists("delivery_items"));
    }

    @Test
    public void seeder_insertsThreeEmployees() throws Exception {
        EmployeeDataAccess reader = new EmployeeDataAccess();

        assertEquals(3, reader.countEmployees());
        assertEquals(3, countRows("employees"));
    }

    @Test
    public void seeder_insertsOneDelivery() throws Exception {
        TransportationDataAccess reader = new TransportationDataAccess();

        assertEquals(1, reader.countDeliveries());
        assertEquals(1, countRows("deliveries"));
    }

    @Test
    public void seeder_insertsBasicTransportationData() throws Exception {
        assertEquals(2, countRows("shipping_zones"));
        assertEquals(3, countRows("sites"));
        assertEquals(2, countRows("trucks"));
        assertEquals(1, countRows("drivers"));
    }

    @Test
    public void employeeWriter_insertsBranchUserEmployeeAndRole() throws Exception {
        EmployeeWriteDataAccess writer = new EmployeeWriteDataAccess();

        writer.insertBranch(20, "Eilat", "HaTmarim 20, Eilat");
        writer.insertUser("100000020", "1234", false);
        writer.insertEmployee(
                "100000020",
                "New Cashier",
                "444-444",
                "HOURLY",
                "PART_TIME",
                40,
                0,
                "2026-07-01",
                false,
                10,
                20
        );
        writer.insertEmployeeRole("100000020", "CASHIER");

        assertEquals(3, countRows("branches"));
        assertEquals(4, countRows("users"));
        assertEquals(4, countRows("employees"));
        assertEquals(1, countRowsWhere("employee_roles", "employee_id = '100000020' AND role_name = 'CASHIER'"));
    }

    @Test
    public void employeeWriter_insertsShiftAndAssignment() throws Exception {
        EmployeeWriteDataAccess writer = new EmployeeWriteDataAccess();

        writer.insertBranch(30, "Ashdod", "Sea 1, Ashdod");
        writer.insertUser("100000030", "1234", false);
        writer.insertEmployee(
                "100000030",
                "Shift Worker",
                "555-555",
                "HOURLY",
                "FULL_TIME",
                45,
                0,
                "2026-07-01",
                false,
                10,
                30
        );
        writer.insertEmployeeRole("100000030", "STOREKEEPER");
        writer.insertShift(30, "2026-07-03", "MORNING", 30);
        writer.insertShiftAssignment(30, 30, "100000030", "STOREKEEPER", "APPROVED");

        assertEquals(3, countRows("shifts"));
        assertEquals(3, countRows("shift_assignments"));
    }

    @Test
    public void transportationWriter_insertsZoneSiteTruckDriverAndLicense() throws Exception {
        TransportationWriteDataAccess writer = new TransportationWriteDataAccess();

        writer.insertShippingZone("NORTH", "North Zone");
        writer.insertSite(10, "Haifa Branch", "HaNamal 5, Haifa", "Yossi", "050-4444444", "NORTH", "BRANCH");
        writer.insertTruck("222-22-222", "Mercedes Actros", 9000, 20000, "C");
        writer.insertDriver("100000010", "Amit Driver");
        writer.insertDriverLicenseType("100000010", "C");

        assertEquals(3, countRows("shipping_zones"));
        assertEquals(4, countRows("sites"));
        assertEquals(3, countRows("trucks"));
        assertEquals(2, countRows("drivers"));
        assertEquals(3, countRows("driver_license_types"));
    }

    @Test
    public void transportationWriter_insertsDeliveryWithStopsDocumentAndItem() throws Exception {
        TransportationWriteDataAccess writer = new TransportationWriteDataAccess();

        writer.insertShippingZone("NORTH", "North Zone");
        writer.insertSite(10, "Haifa Branch", "HaNamal 5, Haifa", "Yossi", "050-4444444", "NORTH", "BRANCH");
        writer.insertSite(11, "North Supplier", "Industrial Area, Haifa", "Maya", "050-5555555", "NORTH", "SUPPLIER");
        writer.insertTruck("222-22-222", "Mercedes Actros", 9000, 20000, "C");
        writer.insertDriver("100000010", "Amit Driver");
        writer.insertDriverLicenseType("100000010", "C");

        writer.insertDelivery(2, "2026-07-02", 11, "09:00", 10000, "222-22-222", "100000010", "NORTH", "PLANNED");
        writer.insertDeliveryStop(10, 2, 1, "PICKUP", 11, "2026-07-02T09:30");
        writer.insertDeliveryStop(11, 2, 2, "DROPOFF", 10, "2026-07-02T11:00");
        writer.insertDeliveryDocument(2, 10);
        writer.insertDeliveryItem("ITEM-NEW-1", 2, "Water Bottles", 100);
        writer.insertDeliveryFormMeasurement(2, 10000);

        TransportationDataAccess reader = new TransportationDataAccess();

        assertEquals(2, reader.countDeliveries());
        assertEquals(4, countRows("delivery_stops"));
        assertEquals(2, countRows("delivery_documents"));
        assertEquals(3, countRows("delivery_items"));
        assertEquals(2, countRows("delivery_form_measurements"));
    }

    @Test
    public void duplicateInsert_isIgnoredAndDoesNotCreateDuplicateRows() throws Exception {
        TransportationWriteDataAccess writer = new TransportationWriteDataAccess();

        writer.insertShippingZone("SOUTH", "South Zone");
        writer.insertShippingZone("SOUTH", "South Zone");

        assertEquals(1, countRowsWhere("shipping_zones", "zone_code = 'SOUTH'"));
    }

    @Test
    public void temporaryDatabase_doesNotUseMainDatabasePath() {
        assertTrue(DatabaseConnection.getDatabasePath().contains("test_adss_"));
        assertTrue(!DatabaseConnection.getDatabasePath().equals("data/adss_group_a.db"));
    }

    private boolean tableExists(String tableName) throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM sqlite_master WHERE type = 'table' AND name = '" + tableName + "'";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            return resultSet.next() && resultSet.getInt("total") == 1;
        }
    }

    private int countRows(String tableName) throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM " + tableName;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

            return 0;
        }
    }

    private int countRowsWhere(String tableName, String whereClause) throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM " + tableName + " WHERE " + whereClause;

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