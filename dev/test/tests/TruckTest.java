package tests;

import domain.LicenseType;
import domain.Truck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TruckTest {

    private Truck createValidTruck() {
        return new Truck("123-45-678", "Volvo", 5000, 10000, LicenseType.C);
    }

    @Test
    void constructor_validInput_createsTruckSuccessfully() {
        Truck truck = createValidTruck();

        assertNotNull(truck);
        assertEquals("123-45-678", truck.getLicenseNumber());
        assertEquals("Volvo", truck.getModel());
        assertEquals(5000, truck.getNetWeight());
        assertEquals(10000, truck.getMaxAllowedWeight());
        assertEquals(LicenseType.C, truck.getRequiredLicenseType());
    }

    @Test
    void constructor_nullLicenseNumber_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Truck(null, "Volvo", 5000, 10000, LicenseType.C)
        );
    }

    @Test
    void constructor_blankLicenseNumber_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Truck("   ", "Volvo", 5000, 10000, LicenseType.C)
        );
    }

    @Test
    void constructor_nullModel_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Truck("123-45-678", null, 5000, 10000, LicenseType.C)
        );
    }

    @Test
    void constructor_blankModel_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Truck("123-45-678", "   ", 5000, 10000, LicenseType.C)
        );
    }

    @Test
    void constructor_negativeNetWeight_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Truck("123-45-678", "Volvo", -1, 10000, LicenseType.C)
        );
    }

    @Test
    void constructor_negativeMaxAllowedWeight_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Truck("123-45-678", "Volvo", 5000, -1, LicenseType.C)
        );
    }

    @Test
    void constructor_maxAllowedWeightSmallerThanNetWeight_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Truck("123-45-678", "Volvo", 7000, 6000, LicenseType.C)
        );
    }

    @Test
    void constructor_nullRequiredLicenseType_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Truck("123-45-678", "Volvo", 5000, 10000, null)
        );
    }

    @Test
    void canCarryWeight_whenWeightIsLessThanLimit_returnsTrue() {
        Truck truck = createValidTruck();

        assertTrue(truck.canCarryWeight(9000));
    }

    @Test
    void canCarryWeight_whenWeightEqualsLimit_returnsTrue() {
        Truck truck = createValidTruck();

        assertTrue(truck.canCarryWeight(10000));
    }

    @Test
    void canCarryWeight_whenWeightIsGreaterThanLimit_returnsFalse() {
        Truck truck = createValidTruck();

        assertFalse(truck.canCarryWeight(10001));
    }

    @Test
    void canCarryWeight_negativeWeight_throwsException() {
        Truck truck = createValidTruck();

        assertThrows(IllegalArgumentException.class, () ->
                truck.canCarryWeight(-1)
        );
    }
}