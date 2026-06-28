package transportation.test;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.impl.EmployeeRepositoryImpl;
import dataaccess.repository.impl.ShiftRepositoryImpl;
import employee.domain.*;
import employee.service.EmployeeTransportationService;
import org.junit.jupiter.api.*;
import transportation.domain.*;
import transportation.service.DeliveriesApplication;

import java.nio.file.*;
import java.sql.Connection;
import java.sql.Statement;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

public class TransportationEmployeeBrutalIntegrationTest {
    private Path dbPath;
    private ShiftRepositoryImpl shiftRepository;
    private EmployeeRepositoryImpl employeeRepository;
    private EmployeeTransportationService service;
    private DeliveriesApplication app;

    private static final LocalDate DATE = LocalDate.of(2026, 4, 20);
    private static final String DRIVER_ID = "100000001";

    @BeforeEach
    void setUp() throws Exception {
        dbPath = Path.of("data", "brutal_transport_employee_" + System.nanoTime() + ".db");
        System.setProperty("adss.db.path", dbPath.toString());
        Files.deleteIfExists(dbPath);
        DatabaseInitializer.initializeDatabase();
        seedBranch();

        shiftRepository = new ShiftRepositoryImpl();
        employeeRepository = new EmployeeRepositoryImpl();
        service = new EmployeeTransportationService(shiftRepository, employeeRepository);
        app = new DeliveriesApplication(service);

        ShippingZone zone = new ShippingZone("NORTH", "North");
        app.addShippingZone(zone);
        app.addSite(new Site("Warehouse", "A", "03", "Manager", zone));
        app.addSite(new Site("Store", "B", "03", "Contact", zone));
        app.addTruck(new Truck("111-22-333", "Volvo", 5000, 10000, LicenseType.C));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.clearProperty("adss.db.path");
        if (dbPath != null) {
            Files.deleteIfExists(dbPath);
            Files.deleteIfExists(Path.of(dbPath + "-wal"));
            Files.deleteIfExists(Path.of(dbPath + "-shm"));
        }
    }

