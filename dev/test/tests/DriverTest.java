package tests;

import domain.Driver;
import domain.LicenseType;
import domain.Truck;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DriverTest {

    private Driver createDriverWithCAndB() {
        return new Driver("Eden", new HashSet<>(Set.of(LicenseType.C, LicenseType.B)));
    }

    private Truck createTruckRequiringC() {
        return new Truck("123-45-678", "Volvo", 5000, 10000, LicenseType.C);
    }

    private Truck createTruckRequiringC1() {
        return new Truck("987-65-432", "MAN", 4000, 9000, LicenseType.C1);
    }

    @Test
    void constructor_validInput_createsDriverSuccessfully() {
        Driver driver = createDriverWithCAndB();

        assertNotNull(driver);
        assertEquals("Eden", driver.getDriverName());
        assertEquals(2, driver.getLicenseTypes().size());
        assertTrue(driver.getLicenseTypes().contains(LicenseType.C));
        assertTrue(driver.getLicenseTypes().contains(LicenseType.B));
    }

    @Test
    void constructor_nullDriverName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Driver(null, new HashSet<>(Set.of(LicenseType.C)))
        );
    }

    @Test
    void constructor_blankDriverName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Driver("   ", new HashSet<>(Set.of(LicenseType.C)))
        );
    }

    @Test
    void constructor_nullLicenseTypes_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Driver("Eden", null)
        );
    }

    @Test
    void constructor_emptyLicenseTypes_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Driver("Eden", new HashSet<>())
        );
    }

    @Test
    void getLicenseTypes_returnsCopyAndNotOriginalSet() {
        Driver driver = createDriverWithCAndB();

        Set<LicenseType> licenseTypesFromGetter = driver.getLicenseTypes();
        licenseTypesFromGetter.add(LicenseType.C1);

        assertFalse(driver.getLicenseTypes().contains(LicenseType.C1));
        assertEquals(2, driver.getLicenseTypes().size());
    }

    @Test
    void hasLicenseType_whenDriverHasLicense_returnsTrue() {
        Driver driver = createDriverWithCAndB();

        assertTrue(driver.hasLicenseType(LicenseType.C));
    }

    @Test
    void hasLicenseType_whenDriverDoesNotHaveLicense_returnsFalse() {
        Driver driver = createDriverWithCAndB();

        assertFalse(driver.hasLicenseType(LicenseType.C1));
    }

    @Test
    void hasLicenseType_nullLicenseType_throwsException() {
        Driver driver = createDriverWithCAndB();

        assertThrows(IllegalArgumentException.class, () ->
                driver.hasLicenseType(null)
        );
    }

    @Test
    void canDrive_whenDriverHasRequiredLicense_returnsTrue() {
        Driver driver = createDriverWithCAndB();
        Truck truck = createTruckRequiringC();

        assertTrue(driver.canDrive(truck));
    }

    @Test
    void canDrive_whenDriverDoesNotHaveRequiredLicense_returnsFalse() {
        Driver driver = createDriverWithCAndB();
        Truck truck = createTruckRequiringC1();

        assertFalse(driver.canDrive(truck));
    }

    @Test
    void canDrive_nullTruck_throwsException() {
        Driver driver = createDriverWithCAndB();

        assertThrows(IllegalArgumentException.class, () ->
                driver.canDrive(null)
        );
    }

    @Test
    void addLicenseType_validLicense_addsLicenseSuccessfully() {
        Driver driver = createDriverWithCAndB();

        driver.addLicenseType(LicenseType.C1);

        assertTrue(driver.getLicenseTypes().contains(LicenseType.C1));
        assertEquals(3, driver.getLicenseTypes().size());
    }

    @Test
    void addLicenseType_existingLicense_doesNotDuplicateIt() {
        Driver driver = createDriverWithCAndB();

        driver.addLicenseType(LicenseType.C);

        assertEquals(2, driver.getLicenseTypes().size());
        assertTrue(driver.getLicenseTypes().contains(LicenseType.C));
    }

    @Test
    void addLicenseType_nullLicense_throwsException() {
        Driver driver = createDriverWithCAndB();

        assertThrows(IllegalArgumentException.class, () ->
                driver.addLicenseType(null)
        );
    }

    @Test
    void removeLicenseType_existingLicense_removesItSuccessfully() {
        Driver driver = createDriverWithCAndB();

        driver.removeLicenseType(LicenseType.B);

        assertFalse(driver.getLicenseTypes().contains(LicenseType.B));
        assertEquals(1, driver.getLicenseTypes().size());
        assertTrue(driver.getLicenseTypes().contains(LicenseType.C));
    }

    @Test
    void removeLicenseType_nonExistingLicense_throwsException() {
        Driver driver = createDriverWithCAndB();

        assertThrows(IllegalArgumentException.class, () ->
                driver.removeLicenseType(LicenseType.C1)
        );
    }

    @Test
    void removeLicenseType_nullLicense_throwsException() {
        Driver driver = createDriverWithCAndB();

        assertThrows(IllegalArgumentException.class, () ->
                driver.removeLicenseType(null)
        );
    }

    @Test
    void removeLicenseType_whenTryingToRemoveLastLicense_throwsException() {
        Driver driver = new Driver("Noa", new HashSet<>(Set.of(LicenseType.C)));

        assertThrows(IllegalStateException.class, () ->
                driver.removeLicenseType(LicenseType.C)
        );
    }
}