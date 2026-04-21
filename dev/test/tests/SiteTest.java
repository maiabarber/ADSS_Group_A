package tests;

import domain.ShippingZone;
import domain.Site;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SiteTest {

    private ShippingZone createNorthZone() {
        return new ShippingZone("NORTH", "Northern Zone");
    }

    private ShippingZone createSouthZone() {
        return new ShippingZone("SOUTH", "Southern Zone");
    }

    private Site createValidSite() {
        return new Site(
                "Main Warehouse",
                "10 Haifa Port Rd",
                "04-1111111",
                "Dana Levi",
                createNorthZone()
        );
    }

    @Test
    void constructor_withFullValidInput_createsSiteSuccessfully() {
        Site site = createValidSite();

        assertNotNull(site);
        assertEquals("Main Warehouse", site.getSiteName());
        assertEquals("10 Haifa Port Rd", site.getAddress());
        assertEquals("04-1111111", site.getPhoneNumber());
        assertEquals("Dana Levi", site.getContactName());
        assertEquals("NORTH", site.getShippingZone().getZoneCode());
    }

    @Test
    void legacyConstructor_validInput_createsSiteWithDefaultShippingZone() {
        Site site = new Site(
                "Legacy Site",
                "Beer Sheva",
                "0501234567",
                "Yossi"
        );

        assertNotNull(site);
        assertEquals("Legacy Site", site.getSiteName());
        assertEquals("UNASSIGNED", site.getShippingZone().getZoneCode());
        assertEquals("Unassigned Zone", site.getShippingZone().getZoneName());
    }

    @Test
    void constructor_nullSiteName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site(null, "Address", "0501234567", "Dana", createNorthZone())
        );
    }

    @Test
    void constructor_blankSiteName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site("   ", "Address", "0501234567", "Dana", createNorthZone())
        );
    }

    @Test
    void constructor_nullAddress_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site("Site", null, "0501234567", "Dana", createNorthZone())
        );
    }

    @Test
    void constructor_blankAddress_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site("Site", "   ", "0501234567", "Dana", createNorthZone())
        );
    }

    @Test
    void constructor_nullPhoneNumber_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site("Site", "Address", null, "Dana", createNorthZone())
        );
    }

    @Test
    void constructor_blankPhoneNumber_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site("Site", "Address", "   ", "Dana", createNorthZone())
        );
    }

    @Test
    void constructor_nullContactName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site("Site", "Address", "0501234567", null, createNorthZone())
        );
    }

    @Test
    void constructor_blankContactName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site("Site", "Address", "0501234567", "   ", createNorthZone())
        );
    }

    @Test
    void constructor_nullShippingZone_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Site("Site", "Address", "0501234567", "Dana", null)
        );
    }

    @Test
    void setAddress_validAddress_updatesAddress() {
        Site site = createValidSite();

        site.setAddress("25 New Address");

        assertEquals("25 New Address", site.getAddress());
    }

    @Test
    void setAddress_blankAddress_throwsException() {
        Site site = createValidSite();

        assertThrows(IllegalArgumentException.class, () ->
                site.setAddress("   ")
        );
    }

    @Test
    void setPhoneNumber_validPhone_updatesPhone() {
        Site site = createValidSite();

        site.setPhoneNumber("08-2222222");

        assertEquals("08-2222222", site.getPhoneNumber());
    }

    @Test
    void setPhoneNumber_nullPhone_throwsException() {
        Site site = createValidSite();

        assertThrows(IllegalArgumentException.class, () ->
                site.setPhoneNumber(null)
        );
    }

    @Test
    void setContactName_validName_updatesContactName() {
        Site site = createValidSite();

        site.setContactName("Rina Cohen");

        assertEquals("Rina Cohen", site.getContactName());
    }

    @Test
    void setContactName_blankName_throwsException() {
        Site site = createValidSite();

        assertThrows(IllegalArgumentException.class, () ->
                site.setContactName("   ")
        );
    }

    @Test
    void setShippingZone_validZone_updatesShippingZone() {
        Site site = createValidSite();
        ShippingZone southZone = createSouthZone();

        site.setShippingZone(southZone);

        assertEquals("SOUTH", site.getShippingZone().getZoneCode());
    }

    @Test
    void setShippingZone_nullZone_throwsException() {
        Site site = createValidSite();

        assertThrows(IllegalArgumentException.class, () ->
                site.setShippingZone(null)
        );
    }

    @Test
    void belongsToZone_whenSiteBelongsToZone_returnsTrue() {
        Site site = createValidSite();

        assertTrue(site.belongsToZone(createNorthZone()));
    }

    @Test
    void belongsToZone_whenSiteDoesNotBelongToZone_returnsFalse() {
        Site site = createValidSite();

        assertFalse(site.belongsToZone(createSouthZone()));
    }

    @Test
    void belongsToZone_nullZone_throwsException() {
        Site site = createValidSite();

        assertThrows(IllegalArgumentException.class, () ->
                site.belongsToZone(null)
        );
    }
}