    @Test
    void shiftBoundary_0559_hasNoActiveShiftAndThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getShiftByDateTime(DATE.atTime(5, 59)));
    }

    @Test
    void shiftBoundary_0600_mapsToMorningShift() throws RepositoryException {
        Shift shift = saveShift(DATE, ShiftType.MORNING);

        assertSameShift(shift, service.getShiftByDateTime(DATE.atTime(6, 0)));
    }

    @Test
    void shiftBoundary_1359_stillMorning() throws RepositoryException {
        Shift shift = saveShift(DATE, ShiftType.MORNING);

        assertSameShift(shift, service.getShiftByDateTime(DATE.atTime(13, 59)));
    }

    @Test
    void shiftBoundary_1400_mapsToMorningOvertime() throws RepositoryException {
        Shift shift = saveShift(DATE, ShiftType.MORNING_OVERTIME);

        assertSameShift(shift, service.getShiftByDateTime(DATE.atTime(14, 0)));
    }

    @Test
    void shiftBoundary_1559_stillMorningOvertime() throws RepositoryException {
        Shift shift = saveShift(DATE, ShiftType.MORNING_OVERTIME);

        assertSameShift(shift, service.getShiftByDateTime(DATE.atTime(15, 59)));
    }

    @Test
    void shiftBoundary_1600_mapsToEvening() throws RepositoryException {
        Shift shift = saveShift(DATE, ShiftType.EVENING);

        assertSameShift(shift, service.getShiftByDateTime(DATE.atTime(16, 0)));
    }

    @Test
    void shiftBoundary_2159_stillEvening() throws RepositoryException {
        Shift shift = saveShift(DATE, ShiftType.EVENING);

        assertSameShift(shift, service.getShiftByDateTime(DATE.atTime(21, 59)));
    }

    @Test
    void shiftBoundary_2200_hasNoActiveShiftAndThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getShiftByDateTime(DATE.atTime(22, 0)));
    }

    @Test
    void createAssignmentRequest_at1400_usesMorningOvertimeShiftType() {
        service.createDriverAssignmentRequest(DRIVER_ID, 77, DATE.atTime(14, 0));

        DriverAssignmentRequest request = service.getAllDriverAssignmentRequests().get(0);
        assertEquals(ShiftType.MORNING_OVERTIME, request.getShiftType());
    }

    @Test
    void createAssignmentRequest_at2200_throwsAndDoesNotCreateRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createDriverAssignmentRequest(DRIVER_ID, 77, DATE.atTime(22, 0)));

        assertTrue(service.getAllDriverAssignmentRequests().isEmpty());
    }

    @Test
    void availableDrivers_fixedDayOffSameDay_excludedEvenWithoutAvailabilityRequest()
            throws RepositoryException {
        Employee driver = driverEmployee(DRIVER_ID, false, DayOfWeek.MONDAY, null);
        employeeRepository.save(driver);

        List<Employee> available = service.getAvailableDriversForDelivery(DATE.atTime(10, 0));

        assertTrue(available.isEmpty());
    }

    @Test
    void availableDrivers_fixedDayOffWinsEvenWhenEmployeePrefersThatShift()
            throws RepositoryException {
        WeeklyAvailabilityRequest availability = new WeeklyAvailabilityRequest();
        availability.addPreference(new Preference(DayOfWeek.MONDAY, ShiftType.MORNING));

        Employee driver = driverEmployee(DRIVER_ID, false, DayOfWeek.MONDAY, availability);
        employeeRepository.save(driver);

        List<Employee> available = service.getAvailableDriversForDelivery(DATE.atTime(10, 0));

        assertTrue(available.isEmpty());
    }

    @Test
    void availableDrivers_eveningConstraintDoesNotBlockMorningDelivery()
            throws RepositoryException {
        WeeklyAvailabilityRequest availability = new WeeklyAvailabilityRequest();
        availability.addConstraint(new Constraint(DayOfWeek.MONDAY, ShiftType.EVENING));

        Employee driver = driverEmployee(DRIVER_ID, false, null, availability);
        employeeRepository.save(driver);

        List<Employee> available = service.getAvailableDriversForDelivery(DATE.atTime(10, 0));

        assertEquals(1, available.size());
        assertEquals(DRIVER_ID, available.get(0).getId());
    }

    @Test
    void availableDrivers_returnedListIsUnmodifiable() throws RepositoryException {
        employeeRepository.save(driverEmployee(DRIVER_ID, false, null, null));

        List<Employee> available = service.getAvailableDriversForDelivery(DATE.atTime(10, 0));

        assertThrows(UnsupportedOperationException.class, available::clear);
    }

    @Test
    void openAssignmentRequests_returnedListIsUnmodifiable() {
        service.createDriverAssignmentRequest(DRIVER_ID, 1, DATE.atTime(10, 0));

        assertThrows(UnsupportedOperationException.class,
                () -> service.getOpenDriverAssignmentRequests().clear());
    }

    @Test
    void allAssignmentRequests_returnedListIsUnmodifiable() {
        service.createDriverAssignmentRequest(DRIVER_ID, 1, DATE.atTime(10, 0));

        assertThrows(UnsupportedOperationException.class,
                () -> service.getAllDriverAssignmentRequests().clear());
    }

    @Test
    void driverAssignmentCancellationRequested_driverIsNotAssignedToShift()
            throws RepositoryException {
        Employee driver = driverEmployee(DRIVER_ID, false, null, null);
        employeeRepository.save(driver);

        Shift shift = saveShift(DATE, ShiftType.MORNING);
        ShiftAssignment assignment = new ShiftAssignment(driver, shift, Role.DRIVER);
        assignment.setApproved(true);
        assignment.setCancellationRequested(true);
        shift.addAssignment(assignment);
        shiftRepository.save(shift);

        assertFalse(service.isDriverAssignedToShift(DRIVER_ID, DATE.atTime(10, 0)));
    }

    @Test
    void storekeeperCancellationRequested_dispatchFails()
            throws RepositoryException {
        Employee driver = driverEmployee(DRIVER_ID, false, null, null);
        Employee storekeeper = storekeeperEmployee("100000002", false);
        employeeRepository.save(driver);
        employeeRepository.save(storekeeper);

        Shift shift = saveShift(DATE, ShiftType.MORNING);
        assign(shift, driver, Role.DRIVER, true, false);
        assign(shift, storekeeper, Role.STOREKEEPER, true, true);

        Driver transportDriver = transportDriver(DRIVER_ID, LicenseType.C);
        app.addDriver(transportDriver);

        Delivery delivery = plan(transportDriver, LocalTime.of(10, 0));

        assertThrows(IllegalStateException.class, () -> app.dispatchDelivery(delivery));
    }

    @Test
    void legacyDriver_dispatchSkipsEmployeeShiftAndStorekeeperValidation()
            throws RepositoryException {
        Driver legacy = new Driver("Legacy Driver", Set.of(LicenseType.C));
        app.addDriver(legacy);

        Delivery delivery = plan(legacy, LocalTime.of(10, 0));

        assertDoesNotThrow(() -> app.dispatchDelivery(delivery));
        assertEquals(DeliveryStatus.DISPATCHED, delivery.getStatus());
    }

    @Test
    void nonLegacyDriver_withoutShift_dispatchFailsEvenIfDeliveryWasPlanned()
            throws RepositoryException {
        employeeRepository.save(driverEmployee(DRIVER_ID, false, null, null));
        Driver driver = transportDriver(DRIVER_ID, LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = plan(driver, LocalTime.of(10, 0));

        assertThrows(IllegalStateException.class, () -> app.dispatchDelivery(delivery));
        assertEquals(DeliveryStatus.PLANNED, delivery.getStatus());
    }

    @Test
    void failedReplaceDriver_wrongLicense_doesNotCreateExtraAssignmentRequest()
            throws RepositoryException {
        employeeRepository.save(driverEmployee(DRIVER_ID, false, null, null));
        employeeRepository.save(driverEmployee("100000002", false, null, null));

        Driver original = transportDriver(DRIVER_ID, LicenseType.C);
        Driver badReplacement = transportDriver("100000002", LicenseType.B);
        app.addDriver(original);
        app.addDriver(badReplacement);

        Delivery delivery = plan(original, LocalTime.of(10, 0));
        int before = service.getAllDriverAssignmentRequests().size();

        assertThrows(IllegalArgumentException.class,
                () -> app.replaceDriver(delivery, badReplacement));

        assertSame(original, delivery.getDriver());
        assertEquals(before, service.getAllDriverAssignmentRequests().size());
    }

    @Test
    void failedReplaceTruck_tooLightForCurrentWeight_keepsOriginalTruck()
            throws RepositoryException {
        employeeRepository.save(driverEmployee(DRIVER_ID, false, null, null));
        Driver driver = transportDriver(DRIVER_ID, LicenseType.C);
        app.addDriver(driver);

        Truck original = app.getTruckByIndex(0);
        Truck tooLight = new Truck("999-88-777", "Small", 3000, 4000, LicenseType.C);
        app.addTruck(tooLight);

        Delivery delivery = plan(driver, LocalTime.of(10, 0));

        assertThrows(IllegalArgumentException.class,
                () -> app.replaceTruck(delivery, tooLight));

        assertSame(original, delivery.getTruck());
    }

    @Test
    void cancelledDelivery_cannotBeCancelledAgainButCanStillBeModifiedBugDetector()
            throws RepositoryException {
        Driver legacy = new Driver("Legacy Driver", Set.of(LicenseType.C));
        app.addDriver(legacy);
        Delivery delivery = plan(legacy, LocalTime.of(10, 0));

        app.cancelDelivery(delivery);

        assertEquals(DeliveryStatus.CANCELLED, delivery.getStatus());

        // אם זה עובר, יש באג עסקי: משלוח מבוטל עדיין ניתן לשינוי כי canStillBeModified
        // בודק רק DISPATCHED.
        assertThrows(IllegalStateException.class,
                () -> app.recordWeightMeasurement(delivery, 7000));
    }

    @Test
    void addStopBeforeDeparture_throwsAndDoesNotChangeStops()
            throws RepositoryException {
        Driver legacy = new Driver("Legacy Driver", Set.of(LicenseType.C));
        app.addDriver(legacy);
        Delivery delivery = plan(legacy, LocalTime.of(10, 0));

        int before = delivery.getStops().size();

        DeliveryStop earlyStop = new DeliveryStop(
                99,
                StopType.DROPOFF,
                app.findSiteByName("Store"),
                DATE.atTime(9, 59));

        assertThrows(IllegalArgumentException.class,
                () -> app.addStopToDelivery(delivery, earlyStop));

        assertEquals(before, delivery.getStops().size());
    }

    @Test
    void dispatchWithOneStopAt2200_throwsBecauseNoActiveStorekeeperShift()
            throws RepositoryException {
        Employee driverEmployee = driverEmployee(DRIVER_ID, false, null, null);
        Employee storekeeper = storekeeperEmployee("100000002", false);
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift morning = saveShift(DATE, ShiftType.MORNING);
        assign(morning, driverEmployee, Role.DRIVER, true, false);
        assign(morning, storekeeper, Role.STOREKEEPER, true, false);

        Driver driver = transportDriver(DRIVER_ID, LicenseType.C);
        app.addDriver(driver);

        List<DeliveryStop> stops = List.of(
                new DeliveryStop(0, StopType.PICKUP, app.findSiteByName("Warehouse"), DATE.atTime(10, 30)),
                new DeliveryStop(1, StopType.DROPOFF, app.findSiteByName("Store"), DATE.atTime(22, 0)));

        Delivery delivery = app.planDelivery(
                DATE, app.findSiteByName("Warehouse"), stops,
                LocalTime.of(10, 0), 6000,
                app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        assertThrows(IllegalArgumentException.class,
                () -> app.dispatchDelivery(delivery));
    }

    @Test
    void validateWarehouseWorkerForArrival_nullSite_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> app.getDeliveryManager().validateWarehouseWorkerForArrival(null, DATE.atTime(10, 0)));
    }

    @Test
    void validateWarehouseWorkerForArrival_nullDateTime_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> app.getDeliveryManager().validateWarehouseWorkerForArrival(
                        app.findSiteByName("Warehouse"), null));
    }

    @Test
    void validateWarehouseWorkerForArrival_storekeeperExists_returnsTrue()
            throws RepositoryException {
        Employee storekeeper = storekeeperEmployee("100000002", false);
        employeeRepository.save(storekeeper);

        Shift shift = saveShift(DATE, ShiftType.MORNING);
        assign(shift, storekeeper, Role.STOREKEEPER, true, false);

        assertTrue(app.getDeliveryManager().validateWarehouseWorkerForArrival(
                app.findSiteByName("Warehouse"),
                DATE.atTime(10, 0)));
    }

    @Test
    void duplicateDriverNameDifferentEmployeeId_isRejectedBecauseTransportUsesNameUniqueness() {
        Driver d1 = transportDriver("100000001", LicenseType.C);
        Driver d2 = new Driver("100000002", d1.getDriverName(), Set.of(LicenseType.C));

        app.addDriver(d1);

        assertThrows(IllegalArgumentException.class, () -> app.addDriver(d2));
    }

    private Delivery plan(Driver driver, LocalTime time) {
        return app.planDelivery(
                DATE,
                app.findSiteByName("Warehouse"),
                stops(time),
                time,
                6000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));
    }

    private List<DeliveryStop> stops(LocalTime departure) {
        return List.of(
                new DeliveryStop(0, StopType.PICKUP, app.findSiteByName("Warehouse"),
                        DATE.atTime(departure).plusMinutes(30)),
                new DeliveryStop(1, StopType.DROPOFF, app.findSiteByName("Store"),
                        DATE.atTime(departure).plusHours(2)));
    }

    private Driver transportDriver(String id, LicenseType... licenses) {
        return new Driver(id, "Driver " + id, Set.of(licenses));
    }

    private Shift saveShift(LocalDate date, ShiftType type) throws RepositoryException {
        Shift shift = new Shift(date, type, managerEmployee(), 5, 5);
        shiftRepository.save(shift);
        return shift;
    }

    private void assign(Shift shift, Employee employee, Role role, boolean approved, boolean cancellationRequested)
            throws RepositoryException {
        ShiftAssignment assignment = new ShiftAssignment(employee, shift, role);
        assignment.setApproved(approved);
        assignment.setCancellationRequested(cancellationRequested);
        shift.addAssignment(assignment);
        shiftRepository.save(shift);
    }

    private void assertSameShift(Shift expected, Shift actual) {
        assertNotNull(actual);
        assertEquals(expected.getDate(), actual.getDate());
        assertEquals(expected.getShiftType(), actual.getShiftType());
    }

    private Employee managerEmployee() {
        return new Employee(
                "200000001", "managerPass",
                new BankAccount("10", "200", "200000001"),
                "Sara Manager",
                new Salary(15000, 75, 190),
                EmploymentType.REGULAR,
                new EmploymentTerms(LocalDate.of(2026, 7, 1), EmploymentScope.FULL_TIME, 15000, 75, 14),
                Set.of(Role.CASHIER),
                true,
                false,
                null,
                null,
                new Branch("1", "North Branch", "Haifa"));
    }

    private Employee driverEmployee(String id, boolean fired, DayOfWeek fixedDayOff,
            WeeklyAvailabilityRequest availability) {
        return new Employee(
                id, "password123",
                new BankAccount("10", "100", id),
                "Moshe Cohen",
                new Salary(10000, 50, 190),
                EmploymentType.REGULAR,
                new EmploymentTerms(LocalDate.of(2026, 7, 1), EmploymentScope.FULL_TIME, 10000, 50, 14),
                Set.of(Role.DRIVER),
                false,
                fired,
                fixedDayOff,
                availability,
                null);
    }

    private Employee storekeeperEmployee(String id, boolean fired) {
        return new Employee(
                id, "password123",
                new BankAccount("10", "100", id),
                "Store Keeper",
                new Salary(8000, 40, 180),
                EmploymentType.REGULAR,
                new EmploymentTerms(LocalDate.of(2026, 7, 1), EmploymentScope.FULL_TIME, 8000, 40, 14),
                Set.of(Role.STOREKEEPER),
                false,
                fired,
                null,
                null,
                new Branch("1", "North Branch", "Haifa"));
    }

    private void seedBranch() throws Exception {
        try (Connection connection = DatabaseConnection.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT OR IGNORE INTO branches (branch_id, branch_name, address)
                    VALUES (1, 'North Branch', 'Haifa')
                    """);
        }
    }
}