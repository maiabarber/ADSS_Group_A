package tests;

import domain.DeliveryDocument;
import domain.DeliveryItem;
import domain.DeliveryStop;
import domain.ShippingZone;
import domain.Site;
import domain.StopType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryStopTest {

    private ShippingZone createNorthZone() {
        return new ShippingZone("NORTH", "Northern Zone");
    }

    private ShippingZone createSouthZone() {
        return new ShippingZone("SOUTH", "Southern Zone");
    }

    private Site createNorthSite() {
        return new Site(
                "North Warehouse",
                "10 Haifa Port Rd",
                "04-1111111",
                "Dana Levi",
                createNorthZone()
        );
    }

    private DeliveryDocument createDocument() {
        List<DeliveryItem> items = new ArrayList<>();
        items.add(new DeliveryItem("ITEM-001", "Milk 3%", 10));
        return new DeliveryDocument(1001, items);
    }

    @Test
    void legacyConstructor_validInput_createsStopSuccessfullyWithoutDocument() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        assertNotNull(stop);
        assertEquals(0, stop.getStopOrder());
        assertEquals(StopType.PICKUP, stop.getStopType());
        assertEquals("North Warehouse", stop.getSite().getSiteName());
        assertNull(stop.getDocument());
        assertFalse(stop.hasDocument());
    }

    @Test
    void fullConstructor_validInput_createsStopSuccessfullyWithDocument() {
        DeliveryStop stop = new DeliveryStop(1, StopType.DROPOFF, createNorthSite(), createDocument());

        assertNotNull(stop);
        assertEquals(1, stop.getStopOrder());
        assertEquals(StopType.DROPOFF, stop.getStopType());
        assertEquals("North Warehouse", stop.getSite().getSiteName());
        assertNotNull(stop.getDocument());
        assertTrue(stop.hasDocument());
    }

    @Test
    void constructor_negativeStopOrder_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryStop(-1, StopType.PICKUP, createNorthSite())
        );
    }

    @Test
    void constructor_nullStopType_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryStop(0, null, createNorthSite())
        );
    }

    @Test
    void constructor_nullSite_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryStop(0, StopType.PICKUP, null)
        );
    }

    @Test
    void setStopOrder_validOrder_updatesOrder() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        stop.setStopOrder(3);

        assertEquals(3, stop.getStopOrder());
    }

    @Test
    void setStopOrder_negativeOrder_throwsException() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        assertThrows(IllegalArgumentException.class, () ->
                stop.setStopOrder(-1)
        );
    }

    @Test
    void setSite_validSite_updatesSite() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        Site newSite = new Site(
                "Another Site",
                "25 New Address",
                "08-2222222",
                "Rina Cohen",
                createNorthZone()
        );

        stop.setSite(newSite);

        assertEquals("Another Site", stop.getSite().getSiteName());
    }

    @Test
    void setSite_nullSite_throwsException() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        assertThrows(IllegalArgumentException.class, () ->
                stop.setSite(null)
        );
    }

    @Test
    void setDocument_validDocument_updatesDocument() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        stop.setDocument(createDocument());

        assertNotNull(stop.getDocument());
        assertTrue(stop.hasDocument());
        assertEquals(1001, stop.getDocument().getDocumentNumber());
    }

    @Test
    void setDocument_nullDocument_removesDocument() {
        DeliveryStop stop = new DeliveryStop(0, StopType.DROPOFF, createNorthSite(), createDocument());

        stop.setDocument(null);

        assertNull(stop.getDocument());
        assertFalse(stop.hasDocument());
    }

    @Test
    void hasDocument_whenDocumentExists_returnsTrue() {
        DeliveryStop stop = new DeliveryStop(1, StopType.DROPOFF, createNorthSite(), createDocument());

        assertTrue(stop.hasDocument());
    }

    @Test
    void hasDocument_whenDocumentDoesNotExist_returnsFalse() {
        DeliveryStop stop = new DeliveryStop(1, StopType.PICKUP, createNorthSite());

        assertFalse(stop.hasDocument());
    }

    @Test
    void belongsToZone_whenStopSiteBelongsToZone_returnsTrue() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        assertTrue(stop.belongsToZone(createNorthZone()));
    }

    @Test
    void belongsToZone_whenStopSiteDoesNotBelongToZone_returnsFalse() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        assertFalse(stop.belongsToZone(createSouthZone()));
    }

    @Test
    void belongsToZone_nullZone_throwsException() {
        DeliveryStop stop = new DeliveryStop(0, StopType.PICKUP, createNorthSite());

        assertThrows(IllegalArgumentException.class, () ->
                stop.belongsToZone(null)
        );
    }
}