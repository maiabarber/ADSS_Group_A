package transportation.test;

import dataaccess.DatabaseInitializer;
import dataaccess.DatabaseConnection;
import employee.domain.*;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.impl.EmployeeRepositoryImpl;
import dataaccess.repository.impl.ShiftRepositoryImpl;
import employee.service.EmployeeTransportationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import transportation.domain.*;
import transportation.service.DeliveriesApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests between the Transportation and Employee modules.
 * Tests cover delivery creation scenarios involving existing/missing employees
 * and shifts.
 */
public class TransportationEmployeeIntegrationTest {
    private Path testDatabasePath;

    // 2026-04-20 is a Monday; use a past date so that the default stop arrival
    // time (LocalDateTime.now() + 1h) is never before the delivery departure.
    private static final LocalDate DELIVERY_DATE = LocalDate.of(2026, 4, 20);
    private static final LocalTime MORNING_TIME = LocalTime.of(10, 0);
    private static final LocalTime EVENING_TIME = LocalTime.of(18, 0);
    // Employee IDs must be exactly 9 digits per User.validateId()
    private static final String DRIVER_ID = "100000001";

    private ShiftRepositoryImpl shiftRepository;
    private EmployeeRepositoryImpl employeeRepository;
    private EmployeeTransportationService employeeTransportationService;
    private DeliveriesApplication app;

    @BeforeEach
    void setUp() throws Exception {
        testDatabasePath = Path.of("data", "transport_employee_test_" + System.nanoTime() + ".db");
        System.setProperty("adss.db.path", testDatabasePath.toString());
        Files.deleteIfExists(testDatabasePath);
        DatabaseInitializer.initializeDatabase();
        seedBranchForStorekeeperTests();

        shiftRepository = new ShiftRepositoryImpl();
        employeeRepository = new EmployeeRepositoryImpl();
        employeeTransportationService = new EmployeeTransportationService(shiftRepository, employeeRepository);
        app = new DeliveriesApplication(employeeTransportationService);

        ShippingZone northZone = new ShippingZone("NORTH", "Northern Zone");
        app.addShippingZone(northZone);

        Site warehouse = new Site("Warehouse", "10 Main St", "03-1111111", "Warehouse Manager", northZone);
        Site store = new Site("Store", "20 Side St", "03-2222222", "Store Contact", northZone);
        app.addSite(warehouse);
        app.addSite(store);

        Truck truck = new Truck("123-45-678", "Volvo", 5000, 10000, LicenseType.C);
        app.addTruck(truck);
    }

    @AfterEach
    void tearDownDatabase() throws Exception {
        System.clearProperty("adss.db.path");
        if (testDatabasePath != null) {
            Files.deleteIfExists(testDatabasePath);
            Files.deleteIfExists(Path.of(testDatabasePath.toString() + "-wal"));
            Files.deleteIfExists(Path.of(testDatabasePath.toString() + "-shm"));
        }
    }

    // ─── Helper factories ────────────────────────────────────────────────────

    private Employee buildDriverEmployee(String id, boolean isFired) {
        return new Employee(
                id,
                "password123",
                new BankAccount("10", "100", "100000001"),
                "Moshe Cohen",
                new Salary(10000, 50, 190),
                EmploymentType.REGULAR,
                new EmploymentTerms(
                        LocalDate.of(2026, 7, 1),
                        EmploymentScope.FULL_TIME,
                        10000, 50, 14),
                Set.of(Role.DRIVER),
                false,
                isFired,
                null,
                null,
                null);
    }

    private Employee buildShiftManagerEmployee(String id) {
        return new Employee(
                id, // must be 9 digits
                "managerPass",
                new BankAccount("10", "200", "200000001"),
                "Sara Manager",
                new Salary(15000, 75, 190),
                EmploymentType.REGULAR,
                new EmploymentTerms(
                        LocalDate.of(2026, 7, 1),
                        EmploymentScope.FULL_TIME,
                        15000, 75, 14),
                Set.of(Role.CASHIER),
                true,
                false,
                null,
                null,
                new Branch("BR-01", "North Branch", "Haifa"));
    }

    /** Builds a MORNING shift for the given date and saves it in the repository. */
    private Shift buildAndSaveShift(LocalDate date) throws RepositoryException {
        Employee manager = buildShiftManagerEmployee("200000001");
        Shift shift = new Shift(date, ShiftType.MORNING, manager, 2, 1);
        shiftRepository.save(shift);
        return shift;
    }

