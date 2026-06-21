package tests;

import domain.Delivery;
import domain.DeliveryItem;
import domain.DeliveryStatus;
import domain.DeliveryStop;
import domain.Driver;
import domain.LicenseType;
import domain.ShippingZone;
import domain.Site;
import domain.Truck;
import org.junit.jupiter.api.Test;
import service.DeliveriesApplication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveriesApplicationTest {

    private ShippingZone createNorthZone() {
        return new ShippingZone("NORTH", "Northern Zone");
    }

    private ShippingZone createSouthZone() {
        return new ShippingZone("SOUTH", "Southern Zone");
    }

    private Site createNorthWarehouse() {
        return new Site("North Warehouse", "10 Haifa Port Rd", "04-1111111", "Dana Levi", createNorthZone());
    }

    private Site createNorthStore() {
        return new Site("North Store", "25 Market St", "04-2222222", "Yossi Cohen", createNorthZone());
    }

    private Site createSouthStore() {
        return new Site("South Store", "5 Negev Rd", "08-3333333", "Noa Levi", createSouthZone());
    }

    private Truck createTruckC() {
        return new Truck("123-45-678", "Volvo", 5000, 10000, LicenseType.C);
    }

    private Truck createTruckC1() {
        return new Truck("987-65-432", "MAN", 4000, 9000, LicenseType.C1);
    }

    private Driver createDriverC() {
        return new Driver("Eden", Set.of(LicenseType.C, LicenseType.B));
    }

    private Driver createDriverC1() {
        return new Driver("Noa", Set.of(LicenseType.C1));
    }

    private DeliveriesApplication createApplicationWithBaseData() {
        DeliveriesApplication app = new DeliveriesApplication();

        app.addShippingZone(createNorthZone());
        app.addShippingZone(createSouthZone());

        app.addSite(createNorthWarehouse());
        app.addSite(createNorthStore());
        app.addSite(createSouthStore());

        app.addTruck(createTruckC());
        app.addTruck(createTruckC1());

        app.addDriver(createDriverC());
        app.addDriver(createDriverC1());

        return app;
    }

    private Delivery createManagedDelivery(DeliveriesApplication app) {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(app.createPickupStop(0, createNorthWarehouse()));
        stops.add(app.createDropoffStop(1, createNorthStore()));

        return app.planDelivery(
                LocalDate.of(2026, 4, 20),
                createNorthWarehouse(),
                stops,
                LocalTime.of(10, 30),
                7000,
                createTruckC(),
                createDriverC(),
                createNorthZone()
        );
    }

    @Test
    void constructor_defaultConstructor_createsApplicationSuccessfully() {
        DeliveriesApplication app = new DeliveriesApplication();

        assertNotNull(app);
        assertNotNull(app.getDeliveryManager());
        assertTrue(app.getAllDeliveries().isEmpty());
    }

    @Test
    void constructor_nullDeliveryManager_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveriesApplication(null)
        );
    }

    @Test
    void initializeEmpty_clearsAllData() {
        DeliveriesApplication app = createApplicationWithBaseData();

        app.initializeEmpty();

        assertTrue(app.getAllShippingZones().isEmpty());
        assertTrue(app.getAllSites().isEmpty());
        assertTrue(app.getAllTrucks().isEmpty());
        assertTrue(app.getAllDrivers().isEmpty());
        assertTrue(app.getAllDeliveries().isEmpty());
    }

    @Test
    void initializeWithSampleData_loadsSampleDataSuccessfully() {
        DeliveriesApplication app = new DeliveriesApplication();

        app.initializeWithSampleData();

        assertFalse(app.getAllShippingZones().isEmpty());
        assertFalse(app.getAllSites().isEmpty());
        assertFalse(app.getAllTrucks().isEmpty());
        assertFalse(app.getAllDrivers().isEmpty());
        assertFalse(app.getAllDeliveries().isEmpty());
    }

    @Test
    void addMethods_validEntities_addThemSuccessfully() {
        DeliveriesApplication app = new DeliveriesApplication();

        app.addShippingZone(createNorthZone());
        app.addSite(createNorthWarehouse());
        app.addTruck(createTruckC());
        app.addDriver(createDriverC());

        assertEquals(1, app.getAllShippingZones().size());
        assertEquals(1, app.getAllSites().size());
        assertEquals(1, app.getAllTrucks().size());
        assertEquals(1, app.getAllDrivers().size());
    }

    @Test
    void findMethods_whenEntitiesExist_returnThemSuccessfully() {
        DeliveriesApplication app = createApplicationWithBaseData();

        assertNotNull(app.findShippingZoneByCode("NORTH"));
        assertNotNull(app.findSiteByName("North Warehouse"));
        assertNotNull(app.findTruckByLicenseNumber("123-45-678"));
        assertNotNull(app.findDriverByName("Eden"));
    }

    @Test
    void getByIndex_whenIndexIsValid_returnsCorrectEntity() {
        DeliveriesApplication app = createApplicationWithBaseData();

        assertEquals("NORTH", app.getShippingZoneByIndex(0).getZoneCode());
        assertNotNull(app.getSiteByIndex(0));
        assertNotNull(app.getTruckByIndex(0));
        assertNotNull(app.getDriverByIndex(0));
    }

    @Test
    void getByIndex_whenIndexIsOutOfRange_throwsException() {
        DeliveriesApplication app = new DeliveriesApplication();

        assertThrows(IllegalArgumentException.class, () -> app.getShippingZoneByIndex(0));
        assertThrows(IllegalArgumentException.class, () -> app.getSiteByIndex(0));
        assertThrows(IllegalArgumentException.class, () -> app.getTruckByIndex(0));
        assertThrows(IllegalArgumentException.class, () -> app.getDriverByIndex(0));
        assertThrows(IllegalArgumentException.class, () -> app.getDeliveryByIndex(0));
    }

    @Test
    void canAssignDriverToTruck_whenCompatible_returnsTrue() {
        DeliveriesApplication app = new DeliveriesApplication();

        assertTrue(app.canAssignDriverToTruck(createDriverC(), createTruckC()));
    }

    @Test
    void canAssignDriverToTruck_whenIncompatible_returnsFalse() {
        DeliveriesApplication app = new DeliveriesApplication();

        assertFalse(app.canAssignDriverToTruck(createDriverC1(), createTruckC()));
    }

    @Test
    void createDocument_createsDocumentSuccessfully() {
        DeliveriesApplication app = new DeliveriesApplication();
        List<DeliveryItem> items = new ArrayList<>();
        items.add(new DeliveryItem("ITEM-001", "Milk 3%", 10));

        var firstDocument = app.createDocument(items);
        var secondDocument = app.createDocument(new ArrayList<>());

        assertNotNull(firstDocument);
        assertEquals(1, firstDocument.getDocumentNumber());
        assertEquals(1, firstDocument.getItems().size());

        assertNotNull(secondDocument);
        assertEquals(2, secondDocument.getDocumentNumber());
    }

    @Test
    void createPickupAndDropoffStops_createStopsSuccessfully() {
        DeliveriesApplication app = new DeliveriesApplication();

        DeliveryStop pickup = app.createPickupStop(0, createNorthWarehouse());
        DeliveryStop dropoff = app.createDropoffStop(1, createNorthStore());

        assertEquals(0, pickup.getStopOrder());
        assertEquals(1, dropoff.getStopOrder());
        assertFalse(pickup.hasDocument());
        assertTrue(dropoff.hasDocument());
    }

    @Test
    void planDelivery_validInput_createsDeliverySuccessfully() {
        DeliveriesApplication app = createApplicationWithBaseData();

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(app.createPickupStop(0, createNorthWarehouse()));
        stops.add(app.createDropoffStop(1, createNorthStore()));

        Delivery delivery = app.planDelivery(
                LocalDate.of(2026, 4, 20),
                createNorthWarehouse(),
                stops,
                LocalTime.of(10, 30),
                7000,
                createTruckC(),
                createDriverC(),
                createNorthZone()
        );

        assertNotNull(delivery);
        assertEquals(1, app.getAllDeliveries().size());
        assertEquals(DeliveryStatus.PLANNED, delivery.getStatus());
    }

    @Test
    void recordWeightMeasurement_updatesDeliveryWeight() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        app.recordWeightMeasurement(delivery, 8500);

        assertEquals(8500, delivery.getFinalMeasuredWeightBeforeDeparture());
    }

    @Test
    void isOverweight_whenWeightExceedsLimit_returnsTrue() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        app.recordWeightMeasurement(delivery, 12000);

        assertTrue(app.isOverweight(delivery));
    }

    @Test
    void markDeliveryForReplan_setsPendingReplan() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        app.markDeliveryForReplan(delivery);

        assertEquals(DeliveryStatus.PENDING_REPLAN, delivery.getStatus());
    }

    @Test
    void replaceTruck_withCompatibleTruck_replacesTruckSuccessfully() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        Truck newTruck = new Truck("555-55-555", "Scania", 7000, 18000, LicenseType.C);
        app.replaceTruck(delivery, newTruck);

        assertEquals("555-55-555", delivery.getTruck().getLicenseNumber());
    }

    @Test
    void replaceDriver_withCompatibleDriver_replacesDriverSuccessfully() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        Driver newDriver = new Driver("Amit", Set.of(LicenseType.C));
        app.replaceDriver(delivery, newDriver);

        assertEquals("Amit", delivery.getDriver().getDriverName());
    }

    @Test
    void addStopToDelivery_addsStopSuccessfully() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        DeliveryStop newStop = app.createDropoffStop(99, createNorthStore());
        app.addStopToDelivery(delivery, newStop);

        assertEquals(3, delivery.getStops().size());
    }

    @Test
    void removeStopFromDeliveryByOrder_removesStopSuccessfully() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        app.removeStopFromDeliveryByOrder(delivery, 1);

        assertEquals(1, delivery.getStops().size());
    }

    @Test
    void updateStopDocumentItems_updatesDocumentSuccessfully() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        List<DeliveryItem> items = new ArrayList<>();
        items.add(new DeliveryItem("ITEM-009", "Rice", 20));

        app.updateStopDocumentItems(delivery, 1, items);

        assertTrue(delivery.getStops().get(1).getDocument().containsItem("ITEM-009"));
    }

    @Test
    void dispatchDelivery_validDelivery_setsStatusToDispatched() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        app.dispatchDelivery(delivery);

        assertEquals(DeliveryStatus.DISPATCHED, delivery.getStatus());
    }

    @Test
    void getDeliverySummary_returnsNonEmptySummary() {
        DeliveriesApplication app = createApplicationWithBaseData();
        Delivery delivery = createManagedDelivery(app);

        String summary = app.getDeliverySummary(delivery);

        assertNotNull(summary);
        assertFalse(summary.isBlank());
        assertTrue(summary.contains("North Warehouse"));
        assertTrue(summary.contains("PLANNED"));
    }

    @Test
    void getAllSummaries_returnExpectedSizes() {
        DeliveriesApplication app = createApplicationWithBaseData();
        createManagedDelivery(app);

        assertEquals(app.getAllDeliveries().size(), app.getAllDeliverySummaries().size());
        assertEquals(app.getAllSites().size(), app.getAllSiteSummaries().size());
        assertEquals(app.getAllTrucks().size(), app.getAllTruckSummaries().size());
        assertEquals(app.getAllDrivers().size(), app.getAllDriverSummaries().size());
        assertEquals(app.getAllShippingZones().size(), app.getAllShippingZoneSummaries().size());
    }

    @Test
    void getDeliveryStatus_nullDelivery_throwsException() {
        DeliveriesApplication app = new DeliveriesApplication();

        assertThrows(IllegalArgumentException.class, () ->
                app.getDeliveryStatus(null)
        );
    }
}