package tests;

import domain.Delivery;
import domain.DeliveryDocument;
import domain.DeliveryForm;
import domain.DeliveryItem;
import domain.DeliveryManager;
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

public class DeliveryManagerTest {

    private ShippingZone createNorthZone() {
        return new ShippingZone("NORTH", "Northern Zone");
    }

    private ShippingZone createSouthZone() {
        return new ShippingZone("SOUTH", "Southern Zone");
    }

    private Site createNorthSource() {
        return new Site("North Warehouse", "10 Haifa Port Rd", "04-1111111", "Dana Levi", createNorthZone());
    }

    private Site createNorthDropoffSite() {
        return new Site("North Store", "25 Market St", "04-2222222", "Yossi Cohen", createNorthZone());
    }

    private Site createSouthSite() {
        return new Site("South Store", "5 Negev Rd", "08-3333333", "Noa Levi", createSouthZone());
    }

    private Truck createTruckRequiringC() {
        return new Truck("123-45-678", "Volvo", 5000, 10000, LicenseType.C);
    }

    private Truck createTruckRequiringC1() {
        return new Truck("987-65-432", "MAN", 4000, 9000, LicenseType.C1);
    }

    private Truck createHeavyTruckRequiringC() {
        return new Truck("555-55-555", "Scania", 7000, 18000, LicenseType.C);
    }

    private Driver createDriverWithC() {
        return new Driver("Eden", Set.of(LicenseType.C, LicenseType.B));
    }

    private Driver createDriverWithC1Only() {
        return new Driver("Noa", Set.of(LicenseType.C1));
    }

    private DeliveryItem createMilkItem(int quantity) {
        return new DeliveryItem("ITEM-001", "Milk 3%", quantity);
    }

    private DeliveryDocument createDocument(int documentNumber) {
        List<DeliveryItem> items = new ArrayList<>();
        items.add(createMilkItem(10));
        return new DeliveryDocument(documentNumber, items);
    }

    private DeliveryStop createNorthPickupStop(int order) {
        return new DeliveryStop(order, StopType.PICKUP, createNorthSource());
    }

    private DeliveryStop createNorthDropoffStopWithoutDocument(int order) {
        return new DeliveryStop(order, StopType.DROPOFF, createNorthDropoffSite());
    }

    private DeliveryStop createNorthDropoffStopWithDocument(int order, int documentNumber) {
        return new DeliveryStop(order, StopType.DROPOFF, createNorthDropoffSite(), createDocument(documentNumber));
    }

    private DeliveryStop createSouthDropoffStop(int order) {
        return new DeliveryStop(order, StopType.DROPOFF, createSouthSite(), createDocument(9999));
    }

    private DeliveryManager createManagerWithRegisteredBaseData() {
        DeliveryManager manager = new DeliveryManager();
        manager.addShippingZone(createNorthZone());
        manager.addShippingZone(createSouthZone());
        manager.addSite(createNorthSource());
        manager.addSite(createNorthDropoffSite());
        manager.addSite(createSouthSite());
        manager.addTruck(createTruckRequiringC());
        manager.addTruck(createTruckRequiringC1());
        manager.addTruck(createHeavyTruckRequiringC());
        manager.addDriver(createDriverWithC());
        manager.addDriver(createDriverWithC1Only());
        return manager;
    }

    private Delivery createValidManagedDelivery(DeliveryManager manager) {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthPickupStop(0));
        stops.add(createNorthDropoffStopWithoutDocument(1));