    /** Adds an approved DRIVER assignment for the employee to the shift. */
    private void assignDriverToShift(Shift shift, Employee employee, boolean approved)
            throws RepositoryException {
        ShiftAssignment assignment = new ShiftAssignment(employee, shift, Role.DRIVER);
        assignment.setApproved(approved);
        shift.addAssignment(assignment);
        shiftRepository.save(shift); // re-save with updated assignment
    }

    private Driver buildTransportDriver(String employeeId, String name, LicenseType... licenses) {
        return new Driver(employeeId, name, Set.of(licenses));
    }

    private List<DeliveryStop> basicStops() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(app.createPickupStop(0, app.findSiteByName("Warehouse")));
        stops.add(app.createDropoffStop(1, app.findSiteByName("Store")));
        return stops;
    }

    // ─── Tests ───────────────────────────────────────────────────────────────

    // 1. Delivery creation with existing employee (no shift)
    @Test
    void createDelivery_employeeExistsButNoShiftCreated_deliverySucceedsAndAssignmentRequestIsTracked()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        assertNotNull(delivery);
        assertEquals(1, app.getAllDeliveries().size());

        // A driver assignment request must be created even though no shift exists yet
        List<DriverAssignmentRequest> requests = employeeTransportationService.getAllDriverAssignmentRequests();
        assertEquals(1, requests.size());
        assertEquals(DRIVER_ID, requests.get(0).getDriverId());
        assertFalse(requests.get(0).isHandled());
    }

    // 2. Delivery creation with existing shift (driver assigned and approved)
    @Test
    void createDelivery_existingShiftWithApprovedDriver_deliverySucceedsAndDriverIsAvailable()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, employee, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        // Driver should appear as available before delivery is created
        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));
        assertEquals(1, available.size());
        assertEquals(DRIVER_ID, available.get(0).getEmployeeId());

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        assertNotNull(delivery);
        assertEquals(1, app.getAllDeliveries().size());
    }

    // 3. Delivery creation with non-existing employee (driver in transport only)
    @Test
    void createDelivery_driverHasNoEmployeeRecord_deliverySucceedsButIsNotAssignedToShift()
            throws RepositoryException {
        // No employee saved in the employee repository
        Driver driver = buildTransportDriver("TRANSPORT-ONLY-001", "Yossi Driver", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        assertNotNull(delivery);

        // isDriverAssignedToShift returns false because there is no shift and no
        // employee record
        assertFalse(employeeTransportationService.isDriverAssignedToShift(
                "TRANSPORT-ONLY-001", DELIVERY_DATE.atTime(MORNING_TIME)));
    }

    // 4. getAvailableDrivers – no shift exists for the date
    @Test
    void getAvailableDrivers_noShiftExistsForDate_returnsEmpty() {
        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 5. getAvailableDrivers – shift exists but driver assignment not approved
    @Test
    void getAvailableDrivers_shiftExistsButAssignmentNotApproved_returnsEmpty()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, employee, false); // not approved

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 6. Fired employee – driver should not appear as available even if shift
    // exists
    @Test
    void getAvailableDrivers_employeeIsFired_returnsEmpty() throws RepositoryException {
        Employee firedEmployee = buildDriverEmployee(DRIVER_ID, true); // isFired = true
        employeeRepository.save(firedEmployee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, firedEmployee, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 7. Employee has a constraint for the shift day/type – excluded from
    // EmployeeTransportationService.getAvailableDriversForDelivery (the
    // employee-side availability check, not the transport availability check)
    @Test
    void getAvailableDriversFromEmployeeService_employeeHasMorningConstraintOnMonday_returnsEmpty()
            throws RepositoryException {
        WeeklyAvailabilityRequest availability = new WeeklyAvailabilityRequest();
        availability.addConstraint(new Constraint(DayOfWeek.MONDAY, ShiftType.MORNING));

        Employee employee = new Employee(
                DRIVER_ID, "password123",
                new BankAccount("10", "100", "100000001"),
                "Moshe Cohen",
                new Salary(10000, 50, 190),
                EmploymentType.REGULAR,
                new EmploymentTerms(LocalDate.of(2026, 7, 1), EmploymentScope.FULL_TIME, 10000, 50, 14),
                Set.of(Role.DRIVER),
                false, false,
                null,
                availability,
                null);
        employeeRepository.save(employee);

        // Query the employee-side service directly – it respects constraints
        List<Employee> available = employeeTransportationService.getAvailableDriversForDelivery(
                DELIVERY_DATE.atTime(MORNING_TIME)); // DELIVERY_DATE is a Monday

        assertTrue(available.isEmpty(),
                "Employee with MORNING constraint on MONDAY should not be returned by employee service");
    }

    // 7b. Employee with no constraint IS returned by employee service
    @Test
    void getAvailableDriversFromEmployeeService_employeeHasNoConstraint_returnsEmployee()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        List<Employee> available = employeeTransportationService.getAvailableDriversForDelivery(
                DELIVERY_DATE.atTime(MORNING_TIME));

        assertEquals(1, available.size());
        assertEquals(DRIVER_ID, available.get(0).getId());
    }

    // 8. Multiple deliveries – each creates its own assignment request
    @Test
    void createDelivery_twoDifferentDrivers_twoAssignmentRequestsCreated() throws RepositoryException {
        String driverId2 = "100000002";
        Employee employee1 = buildDriverEmployee(DRIVER_ID, false);
        Employee employee2 = new Employee(
                driverId2, "password456",
                new BankAccount("10", "100", "300000001"),
                "David Levy",
                new Salary(10000, 50, 190),
                EmploymentType.REGULAR,
                new EmploymentTerms(LocalDate.of(2026, 7, 1), EmploymentScope.FULL_TIME, 10000, 50, 14),
                Set.of(Role.DRIVER),
                false, false, null, null, null);
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        Driver driver1 = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        Driver driver2 = buildTransportDriver(driverId2, "David Levy", LicenseType.C);
        app.addDriver(driver1);
        app.addDriver(driver2);

        // Use a second truck so each driver has their own truck
        Truck truck2 = new Truck("987-65-432", "MAN", 4000, 9000, LicenseType.C);
        app.addTruck(truck2);

        app.planDelivery(DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver1, app.getShippingZoneByIndex(0));

        app.planDelivery(DELIVERY_DATE.plusDays(1), app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(1), driver2, app.getShippingZoneByIndex(0));

        assertEquals(2, app.getAllDeliveries().size());
        assertEquals(2, employeeTransportationService.getAllDriverAssignmentRequests().size());
    }

    // 9. Replace driver after delivery creation – new assignment request is added
    @Test
    void replaceDriver_afterDeliveryCreated_newAssignmentRequestCreated() throws RepositoryException {
        String replacementId = "100000002";
        Employee employee1 = buildDriverEmployee(DRIVER_ID, false);
        Employee employee2 = new Employee(
                replacementId, "password456",
                new BankAccount("10", "100", "300000001"),
                "Avi Replacement",
                new Salary(10000, 50, 190),
                EmploymentType.REGULAR,
                new EmploymentTerms(LocalDate.of(2026, 7, 1), EmploymentScope.FULL_TIME, 10000, 50, 14),
                Set.of(Role.DRIVER),
                false, false, null, null, null);
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        Driver driver1 = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        Driver driver2 = buildTransportDriver(replacementId, "Avi Replacement", LicenseType.C);
        app.addDriver(driver1);
        app.addDriver(driver2);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver1, app.getShippingZoneByIndex(0));

        // 1 request created for original driver
        assertEquals(1, employeeTransportationService.getAllDriverAssignmentRequests().size());

        app.replaceDriver(delivery, driver2);

        // 2nd request created for replacement driver
        assertEquals(2, employeeTransportationService.getAllDriverAssignmentRequests().size());
        assertEquals(replacementId,
                employeeTransportationService.getAllDriverAssignmentRequests().get(1).getDriverId());
    }

    // 10. isDriverAssignedToShift – correct driver/shift combination returns true
    @Test
    void isDriverAssignedToShift_approvedAssignment_returnsTrue() throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, employee, true);

        assertTrue(employeeTransportationService.isDriverAssignedToShift(
                DRIVER_ID, DELIVERY_DATE.atTime(MORNING_TIME)));
    }

    // 11. isDriverAssignedToShift – wrong employee ID returns false
    @Test
    void isDriverAssignedToShift_wrongEmployeeId_returnsFalse() throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, employee, true);

        assertFalse(employeeTransportationService.isDriverAssignedToShift(
                "NON-EXISTENT-ID", DELIVERY_DATE.atTime(MORNING_TIME)));
    }

    // 12. isDriverAssignedToShift – evening time maps to EVENING shift, not MORNING
    @Test
    void isDriverAssignedToShift_driverInMorningShift_eveningTimeReturnsFalse()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE); // MORNING shift
        assignDriverToShift(shift, employee, true);

        // Querying evening time should return false because the shift is MORNING
        assertFalse(employeeTransportationService.isDriverAssignedToShift(
                DRIVER_ID, DELIVERY_DATE.atTime(EVENING_TIME)));
    }

    // 13. getAvailableDrivers – driver without matching license type is excluded
    @Test
    void getAvailableDrivers_driverLicenseDoesNotMatchTruck_isExcluded() throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, employee, true);

        // Driver only has license B, truck requires C
        Driver driverB = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.B);
        app.addDriver(driverB);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0)); // truck requires C

        assertTrue(available.isEmpty());
    }

    // 14. Assignment request is marked handled after HR processes it
    @Test
    void markDriverAssignmentRequestHandled_requestBecomesHandled() throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        app.planDelivery(DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        List<DriverAssignmentRequest> openBefore = employeeTransportationService.getOpenDriverAssignmentRequests();
        assertEquals(1, openBefore.size());

        employeeTransportationService.markDriverAssignmentRequestHandled(openBefore.get(0));

        List<DriverAssignmentRequest> openAfter = employeeTransportationService.getOpenDriverAssignmentRequests();
        assertTrue(openAfter.isEmpty());
    }

    // 15. Multiple transport drivers: only approved + matching license is returned
    @Test
    void getAvailableDrivers_multipleDrivers_onlyApprovedAndLicensedReturned() throws RepositoryException {
        String driverId2 = "100000002";

        Employee e1 = buildDriverEmployee(DRIVER_ID, false);
        Employee e2 = buildDriverEmployee(driverId2, false);
        employeeRepository.save(e1);
        employeeRepository.save(e2);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, e1, true);
        assignDriverToShift(shift, e2, false);

        Driver approved = buildTransportDriver(DRIVER_ID, "Approved Driver", LicenseType.C);
        Driver notApproved = buildTransportDriver(driverId2, "Not Approved Driver", LicenseType.C);
        Driver wrongLicense = buildTransportDriver("100000003", "Wrong License", LicenseType.B);

        app.addDriver(approved);
        app.addDriver(notApproved);
        app.addDriver(wrongLicense);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));

        assertEquals(1, available.size());
        assertEquals(DRIVER_ID, available.get(0).getEmployeeId());
    }

    // 16. Driver with several licenses including required license is available
    @Test
    void getAvailableDrivers_driverHasMultipleLicensesIncludingRequired_isReturned()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, employee, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Multi License Driver",
                LicenseType.B, LicenseType.C);
        app.addDriver(driver);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));

        assertEquals(1, available.size());
        assertEquals(DRIVER_ID, available.get(0).getEmployeeId());
    }

    // 17. Employee is assigned in employee module, but driver was not added to
    // transport module
    @Test
    void getAvailableDrivers_employeeAssignedButNoTransportDriver_returnsEmpty()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, employee, true);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 18. Driver exists in transport module, but shift is on another date
    @Test
    void getAvailableDrivers_shiftExistsOnDifferentDate_returnsEmpty()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE.plusDays(1));
        assignDriverToShift(shift, employee, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 19. Unapproved assignment is not considered assigned to shift
    @Test
    void isDriverAssignedToShift_unapprovedAssignment_returnsFalse()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, employee, false);

        assertFalse(employeeTransportationService.isDriverAssignedToShift(
                DRIVER_ID, DELIVERY_DATE.atTime(MORNING_TIME)));
    }

    // 20. Assignment exists, but not as DRIVER role
    @Test
    void isDriverAssignedToShift_assignmentWithNonDriverRole_returnsFalse()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        ShiftAssignment cashierAssignment = new ShiftAssignment(employee, shift, Role.CASHIER);
        cashierAssignment.setApproved(true);
        shift.addAssignment(cashierAssignment);
        shiftRepository.save(shift);

        assertFalse(employeeTransportationService.isDriverAssignedToShift(
                DRIVER_ID, DELIVERY_DATE.atTime(MORNING_TIME)));
    }

    // 21. Same driver can create two delivery assignment requests
    @Test
    void createDelivery_sameDriverTwoDeliveries_twoRequestsCreated()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        app.planDelivery(DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        app.planDelivery(DELIVERY_DATE.plusDays(1), app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        assertEquals(2, employeeTransportationService.getAllDriverAssignmentRequests().size());
        assertEquals(2, employeeTransportationService.getOpenDriverAssignmentRequests().size());
    }

    // 22. Handling one request leaves the other open
    @Test
    void markOneOfTwoDriverRequestsHandled_onlySecondRemainsOpen()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        app.planDelivery(DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        app.planDelivery(DELIVERY_DATE.plusDays(1), app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        DriverAssignmentRequest first = employeeTransportationService.getOpenDriverAssignmentRequests().get(0);

        employeeTransportationService.markDriverAssignmentRequestHandled(first);

        assertEquals(1, employeeTransportationService.getOpenDriverAssignmentRequests().size());
        assertTrue(first.isHandled());
    }

    // 23. Planning delivery with driver who lacks truck license should fail
    @Test
    void planDelivery_driverWithoutRequiredLicense_throwsAndNoRequestCreated()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Driver driver = buildTransportDriver(DRIVER_ID, "Wrong License Driver", LicenseType.B);
        app.addDriver(driver);

        assertThrows(IllegalArgumentException.class,
                () -> app.planDelivery(DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                        MORNING_TIME, 6000, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0)));

        assertTrue(employeeTransportationService.getAllDriverAssignmentRequests().isEmpty());
    }

    // 24. Delivery exactly at max truck capacity succeeds
    @Test
    void planDelivery_weightExactlyAtTruckMaxCapacity_succeeds()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                basicStops(),
                MORNING_TIME,
                10000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));

        assertNotNull(delivery);
    }

    // 25. Delivery above max truck capacity fails
    @Test
    void planDelivery_weightAboveTruckMaxCapacity_throws()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(DELIVERY_DATE, app.findSiteByName("Warehouse"), basicStops(),
                MORNING_TIME, 10001, app.getTruckByIndex(0), driver, app.getShippingZoneByIndex(0));

        assertNotNull(delivery);
        assertEquals(DeliveryStatus.PENDING_REPLAN, delivery.getStatus(),
                "Overweight delivery should be created and marked for replanning");
    }

    // 26. Employee service returns only drivers, not regular non-driver employees
    @Test
    void getAvailableDriversFromEmployeeService_employeeWithoutDriverRole_isExcluded()
            throws RepositoryException {
        Branch branch = new Branch("BR-02", "Cashier Branch", "Tel Aviv");
        Employee cashier = new Employee(
                DRIVER_ID,
                "password123",
                new BankAccount("10", "100", "100000001"),
                "Cashier Employee",
                new Salary(8000, 40, 180),
                EmploymentType.REGULAR,
                new EmploymentTerms(LocalDate.of(2026, 7, 1),
                        EmploymentScope.FULL_TIME, 8000, 40, 14),
                Set.of(Role.CASHIER),
                false,
                false,
                null,
                null,
                branch);

        employeeRepository.save(cashier);

        List<Employee> available = employeeTransportationService.getAvailableDriversForDelivery(
                DELIVERY_DATE.atTime(MORNING_TIME));

        assertTrue(available.isEmpty());
    }

    // 27. Fired driver is excluded directly from employee service availability
    @Test
    void getAvailableDriversFromEmployeeService_firedDriver_isExcluded()
            throws RepositoryException {
        Employee firedDriver = buildDriverEmployee(DRIVER_ID, true);
        employeeRepository.save(firedDriver);

        List<Employee> available = employeeTransportationService.getAvailableDriversForDelivery(
                DELIVERY_DATE.atTime(MORNING_TIME));

        assertTrue(available.isEmpty());
    }

    // 28. Empty transport driver list stays empty even when employees exist
    @Test
    void getAvailableDrivers_noTransportDriversEvenWithEmployeeDrivers_returnsEmpty()
            throws RepositoryException {
        Employee employee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(employee);

        List<Driver> available = app.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, app.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 29. Saved driver/storekeeper assignments survive repository reload and support
    // delivery dispatch.
    @Test
    void persistedAssignments_afterReload_driverAvailableAndDispatchSucceeds()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, driverEmployee, true);
        assignStorekeeperToShift(shift, storekeeper, true);

        DeliveriesApplication reloadedApp = buildReloadedApplication();
        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        reloadedApp.addDriver(driver);

        List<Driver> available = reloadedApp.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, reloadedApp.getTruckByIndex(0));
        assertEquals(1, available.size());

        Delivery delivery = reloadedApp.planDelivery(
                DELIVERY_DATE,
                reloadedApp.findSiteByName("Warehouse"),
                futureStops(reloadedApp),
                MORNING_TIME,
                6000,
                reloadedApp.getTruckByIndex(0),
                available.get(0),
                reloadedApp.getShippingZoneByIndex(0));

        assertDoesNotThrow(() -> reloadedApp.dispatchDelivery(delivery));
    }

    // 30. Driver availability alone is not enough: dispatch fails without an
    // approved storekeeper on the delivery arrival shift.
    @Test
    void dispatchDelivery_driverApprovedButNoStorekeeper_fails()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(driverEmployee);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, driverEmployee, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                futureStops(app),
                MORNING_TIME,
                6000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> app.dispatchDelivery(delivery));
        assertTrue(error.getMessage().contains("No storekeeper"));
    }

    // 31. Unapproved driver assignments must not become available after reload.
    @Test
    void persistedUnapprovedDriverAssignment_afterReload_driverNotAvailable()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, driverEmployee, false);
        assignStorekeeperToShift(shift, storekeeper, true);

        DeliveriesApplication reloadedApp = buildReloadedApplication();
        reloadedApp.addDriver(buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C));

        List<Driver> available = reloadedApp.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, reloadedApp.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 32. Storekeeper exists, but is not approved: dispatch must still fail.
    @Test
    void dispatchDelivery_storekeeperAssignedButNotApproved_fails()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, driverEmployee, true);
        assignStorekeeperToShift(shift, storekeeper, false);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                futureStops(app),
                MORNING_TIME,
                6000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> app.dispatchDelivery(delivery));
        assertTrue(error.getMessage().contains("No storekeeper"));
    }

    // 33. Storekeeper on a different date does not satisfy the delivery arrival
    // shift.
    @Test
    void dispatchDelivery_storekeeperOnDifferentDate_fails()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift deliveryShift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(deliveryShift, driverEmployee, true);

        Shift wrongDateShift = new Shift(
                DELIVERY_DATE.plusDays(1),
                ShiftType.MORNING,
                buildShiftManagerEmployee("200000001"),
                1,
                1);
        shiftRepository.save(wrongDateShift);
        assignStorekeeperToShift(wrongDateShift, storekeeper, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                futureStops(app),
                MORNING_TIME,
                6000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> app.dispatchDelivery(delivery));
        assertTrue(error.getMessage().contains("No storekeeper"));
    }

    // 34. Storekeeper on the evening shift does not satisfy a morning delivery.
    @Test
    void dispatchDelivery_storekeeperOnWrongShiftType_fails()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift morningShift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(morningShift, driverEmployee, true);

        Shift eveningShift = new Shift(
                DELIVERY_DATE,
                ShiftType.EVENING,
                buildShiftManagerEmployee("200000001"),
                1,
                1);
        shiftRepository.save(eveningShift);
        assignStorekeeperToShift(eveningShift, storekeeper, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                futureStops(app),
                MORNING_TIME,
                6000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> app.dispatchDelivery(delivery));
        assertTrue(error.getMessage().contains("No storekeeper"));
    }

    // 35. Fired storekeepers do not count even if their assignment is approved.
    @Test
    void dispatchDelivery_firedStorekeeperAssignedAndApproved_fails()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002", true);
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, driverEmployee, true);
        assignStorekeeperToShift(shift, storekeeper, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                futureStops(app),
                MORNING_TIME,
                6000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> app.dispatchDelivery(delivery));
        assertTrue(error.getMessage().contains("No storekeeper"));
    }

    // 36. Once dispatched, a delivery must not be modified.
    @Test
    void dispatchedDelivery_recordWeightMeasurement_fails()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, driverEmployee, true);
        assignStorekeeperToShift(shift, storekeeper, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                futureStops(app),
                MORNING_TIME,
                6000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));

        app.dispatchDelivery(delivery);

        assertThrows(IllegalStateException.class,
                () -> app.recordWeightMeasurement(delivery, 7000));
    }

    // 37. Fired drivers are not available after a reload, even with an approved
    // shift assignment.
    @Test
    void persistedFiredDriverAssignment_afterReload_driverNotAvailable()
            throws RepositoryException {
        Employee firedDriver = buildDriverEmployee(DRIVER_ID, true);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(firedDriver);
        employeeRepository.save(storekeeper);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, firedDriver, true);
        assignStorekeeperToShift(shift, storekeeper, true);

        DeliveriesApplication reloadedApp = buildReloadedApplication();
        reloadedApp.addDriver(buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C));

        List<Driver> available = reloadedApp.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, reloadedApp.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 38. A saved and approved driver assignment still does not help if the
    // transport driver lacks the truck license.
    @Test
    void persistedApprovedDriverAssignment_wrongTruckLicense_driverNotAvailable()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift shift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(shift, driverEmployee, true);
        assignStorekeeperToShift(shift, storekeeper, true);

        DeliveriesApplication reloadedApp = buildReloadedApplication();
        reloadedApp.addDriver(buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.B));

        List<Driver> available = reloadedApp.getAvailableDriversForDelivery(
                DELIVERY_DATE, MORNING_TIME, reloadedApp.getTruckByIndex(0));

        assertTrue(available.isEmpty());
    }

    // 39. A delivery stop outside the selected shipping zone is rejected.
    @Test
    void planDelivery_stopOutsideSelectedZone_throws()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(driverEmployee);
        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        ShippingZone southZone = new ShippingZone("SOUTH", "Southern Zone");
        Site southStore = new Site("South Store", "5 South St", "03-3333333", "South Contact", southZone);
        app.addShippingZone(southZone);
        app.addSite(southStore);

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(new DeliveryStop(
                0,
                StopType.PICKUP,
                app.findSiteByName("Warehouse"),
                DELIVERY_DATE.atTime(MORNING_TIME).plusMinutes(30)));
        stops.add(new DeliveryStop(
                1,
                StopType.DROPOFF,
                southStore,
                DELIVERY_DATE.atTime(MORNING_TIME).plusHours(2)));

        assertThrows(IllegalArgumentException.class,
                () -> app.planDelivery(
                        DELIVERY_DATE,
                        app.findSiteByName("Warehouse"),
                        stops,
                        MORNING_TIME,
                        6000,
                        app.getTruckByIndex(0),
                        driver,
                        app.getShippingZoneByIndex(0)));
    }

    // 40. Initial measured weight cannot be lower than the truck net weight.
    @Test
    void planDelivery_weightBelowTruckNetWeight_throws()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        employeeRepository.save(driverEmployee);
        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        assertThrows(IllegalArgumentException.class,
                () -> app.planDelivery(
                        DELIVERY_DATE,
                        app.findSiteByName("Warehouse"),
                        futureStops(app),
                        MORNING_TIME,
                        4999,
                        app.getTruckByIndex(0),
                        driver,
                        app.getShippingZoneByIndex(0)));
    }

    // 41. A storekeeper in the departure shift does not satisfy a stop that
    // arrives during the evening shift.
    @Test
    void dispatchDelivery_stopArrivesInShiftWithoutStorekeeper_fails()
            throws RepositoryException {
        Employee driverEmployee = buildDriverEmployee(DRIVER_ID, false);
        Employee storekeeper = buildStorekeeperEmployee("100000002");
        employeeRepository.save(driverEmployee);
        employeeRepository.save(storekeeper);

        Shift morningShift = buildAndSaveShift(DELIVERY_DATE);
        assignDriverToShift(morningShift, driverEmployee, true);
        assignStorekeeperToShift(morningShift, storekeeper, true);

        Driver driver = buildTransportDriver(DRIVER_ID, "Moshe Cohen", LicenseType.C);
        app.addDriver(driver);

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(new DeliveryStop(
                0,
                StopType.PICKUP,
                app.findSiteByName("Warehouse"),
                DELIVERY_DATE.atTime(MORNING_TIME).plusMinutes(30)));
        stops.add(new DeliveryStop(
                1,
                StopType.DROPOFF,
                app.findSiteByName("Store"),
                DELIVERY_DATE.atTime(EVENING_TIME).plusMinutes(30)));

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                stops,
                MORNING_TIME,
                6000,
                app.getTruckByIndex(0),
                driver,
                app.getShippingZoneByIndex(0));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> app.dispatchDelivery(delivery));
        assertTrue(error.getMessage().contains("No storekeeper"));
    }

    // 42. Replacing a driver creates a second driver assignment request.
    @Test
    void replaceDriver_manualFlow_twoOpenAssignmentRequests()
            throws RepositoryException {
        String replacementId = "100000002";
        Employee original = buildDriverEmployee(DRIVER_ID, false);
        Employee replacement = buildDriverEmployee(replacementId, false);
        employeeRepository.save(original);
        employeeRepository.save(replacement);

        Driver originalDriver = buildTransportDriver(DRIVER_ID, "Original Driver", LicenseType.C);
        Driver replacementDriver = buildTransportDriver(replacementId, "Replacement Driver", LicenseType.C);
        app.addDriver(originalDriver);
        app.addDriver(replacementDriver);

        Delivery delivery = app.planDelivery(
                DELIVERY_DATE,
                app.findSiteByName("Warehouse"),
                futureStops(app),
                MORNING_TIME,
                6000,
                app.getTruckByIndex(0),
                originalDriver,
                app.getShippingZoneByIndex(0));

        app.replaceDriver(delivery, replacementDriver);

        assertEquals(2, employeeTransportationService.getOpenDriverAssignmentRequests().size());
        assertEquals(replacementId,
                employeeTransportationService.getOpenDriverAssignmentRequests().get(1).getDriverId());
    }

    private Employee buildStorekeeperEmployee(String id) {
        return buildStorekeeperEmployee(id, false);
    }

    private Employee buildStorekeeperEmployee(String id, boolean isFired) {
        return new Employee(
                id,
                "password123",
                new BankAccount("10", "100", "100000002"),
                "Store Keeper",
                new Salary(8000, 40, 180),
                EmploymentType.REGULAR,
                new EmploymentTerms(
                        LocalDate.of(2026, 7, 1),
                        EmploymentScope.FULL_TIME,
                        8000, 40, 14),
                Set.of(Role.STOREKEEPER),
                false,
                isFired,
                null,
                null,
                new Branch("1", "North Branch", "Haifa"));
    }

    private void seedBranchForStorekeeperTests() throws Exception {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT OR IGNORE INTO branches (branch_id, branch_name, address)
                    VALUES (1, 'North Branch', 'Haifa')
                    """);
        }
    }

    private void assignStorekeeperToShift(Shift shift, Employee employee, boolean approved)
            throws RepositoryException {
        ShiftAssignment assignment = new ShiftAssignment(employee, shift, Role.STOREKEEPER);
        assignment.setApproved(approved);
        shift.addAssignment(assignment);
        shiftRepository.save(shift);
    }

    private DeliveriesApplication buildReloadedApplication() {
        EmployeeTransportationService reloadedService = new EmployeeTransportationService(
                new ShiftRepositoryImpl(),
                new EmployeeRepositoryImpl());

        DeliveriesApplication reloadedApp = new DeliveriesApplication(reloadedService);
        ShippingZone northZone = new ShippingZone("NORTH", "Northern Zone");
        reloadedApp.addShippingZone(northZone);
        reloadedApp.addSite(new Site("Warehouse", "10 Main St", "03-1111111", "Warehouse Manager", northZone));
        reloadedApp.addSite(new Site("Store", "20 Side St", "03-2222222", "Store Contact", northZone));
        reloadedApp.addTruck(new Truck("123-45-678", "Volvo", 5000, 10000, LicenseType.C));
        return reloadedApp;
    }

    private List<DeliveryStop> futureStops(DeliveriesApplication deliveriesApp) {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(new DeliveryStop(
                0,
                StopType.PICKUP,
                deliveriesApp.findSiteByName("Warehouse"),
                DELIVERY_DATE.atTime(MORNING_TIME).plusMinutes(30)));
        stops.add(new DeliveryStop(
                1,
                StopType.DROPOFF,
                deliveriesApp.findSiteByName("Store"),
                DELIVERY_DATE.atTime(MORNING_TIME).plusHours(2)));
        return stops;
    }
}
