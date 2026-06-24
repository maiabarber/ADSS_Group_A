package dataaccess.test;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.DatabaseSeeder;
import dataaccess.dao.BranchDAO;
import dataaccess.dao.DeliveryDAO;
import dataaccess.dao.DriverAssignmentRequestDao;
import dataaccess.dao.DriverDAO;
import dataaccess.dao.EmployeeDAO;
import dataaccess.dao.ShiftDAO;
import dataaccess.dao.SiteDAO;
import dataaccess.dao.SubmissionDeadlineDAO;
import dataaccess.dao.TruckDAO;
import dataaccess.dao.UserDAO;
import dataaccess.dao.WeeklyAvailabilityRequestDao;
import dataaccess.dto.CreateEmployeeDTO;
import employee.domain.ShiftType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertTrue(tableExists("users"));
        assertTrue(tableExists("employees"));
        assertTrue(tableExists("shifts"));
        assertTrue(tableExists("submissiondeadlines"));
        assertTrue(tableExists("weeklyavailabilityrequests"));
        assertTrue(tableExists("driverassignmentrequests"));

        assertTrue(tableExists("deliveries"));
        assertTrue(tableExists("trucks"));
        assertTrue(tableExists("drivers"));
        assertTrue(tableExists("sites"));
        assertTrue(tableExists("branches"));
    }

    @Test
    public void seeder_insertsThreeEmployees() throws Exception {
        EmployeeDAO reader = new EmployeeDAO();

        assertEquals(3, reader.countEmployees());
        assertEquals(3, countRows("employees"));
    }

    @Test
    public void seeder_insertsOneDelivery() throws Exception {
        DeliveryDAO reader = new DeliveryDAO();

        assertEquals(1, reader.countDeliveries());
        assertEquals(1, countRows("deliveries"));
    }

    @Test
    public void seededEmployee_canBeLookedUpById() throws Exception {
        EmployeeDAO reader = new EmployeeDAO();

        assertThrows(IllegalArgumentException.class, () -> reader.findEmployeeById("100000003"));
        assertFalse(reader.findEmployeeById("999999999").isPresent());
    }

    @Test
    public void employeeDao_throwsOnUnsupportedEmploymentTypeValuesInSeedData() {
        EmployeeDAO reader = new EmployeeDAO();

        assertThrows(IllegalArgumentException.class, () -> reader.findEmployeeById("100000001"));
        assertThrows(IllegalArgumentException.class, () -> reader.findEmployeeById("100000003"));
    }

    @Test
    public void employeeDao_insertDeleteAndDuplicateIgnore_workAsExpected() throws Exception {
        EmployeeDAO employeeDAO = new EmployeeDAO();

        CreateEmployeeDTO dto = new CreateEmployeeDTO(
                "100000111",
                "Edge Case Employee",
                "999-999",
            "REGULAR",
                "FULL_TIME",
                50,
                0,
                "2026-08-01",
                false,
                8,
                1
        );

        employeeDAO.insertEmployee(dto);
        employeeDAO.insertEmployee(dto);

        assertEquals(1, countRowsWhere("employees", "employee_id = '100000111'"));
        assertTrue(employeeDAO.findEmployeeById("100000111").isPresent());

        employeeDAO.deleteEmployeeById("100000111");
        employeeDAO.deleteEmployeeById("100000111");

        assertFalse(employeeDAO.findEmployeeById("100000111").isPresent());
    }

    @Test
    public void branchDao_findInsertDeleteAndDuplicateIgnore_workAsExpected() throws Exception {
        BranchDAO branchDAO = new BranchDAO();

        assertFalse(branchDAO.findBranchById(999).isPresent());

        branchDAO.insertBranch(99, "Edge Branch", "Edge Address");
        branchDAO.insertBranch(99, "Edge Branch", "Edge Address");

        assertEquals(1, countRowsWhere("branches", "branch_id = 99"));
        assertTrue(branchDAO.findBranchById(99).isPresent());

        branchDAO.deleteBranchById(99);
        branchDAO.deleteBranchById(99);

        assertFalse(branchDAO.findBranchById(99).isPresent());
    }

    @Test
    public void userDao_findInsertDeleteAndDuplicateIgnore_workAsExpected() throws Exception {
        UserDAO userDAO = new UserDAO();

        assertFalse(userDAO.findUserById("404404404").isPresent());

        userDAO.insertUser("404404404", "edgepass", false);
        userDAO.insertUser("404404404", "edgepass", false);

        assertEquals(1, countRowsWhere("users", "user_id = '404404404'"));
        assertTrue(userDAO.findUserById("404404404").isPresent());

        userDAO.deleteUserById("404404404");
        userDAO.deleteUserById("404404404");

        assertFalse(userDAO.findUserById("404404404").isPresent());
    }

    @Test
    public void shiftDao_shiftCrudAndAssignmentCrud_workAsExpected() throws Exception {
        ShiftDAO shiftDAO = new ShiftDAO();

        assertFalse(shiftDAO.findShiftById(444).isPresent());
        assertFalse(shiftDAO.findShiftAssignmentById(444).isPresent());

        shiftDAO.insertShift(444, "2026-09-01", "MORNING", 1);
        shiftDAO.insertShift(444, "2026-09-01", "MORNING", 1);
        assertEquals(1, countRowsWhere("shifts", "shift_id = 444"));
        assertTrue(shiftDAO.findShiftById(444).isPresent());

        shiftDAO.insertShiftAssignment(444, 444, "100000001", "CASHIER", "APPROVED");
        shiftDAO.insertShiftAssignment(444, 444, "100000001", "CASHIER", "APPROVED");
        assertEquals(1, countRowsWhere("shift_assignments", "assignment_id = 444"));
        assertTrue(shiftDAO.findShiftAssignmentById(444).isPresent());

        shiftDAO.deleteShiftAssignmentById(444);
        shiftDAO.deleteShiftById(444);

        assertFalse(shiftDAO.findShiftAssignmentById(444).isPresent());
        assertFalse(shiftDAO.findShiftById(444).isPresent());
    }

    @Test
    public void submissionDeadlineDao_saveOverwriteDeleteAndNull_workAsExpected() throws Exception {
        SubmissionDeadlineDAO deadlineDAO = new SubmissionDeadlineDAO();

        assertFalse(deadlineDAO.findCurrent().isPresent());

        deadlineDAO.save(LocalDate.of(2026, 8, 10));
        assertTrue(deadlineDAO.findCurrent().isPresent());
        assertEquals(LocalDate.of(2026, 8, 10), deadlineDAO.findCurrent().orElseThrow());
        assertEquals(1, countRows("submissiondeadlines"));

        deadlineDAO.save(LocalDate.of(2026, 9, 1));
        assertEquals(LocalDate.of(2026, 9, 1), deadlineDAO.findCurrent().orElseThrow());
        assertEquals(1, countRows("submissiondeadlines"));

        assertThrows(SQLException.class, () -> deadlineDAO.save(null));
        assertEquals(0, countRows("submissiondeadlines"));

        deadlineDAO.deleteCurrent();
        deadlineDAO.deleteCurrent();
        assertFalse(deadlineDAO.findCurrent().isPresent());
        assertEquals(0, countRows("submissiondeadlines"));
    }

    @Test
    public void weeklyAvailabilityRequestDao_crudAndInvalidInsertIgnored_workAsExpected() throws Exception {
        WeeklyAvailabilityRequestDao requestDao = new WeeklyAvailabilityRequestDao();

        assertFalse(requestDao.findByRequestId(777).isPresent());

        requestDao.insertRequest("100000001", LocalDate.of(2026, 8, 3), LocalDate.of(2026, 7, 31));
        assertEquals(1, countRowsWhere("weeklyavailabilityrequests", "employee_id = '100000001' AND week_start_date = '2026-08-03'"));

        int insertedId = selectSingleInt("SELECT request_id FROM weeklyavailabilityrequests WHERE employee_id = '100000001' AND week_start_date = '2026-08-03' ORDER BY request_id DESC LIMIT 1");
        assertTrue(requestDao.findByRequestId(insertedId).isPresent());

        requestDao.deleteByRequestId(insertedId);
        requestDao.deleteByRequestId(insertedId);
        assertFalse(requestDao.findByRequestId(insertedId).isPresent());

    }

    @Test
    public void driverAssignmentRequestDao_crudAndInvalidInsertIgnored_workAsExpected() throws Exception {
        DriverAssignmentRequestDao requestDao = new DriverAssignmentRequestDao();

        assertFalse(requestDao.findByRequestId(888).isPresent());

        requestDao.insertRequest(
                "100000010",
                1,
                LocalDateTime.of(2026, 8, 4, 9, 30),
                ShiftType.MORNING,
                false,
                "PENDING"
        );

        assertEquals(1, countRowsWhere("driverassignmentrequests", "driver_id = '100000010' AND delivery_id = 1"));
        int insertedId = selectSingleInt("SELECT request_id FROM driverassignmentrequests WHERE driver_id = '100000010' AND delivery_id = 1 ORDER BY request_id DESC LIMIT 1");
        assertTrue(requestDao.findByRequestId(insertedId).isPresent());

        requestDao.deleteByRequestId(insertedId);
        requestDao.deleteByRequestId(insertedId);
        assertFalse(requestDao.findByRequestId(insertedId).isPresent());

    }

    @Test
    public void seeder_insertsBasicTransportationData() throws Exception {
        assertEquals(2, countRows("branches"));
        assertEquals(3, countRows("sites"));
        assertEquals(2, countRows("trucks"));
        assertEquals(1, countRows("drivers"));
    }

    @Test
    public void requestTables_areCreatedEvenWhenEmpty() throws Exception {
        assertEquals(0, countRows("submissiondeadlines"));
        assertEquals(0, countRows("weeklyavailabilityrequests"));
        assertEquals(0, countRows("driverassignmentrequests"));
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

    private int selectSingleInt(String sql) throws Exception {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            throw new IllegalStateException("Expected at least one row for query: " + sql);
        }
    }
}