        return manager.createDelivery(
                LocalDate.of(2026, 4, 20),
                createNorthSource(),
                stops,
                LocalTime.of(10, 30),
                7000,
                createTruckRequiringC(),
                createDriverWithC(),
                createNorthZone()
        );
    }

    @Test
    void constructor_createsEmptyManagerSuccessfully() {
        DeliveryManager manager = new DeliveryManager();

        assertNotNull(manager);
        assertTrue(manager.getDeliveries().isEmpty());
        assertTrue(manager.getSites().isEmpty());
        assertTrue(manager.getTrucks().isEmpty());
        assertTrue(manager.getDrivers().isEmpty());
        assertTrue(manager.getShippingZones().isEmpty());
        assertEquals(1, manager.getNextDocumentNumber());
    }

    @Test
    void addShippingZone_validZone_addsZoneSuccessfully() {
        DeliveryManager manager = new DeliveryManager();
        ShippingZone zone = createNorthZone();

        manager.addShippingZone(zone);

        assertEquals(1, manager.getShippingZones().size());
        assertEquals(zone, manager.findShippingZoneByCode("NORTH"));
    }

    @Test
    void addShippingZone_duplicateCode_throwsException() {
        DeliveryManager manager = new DeliveryManager();
        manager.addShippingZone(createNorthZone());

        assertThrows(IllegalArgumentException.class, () ->
                manager.addShippingZone(new ShippingZone("NORTH", "Another North"))
        );
    }

    @Test
    void addSite_duplicateName_throwsException() {
        DeliveryManager manager = new DeliveryManager();
        manager.addSite(createNorthSource());

        assertThrows(IllegalArgumentException.class, () ->
                manager.addSite(new Site(
                        "North Warehouse",
                        "Another Address",
                        "0500000000",
                        "Another Contact",
                        createNorthZone()
                ))
        );
    }

    @Test
    void addTruck_duplicateLicenseNumber_throwsException() {
        DeliveryManager manager = new DeliveryManager();
        manager.addTruck(createTruckRequiringC());

        assertThrows(IllegalArgumentException.class, () ->
                manager.addTruck(new Truck("123-45-678", "Another Model", 4000, 9000, LicenseType.C))
        );
    }

    @Test
    void addDriver_duplicateName_throwsException() {
        DeliveryManager manager = new DeliveryManager();
        manager.addDriver(createDriverWithC());

        assertThrows(IllegalArgumentException.class, () ->
                manager.addDriver(new Driver("Eden", Set.of(LicenseType.C1)))
        );
    }

    @Test
    void findMethods_whenEntitiesExist_returnThemSuccessfully() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();

        assertNotNull(manager.findSiteByName("North Warehouse"));
        assertNotNull(manager.findTruckByLicenseNumber("123-45-678"));
        assertNotNull(manager.findDriverByName("Eden"));
        assertNotNull(manager.findShippingZoneByCode("NORTH"));
    }

    @Test
    void canAssignDriverToTruck_whenDriverHasRequiredLicense_returnsTrue() {
        DeliveryManager manager = new DeliveryManager();

        assertTrue(manager.canAssignDriverToTruck(createDriverWithC(), createTruckRequiringC()));
    }

    @Test
    void canAssignDriverToTruck_whenDriverLacksRequiredLicense_returnsFalse() {
        DeliveryManager manager = new DeliveryManager();

        assertFalse(manager.canAssignDriverToTruck(createDriverWithC1Only(), createTruckRequiringC()));
    }

    @Test
    void canPlanDeliveryInZone_whenSourceAndAllStopsInSameZone_returnsTrue() {
        DeliveryManager manager = new DeliveryManager();

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthPickupStop(0));
        stops.add(createNorthDropoffStopWithoutDocument(1));

        assertTrue(manager.canPlanDeliveryInZone(createNorthSource(), stops, createNorthZone()));
    }

    @Test
    void canPlanDeliveryInZone_whenOneStopInDifferentZone_returnsFalse() {
        DeliveryManager manager = new DeliveryManager();

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthPickupStop(0));
        stops.add(createSouthDropoffStop(1));

        assertFalse(manager.canPlanDeliveryInZone(createNorthSource(), stops, createNorthZone()));
    }

    @Test
    void generateNextDocumentNumber_returnsSequentialNumbers() {
        DeliveryManager manager = new DeliveryManager();

        int first = manager.generateNextDocumentNumber();
        int second = manager.generateNextDocumentNumber();

        assertEquals(1, first);
        assertEquals(2, second);
    }

    @Test
    void createDocument_createsDocumentWithGeneratedNumber() {
        DeliveryManager manager = new DeliveryManager();
        List<DeliveryItem> items = new ArrayList<>();
        items.add(createMilkItem(8));

        DeliveryDocument document = manager.createDocument(items);

        assertNotNull(document);
        assertEquals(1, document.getDocumentNumber());
        assertEquals(1, document.getItems().size());
        assertEquals(2, manager.getNextDocumentNumber());
    }

    @Test
    void createDelivery_validInput_createsAndRegistersDeliverySuccessfully() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthPickupStop(0));
        stops.add(createNorthDropoffStopWithoutDocument(1));

        Delivery delivery = manager.createDelivery(
                LocalDate.of(2026, 4, 20),
                createNorthSource(),
                stops,
                LocalTime.of(10, 30),
                7000,
                createTruckRequiringC(),
                createDriverWithC(),
                createNorthZone()
        );

        assertNotNull(delivery);
        assertEquals(1, manager.getDeliveries().size());
        assertEquals(DeliveryStatus.PLANNED, delivery.getStatus());
        assertTrue(delivery.getStops().get(1).hasDocument());
    }

    @Test
    void createDelivery_driverWithoutRequiredLicense_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthPickupStop(0));
        stops.add(createNorthDropoffStopWithoutDocument(1));

        assertThrows(IllegalArgumentException.class, () ->
                manager.createDelivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSource(),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruckRequiringC(),
                        createDriverWithC1Only(),
                        createNorthZone()
                )
        );
    }

    @Test
    void createDelivery_withStopInDifferentZone_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthPickupStop(0));
        stops.add(createSouthDropoffStop(1));

        assertThrows(IllegalArgumentException.class, () ->
                manager.createDelivery(
                        LocalDate.of(2026, 4, 20),
                        createNorthSource(),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruckRequiringC(),
                        createDriverWithC(),
                        createNorthZone()
                )
        );
    }

    @Test
    void createDelivery_whenInitialWeightIsOverLimit_setsPendingReplan() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthPickupStop(0));
        stops.add(createNorthDropoffStopWithoutDocument(1));

        Delivery delivery = manager.createDelivery(
                LocalDate.of(2026, 4, 20),
                createNorthSource(),
                stops,
                LocalTime.of(10, 30),
                12000,
                createTruckRequiringC(),
                createDriverWithC(),
                createNorthZone()
        );

        assertEquals(DeliveryStatus.PENDING_REPLAN, delivery.getStatus());
    }

    @Test
    void recordWeightMeasurement_validMeasurement_updatesWeight() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        manager.recordWeightMeasurement(delivery, 8200);

        assertEquals(8200, delivery.getFinalMeasuredWeightBeforeDeparture());
        assertEquals(DeliveryStatus.PLANNED, delivery.getStatus());
    }

    @Test
    void recordWeightMeasurement_whenWeightExceedsLimit_setsPendingReplan() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        manager.recordWeightMeasurement(delivery, 12000);

        assertEquals(DeliveryStatus.PENDING_REPLAN, delivery.getStatus());
        assertTrue(manager.isOverweight(delivery));
    }

    @Test
    void markDeliveryForReplan_setsStatusToPendingReplan() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        manager.markDeliveryForReplan(delivery);

        assertEquals(DeliveryStatus.PENDING_REPLAN, delivery.getStatus());
    }

    @Test
    void replaceTruck_withCompatibleTruck_replacesTruckSuccessfully() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        manager.replaceTruck(delivery, createHeavyTruckRequiringC());

        assertEquals("555-55-555", delivery.getTruck().getLicenseNumber());
    }

    @Test
    void replaceTruck_whenCurrentDriverCannotDriveNewTruck_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        delivery.setDriver(createDriverWithC1Only());

        assertThrows(IllegalArgumentException.class, () ->
                manager.replaceTruck(delivery, createHeavyTruckRequiringC())
        );
    }

    @Test
    void replaceDriver_withCompatibleDriver_replacesDriverSuccessfully() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        Driver newDriver = new Driver("Amit", Set.of(LicenseType.C));
        manager.replaceDriver(delivery, newDriver);

        assertEquals("Amit", delivery.getDriver().getDriverName());
    }

    @Test
    void replaceDriver_whenDriverCannotDriveCurrentTruck_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        assertThrows(IllegalArgumentException.class, () ->
                manager.replaceDriver(delivery, createDriverWithC1Only())
        );
    }

    @Test
    void addStop_validStopInSameZone_addsStopSuccessfully() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        DeliveryStop newStop = createNorthDropoffStopWithoutDocument(99);
        manager.addStop(delivery, newStop);

        assertEquals(3, delivery.getStops().size());
        assertEquals(2, delivery.getStops().get(2).getStopOrder());
        assertTrue(delivery.getStops().get(2).hasDocument());
    }

    @Test
    void addStop_stopFromDifferentZone_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        assertThrows(IllegalArgumentException.class, () ->
                manager.addStop(delivery, createSouthDropoffStop(5))
        );
    }

    @Test
    void removeStopByOrder_existingStop_removesItAndReassignsOrders() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        manager.addStop(delivery, createNorthDropoffStopWithoutDocument(5));
        assertEquals(3, delivery.getStops().size());

        manager.removeStopByOrder(delivery, 1);

        assertEquals(2, delivery.getStops().size());
        assertEquals(0, delivery.getStops().get(0).getStopOrder());
        assertEquals(1, delivery.getStops().get(1).getStopOrder());
    }

    @Test
    void removeStopByOrder_nonExistingStop_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        assertThrows(IllegalArgumentException.class, () ->
                manager.removeStopByOrder(delivery, 99)
        );
    }

    @Test
    void updateStopDocumentItems_existingStop_updatesDocumentSuccessfully() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        List<DeliveryItem> newItems = new ArrayList<>();
        newItems.add(new DeliveryItem("ITEM-009", "Rice", 20));

        manager.updateStopDocumentItems(delivery, 1, newItems);

        DeliveryStop updatedStop = delivery.getStops().get(1);
        assertNotNull(updatedStop.getDocument());
        assertTrue(updatedStop.getDocument().containsItem("ITEM-009"));
    }

    @Test
    void updateStopDocumentItems_nonExistingStop_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        List<DeliveryItem> newItems = new ArrayList<>();
        newItems.add(new DeliveryItem("ITEM-009", "Rice", 20));

        assertThrows(IllegalArgumentException.class, () ->
                manager.updateStopDocumentItems(delivery, 99, newItems)
        );
    }

    @Test
    void dispatchDelivery_whenEverythingIsValid_setsStatusToDispatched() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        manager.dispatchDelivery(delivery);

        assertEquals(DeliveryStatus.DISPATCHED, delivery.getStatus());
    }

    @Test
    void dispatchDelivery_whenOverweight_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        manager.recordWeightMeasurement(delivery, 12000);

        assertThrows(IllegalStateException.class, () ->
                manager.dispatchDelivery(delivery)
        );
    }

    @Test
    void dispatchDelivery_whenDriverNotCompatible_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        Delivery delivery = createValidManagedDelivery(manager);

        Driver incompatibleDriver = new Driver("Lior", Set.of(LicenseType.C1));
        delivery.setDriver(incompatibleDriver);

        assertThrows(IllegalStateException.class, () ->
                manager.dispatchDelivery(delivery)
        );
    }

    @Test
    void dispatchDelivery_whenStopOutsideZone_throwsException() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();

        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createNorthPickupStop(0));
        stops.add(createSouthDropoffStop(1));

        Delivery delivery = new Delivery(
                LocalDate.of(2026, 4, 20),
                createNorthSource(),
                stops,
                LocalTime.of(10, 30),
                7000,
                createTruckRequiringC(),
                createDriverWithC(),
                createNorthZone(),
                DeliveryStatus.PLANNED,
                new DeliveryForm()
        );

        manager.addDelivery(delivery);

        assertThrows(IllegalStateException.class, () ->
                manager.dispatchDelivery(delivery)
        );
    }

    @Test
    void clearAllData_clearsAllCollectionsAndResetsDocumentCounter() {
        DeliveryManager manager = createManagerWithRegisteredBaseData();
        createValidManagedDelivery(manager);
        manager.generateNextDocumentNumber();

        manager.clearAllData();

        assertTrue(manager.getDeliveries().isEmpty());
        assertTrue(manager.getSites().isEmpty());
        assertTrue(manager.getTrucks().isEmpty());
        assertTrue(manager.getDrivers().isEmpty());
        assertTrue(manager.getShippingZones().isEmpty());
        assertEquals(1, manager.getNextDocumentNumber());
    }

    @Test
    void loadSampleData_loadsEntitiesAndAtLeastOneDelivery() {
        DeliveryManager manager = new DeliveryManager();

        manager.loadSampleData();

        assertFalse(manager.getShippingZones().isEmpty());
        assertFalse(manager.getSites().isEmpty());
        assertFalse(manager.getTrucks().isEmpty());
        assertFalse(manager.getDrivers().isEmpty());
        assertFalse(manager.getDeliveries().isEmpty());
    }
}