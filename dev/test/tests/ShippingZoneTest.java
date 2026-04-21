package tests;

import domain.ShippingZone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShippingZoneTest {

    private ShippingZone createNorthZone() {
        return new ShippingZone("NORTH", "Northern Zone");
    }

    @Test
    void constructor_validInput_createsShippingZoneSuccessfully() {
        ShippingZone zone = createNorthZone();

        assertNotNull(zone);
        assertEquals("NORTH", zone.getZoneCode());
        assertEquals("Northern Zone", zone.getZoneName());
    }

    @Test
    void constructor_nullZoneCode_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShippingZone(null, "Northern Zone")
        );
    }

    @Test
    void constructor_blankZoneCode_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShippingZone("   ", "Northern Zone")
        );
    }

    @Test
    void constructor_nullZoneName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShippingZone("NORTH", null)
        );
    }

    @Test
    void constructor_blankZoneName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShippingZone("NORTH", "   ")
        );
    }

    @Test
    void setZoneName_validName_updatesZoneName() {
        ShippingZone zone = createNorthZone();

        zone.setZoneName("Updated Northern Zone");

        assertEquals("Updated Northern Zone", zone.getZoneName());
    }

    @Test
    void setZoneName_nullName_throwsException() {
        ShippingZone zone = createNorthZone();

        assertThrows(IllegalArgumentException.class, () ->
                zone.setZoneName(null)
        );
    }

    @Test
    void setZoneName_blankName_throwsException() {
        ShippingZone zone = createNorthZone();

        assertThrows(IllegalArgumentException.class, () ->
                zone.setZoneName("   ")
        );
    }

    @Test
    void equals_whenZoneCodesAreEqual_returnsTrue() {
        ShippingZone zone1 = new ShippingZone("NORTH", "Northern Zone");
        ShippingZone zone2 = new ShippingZone("NORTH", "Another Name");

        assertEquals(zone1, zone2);
    }

    @Test
    void equals_whenZoneCodesAreDifferent_returnsFalse() {
        ShippingZone zone1 = new ShippingZone("NORTH", "Northern Zone");
        ShippingZone zone2 = new ShippingZone("SOUTH", "Southern Zone");

        assertNotEquals(zone1, zone2);
    }

    @Test
    void hashCode_whenZoneCodesAreEqual_isEqual() {
        ShippingZone zone1 = new ShippingZone("NORTH", "Northern Zone");
        ShippingZone zone2 = new ShippingZone("NORTH", "Another Name");

        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test
    void equals_whenComparedToNull_returnsFalse() {
        ShippingZone zone = createNorthZone();

        assertNotEquals(null, zone);
    }

    @Test
    void equals_whenComparedToDifferentType_returnsFalse() {
        ShippingZone zone = createNorthZone();

        assertNotEquals("NORTH", zone);
    }
}