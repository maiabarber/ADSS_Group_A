package tests;

import domain.Delivery;
import domain.DeliveryDocument;
import domain.DeliveryForm;
import domain.DeliveryItem;
import domain.DeliveryStatus;
import domain.DeliveryStop;
import domain.Driver;
import domain.LicenseType;
import domain.ShippingZone;
import domain.Site;
import domain.StopType;
import domain.Truck;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryTest {

    private ShippingZone createNorthZone() {
        return new ShippingZone("NORTH", "Northern Zone");
    }

    private ShippingZone createSouthZone() {
        return new ShippingZone("SOUTH", "Southern Zone");
    }

    private Truck createTruck(double maxAllowedWeight) {
        return new Truck("123-45-678", "Volvo", 5000, maxAllowedWeight, LicenseType.C);
    }

    private Driver createDriver() {
        return new Driver("Eden", Set.of(LicenseType.C, LicenseType.B));
    }

    private Site createNorthSite(String name) {
        return new Site(name, "Haifa", "0501234567", "Dana", createNorthZone());
    }

    private Site createSouthSite(String name) {
        return new Site(name, "Beer Sheva", "0507654321", "Noa", createSouthZone());
    }

    private DeliveryDocument createDocument() {
        List<DeliveryItem> items = new ArrayList<>();
        items.add(new DeliveryItem("1", "Milk", 10));
        return new DeliveryDocument(1001, items);
    }

    private DeliveryStop createNorthStop(int order) {
        return new DeliveryStop(order, StopType.PICKUP, createNorthSite("North Stop " + order));
    }

    private DeliveryStop createNorthStopWithDocument(int order) {
        return new DeliveryStop(order, StopType.DROPOFF, createNorthSite("North Stop " + order), createDocument());
    }

    private DeliveryStop createSouthStop(int order) {
        return new DeliveryStop(order, StopType.PICKUP, createSouthSite("South Stop " + order));
    }

    private Delivery createLegacyDelivery(double actualWeightAtDeparture) {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStop(1));

        return new Delivery(
                LocalDate.of(2026, 4, 20),
                stops,
                LocalTime.of(10, 30),
                actualWeightAtDeparture,
                createTruck(10000),
                createDriver(),
                createDocument()
        );
    }

    private Delivery createNewDelivery(double initialWeight) {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        return new Delivery(
                LocalDate.of(2026, 4, 20),
                createNorthSite("North Warehouse"),
                stops,
                LocalTime.of(10, 30),
                initialWeight,
                createTruck(10000),
                createDriver(),
                createNorthZone(),
                DeliveryStatus.PLANNED,
                new DeliveryForm()
        );
    }

    @Test
    void newConstructor_validInput_createsDeliverySuccessfully() {
        Delivery delivery = createNewDelivery(7000);

        assertNotNull(delivery);
        assertEquals(LocalDate.of(2026, 4, 20), delivery.getDeliveryDate());
        assertEquals(LocalTime.of(10, 30), delivery.getDepartureTime());
        assertEquals(7000, delivery.getFinalMeasuredWeightBeforeDeparture());
        assertEquals("North Warehouse", delivery.getSource().getSiteName());
        assertEquals("NORTH", delivery.getShippingZone().getZoneCode());
        assertEquals(DeliveryStatus.PLANNED, delivery.getStatus());
        assertEquals(1, delivery.getStops().size());
        assertTrue(delivery.getDeliveryForm().hasMeasurements());
        assertEquals(7000, delivery.getDeliveryForm().getLatestWeightMeasurement());
    }

    @Test
    void newConstructor_nullDeliveryDate_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        null,
                        createNorthSite("North Warehouse"),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createNorthZone(),
                        DeliveryStatus.PLANNED,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void newConstructor_nullSource_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        null,
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createNorthZone(),
                        DeliveryStatus.PLANNED,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void newConstructor_nullStops_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSite("North Warehouse"),
                        null,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createNorthZone(),
                        DeliveryStatus.PLANNED,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void newConstructor_nullDepartureTime_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSite("North Warehouse"),
                        stops,
                        null,
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createNorthZone(),
                        DeliveryStatus.PLANNED,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void newConstructor_nullTruck_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSite("North Warehouse"),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        null,
                        createDriver(),
                        createNorthZone(),
                        DeliveryStatus.PLANNED,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void newConstructor_nullDriver_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSite("North Warehouse"),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        null,
                        createNorthZone(),
                        DeliveryStatus.PLANNED,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void newConstructor_nullShippingZone_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSite("North Warehouse"),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        null,
                        DeliveryStatus.PLANNED,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void newConstructor_nullStatus_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSite("North Warehouse"),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createNorthZone(),
                        null,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void newConstructor_nullDeliveryForm_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSite("North Warehouse"),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createNorthZone(),
                        DeliveryStatus.PLANNED,
                        null
                )
        );
    }

    @Test
    void newConstructor_negativeWeight_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSite("North Warehouse"),
                        stops,
                        LocalTime.of(10, 30),
                        -1,
                        createTruck(10000),
                        createDriver(),
                        createNorthZone(),
                        DeliveryStatus.PLANNED,
                        new DeliveryForm()
                )
        );
    }

    @Test
    void legacyConstructor_validInput_createsDeliverySuccessfully() {
        Delivery delivery = createLegacyDelivery(7000);

        assertNotNull(delivery);
        assertEquals(LocalDate.of(2026, 4, 20), delivery.getDeliveryDate());
        assertEquals(LocalTime.of(10, 30), delivery.getDepartureTime());
        assertEquals(7000, delivery.getActualWeightAtDeparture());
        assertEquals(DeliveryStatus.PLANNED, delivery.getStatus());
        assertEquals(1, delivery.getStops().size());
        assertNotNull(delivery.getDocument());
        assertEquals(1001, delivery.getDocument().getDocumentNumber());
    }

    @Test
    void legacyConstructor_emptyStops_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        new ArrayList<>(),
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createDocument()
                )
        );
    }

    @Test
    void legacyConstructor_nullDocument_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStop(1));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        null
                )
        );
    }

    @Test
    void getStops_returnsCopyAndNotOriginalList() {
        Delivery delivery = createNewDelivery(7000);

        List<DeliveryStop> stopsFromGetter = delivery.getStops();
        stopsFromGetter.add(createNorthStop(99));

        assertEquals(1, delivery.getStops().size());
    }

    @Test
    void setFinalMeasuredWeightBeforeDeparture_validWeight_updatesWeightAndAddsMeasurement() {
        Delivery delivery = createNewDelivery(7000);

        delivery.setFinalMeasuredWeightBeforeDeparture(8000);

        assertEquals(8000, delivery.getFinalMeasuredWeightBeforeDeparture());
        assertEquals(2, delivery.getDeliveryForm().getWeightMeasurements().size());
        assertEquals(8000, delivery.getDeliveryForm().getLatestWeightMeasurement());
    }

    @Test
    void setActualWeightAtDeparture_legacySetter_updatesWeight() {
        Delivery delivery = createLegacyDelivery(7000);

        delivery.setActualWeightAtDeparture(8200);

        assertEquals(8200, delivery.getActualWeightAtDeparture());
    }

    @Test
    void setFinalMeasuredWeightBeforeDeparture_negativeWeight_throwsException() {
        Delivery delivery = createNewDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.setFinalMeasuredWeightBeforeDeparture(-5)
        );
    }

    @Test
    void setDriver_validDriver_updatesDriver() {
        Delivery delivery = createNewDelivery(7000);
        Driver newDriver = new Driver("Noa", Set.of(LicenseType.C1, LicenseType.C));

        delivery.setDriver(newDriver);

        assertEquals("Noa", delivery.getDriver().getDriverName());
    }

    @Test
    void setDriver_nullDriver_throwsException() {
        Delivery delivery = createNewDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.setDriver(null)
        );
    }

    @Test
    void setTruck_validTruck_updatesTruck() {
        Delivery delivery = createNewDelivery(7000);
        Truck newTruck = new Truck("987-65-432", "MAN", 4000, 9000, LicenseType.C1);

        delivery.setTruck(newTruck);

        assertEquals("MAN", delivery.getTruck().getModel());
    }

    @Test
    void setTruck_nullTruck_throwsException() {
        Delivery delivery = createNewDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.setTruck(null)
        );
    }

    @Test
    void setSource_validSource_updatesSource() {
        Delivery delivery = createNewDelivery(7000);
        Site newSource = createNorthSite("Alternative Source");

        delivery.setSource(newSource);

        assertEquals("Alternative Source", delivery.getSource().getSiteName());
    }

    @Test
    void setSource_nullSource_throwsException() {
        Delivery delivery = createNewDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.setSource(null)
        );
    }

    @Test
    void setShippingZone_validZone_updatesShippingZone() {
        Delivery delivery = createNewDelivery(7000);

        delivery.setShippingZone(createSouthZone());

        assertEquals("SOUTH", delivery.getShippingZone().getZoneCode());
    }

    @Test
    void setShippingZone_nullZone_throwsException() {
        Delivery delivery = createNewDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.setShippingZone(null)
        );
    }

    @Test
    void setStatus_validStatus_updatesStatus() {
        Delivery delivery = createNewDelivery(7000);

        delivery.setStatus(DeliveryStatus.PENDING_REPLAN);

        assertEquals(DeliveryStatus.PENDING_REPLAN, delivery.getStatus());
    }

    @Test
    void setStatus_nullStatus_throwsException() {
        Delivery delivery = createNewDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.setStatus(null)
        );
    }

    @Test
    void addStop_addsStopSuccessfully() {
        Delivery delivery = createNewDelivery(7000);

        delivery.addStop(createNorthStop(2));

        assertEquals(2, delivery.getStops().size());
    }

    @Test
    void addStop_nullStop_throwsException() {
        Delivery delivery = createNewDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.addStop(null)
        );
    }

    @Test
    void removeStop_removesExistingStop() {
        Delivery delivery = createNewDelivery(7000);
        DeliveryStop existingStop = delivery.getStops().get(0);

        delivery.removeStop(existingStop);

        assertEquals(0, delivery.getStops().size());
    }

    @Test
    void removeStop_nullStop_throwsException() {
        Delivery delivery = createNewDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.removeStop(null)
        );
    }

    @Test
    void recordWeightMeasurement_whenWeightIsNormal_keepsStatusPlanned() {
        Delivery delivery = createNewDelivery(7000);

        delivery.recordWeightMeasurement(9000);

        assertEquals(9000, delivery.getFinalMeasuredWeightBeforeDeparture());
        assertEquals(DeliveryStatus.PLANNED, delivery.getStatus());
    }

    @Test
    void recordWeightMeasurement_whenWeightIsOverLimit_setsPendingReplan() {
        Delivery delivery = createNewDelivery(7000);

        delivery.recordWeightMeasurement(12000);

        assertEquals(12000, delivery.getFinalMeasuredWeightBeforeDeparture());
        assertEquals(DeliveryStatus.PENDING_REPLAN, delivery.getStatus());
    }

    @Test
    void markForReplan_setsStatusToPendingReplan() {
        Delivery delivery = createNewDelivery(7000);

        delivery.markForReplan();

        assertEquals(DeliveryStatus.PENDING_REPLAN, delivery.getStatus());
    }

    @Test
    void markAsDispatched_setsStatusToDispatched() {
        Delivery delivery = createNewDelivery(7000);

        delivery.markAsDispatched();

        assertEquals(DeliveryStatus.DISPATCHED, delivery.getStatus());
    }

    @Test
    void canStillBeModified_whenPlanned_returnsTrue() {
        Delivery delivery = createNewDelivery(7000);

        assertTrue(delivery.canStillBeModified());
    }

    @Test
    void canStillBeModified_whenDispatched_returnsFalse() {
        Delivery delivery = createNewDelivery(7000);
        delivery.markAsDispatched();

        assertFalse(delivery.canStillBeModified());
    }

    @Test
    void isOverweight_whenWeightGreaterThanTruckLimit_returnsTrue() {
        Delivery delivery = createNewDelivery(12000);

        assertTrue(delivery.isOverweight());
    }

    @Test
    void isOverweight_whenWeightEqualsTruckLimit_returnsFalse() {
        Delivery delivery = createNewDelivery(10000);

        assertFalse(delivery.isOverweight());
    }

    @Test
    void isOverweight_whenWeightLessThanTruckLimit_returnsFalse() {
        Delivery delivery = createNewDelivery(7000);

        assertFalse(delivery.isOverweight());
    }

    @Test
    void allStopsBelongToShippingZone_whenAllInSameZone_returnsTrue() {
        Delivery delivery = createNewDelivery(7000);

        assertTrue(delivery.allStopsBelongToShippingZone());
    }

    @Test
    void allStopsBelongToShippingZone_whenOneStopInDifferentZone_returnsFalse() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStopWithDocument(0));
        stops.add(createSouthStop(1));

        Delivery delivery = new Delivery(
                LocalDate.of(2026, 4, 20),
                createNorthSite("North Warehouse"),
                stops,
                LocalTime.of(10, 30),
                7000,
                createTruck(10000),
                createDriver(),
                createNorthZone(),
                DeliveryStatus.PLANNED,
                new DeliveryForm()
        );

        assertFalse(delivery.allStopsBelongToShippingZone());
    }

    @Test
    void getDocument_returnsFirstStopDocument() {
        Delivery delivery = createNewDelivery(7000);

        assertNotNull(delivery.getDocument());
        assertEquals(1001, delivery.getDocument().getDocumentNumber());
    }

    @Test
    void getDocument_whenFirstStopHasNoDocument_returnsNull() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthStop(0));

        Delivery delivery = new Delivery(
                LocalDate.of(2026, 4, 20),
                createNorthSite("North Warehouse"),
                stops,
                LocalTime.of(10, 30),
                7000,
                createTruck(10000),
                createDriver(),
                createNorthZone(),
                DeliveryStatus.PLANNED,
                new DeliveryForm()
        );

        assertNull(delivery.getDocument());
    }
}