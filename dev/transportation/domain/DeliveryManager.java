package transportation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import dataaccess.repository.impl.EmployeeRepositoryImpl;
import dataaccess.repository.impl.ShiftRepositoryImpl;
import employee.service.EmployeeTransportationService;

public class DeliveryManager {

    private List<Delivery> deliveries;
    int nextDeliveryId;
    private List<Site> sites;
    private List<Truck> trucks;
    private List<Driver> drivers;
    private List<ShippingZone> shippingZones;
    private int nextDocumentNumber;
    private final EmployeeTransportationService employeeTransportationService;

    public DeliveryManager() {
        this(new EmployeeTransportationService(
                new ShiftRepositoryImpl(),
                new EmployeeRepositoryImpl()));
    }
    public DeliveryManager(EmployeeTransportationService employeeTransportationService) {
        if (employeeTransportationService == null) {
            throw new IllegalArgumentException("employeeTransportationService cannot be null");
        }
        this.nextDeliveryId = 1;
        this.deliveries = new ArrayList<>();
        this.sites = new ArrayList<>();
        this.trucks = new ArrayList<>();
        this.drivers = new ArrayList<>();
        this.shippingZones = new ArrayList<>();
        this.nextDocumentNumber = 1;
        this.employeeTransportationService = employeeTransportationService;
    }

    public List<Delivery> getDeliveries() {
        return new ArrayList<>(deliveries);
    }

    public List<Site> getSites() {
        return new ArrayList<>(sites);
    }

    public List<Truck> getTrucks() {
        return new ArrayList<>(trucks);
    }

    public List<Driver> getDrivers() {
        return new ArrayList<>(drivers);
    }

    public List<ShippingZone> getShippingZones() {
        return new ArrayList<>(shippingZones);
    }

    public int getNextDocumentNumber() {
        return nextDocumentNumber;
    }

    public List<Driver> getAvailableDriversForDelivery(LocalDate date, LocalTime time, Truck truck) {
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        if (time == null) {
            throw new IllegalArgumentException("time cannot be null");
        }
        if (truck == null) {
            throw new IllegalArgumentException("truck cannot be null");
        }

        LocalDateTime departureDateTime = LocalDateTime.of(date, time);
        List<Driver> availableDrivers = new ArrayList<>();

        for (Driver driver : drivers) {
            if (driver.canDrive(truck)
                    && employeeTransportationService.canRequestDriverForDeliveryShift(
                            driver.getEmployeeId(),
                            departureDateTime)) {
                availableDrivers.add(driver);
            }
        }

        return availableDrivers;
    }

    public void addDelivery(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("delivery cannot be null");
        }
        deliveries.add(delivery);
        if (delivery.getDeliveryId() >= nextDeliveryId) {
            nextDeliveryId = delivery.getDeliveryId() + 1;
        }
    }

    public void addSite(Site site) {
        if (site == null) {
            throw new IllegalArgumentException("site cannot be null");
        }
        if (findSiteByName(site.getSiteName()) != null) {
            throw new IllegalArgumentException("site with the same name already exists");
        }
        sites.add(site);
    }

    public void addTruck(Truck truck) {
        if (truck == null) {
            throw new IllegalArgumentException("truck cannot be null");
        }
        if (findTruckByLicenseNumber(truck.getLicenseNumber()) != null) {
            throw new IllegalArgumentException("truck with the same license number already exists");
        }
        trucks.add(truck);
    }

    public void addDriver(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("driver cannot be null");
        }
        if (findDriverByName(driver.getDriverName()) != null) {
            throw new IllegalArgumentException("driver with the same name already exists");
        }
        drivers.add(driver);
    }

    public void addShippingZone(ShippingZone shippingZone) {
        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }
        if (findShippingZoneByCode(shippingZone.getZoneCode()) != null) {
            throw new IllegalArgumentException("shipping zone with the same code already exists");
        }
        shippingZones.add(shippingZone);
    }

    public Site findSiteByName(String siteName) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("siteName cannot be empty");
        }

        for (Site site : sites) {
            if (site.getSiteName().equals(siteName)) {
                return site;
            }
        }
        return null;
    }

    public Truck findTruckByLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException("licenseNumber cannot be empty");
        }

        for (Truck truck : trucks) {
            if (truck.getLicenseNumber().equals(licenseNumber)) {
                return truck;
            }
        }
        return null;
    }

    public Driver findDriverByName(String driverName) {
        if (driverName == null || driverName.isBlank()) {
            throw new IllegalArgumentException("driverName cannot be empty");
        }

        for (Driver driver : drivers) {
            if (driver.getDriverName().equals(driverName)) {
                return driver;
            }
        }
        return null;
    }

    public ShippingZone findShippingZoneByCode(String zoneCode) {
        if (zoneCode == null || zoneCode.isBlank()) {
            throw new IllegalArgumentException("zoneCode cannot be empty");
        }

        for (ShippingZone shippingZone : shippingZones) {
            if (shippingZone.getZoneCode().equals(zoneCode)) {
                return shippingZone;
            }
        }
        return null;
    }

    public boolean canAssignDriverToTruck(Driver driver, Truck truck) {
        if (driver == null) {
            throw new IllegalArgumentException("driver cannot be null");
        }
        if (truck == null) {
            throw new IllegalArgumentException("truck cannot be null");
        }

        return driver.canDrive(truck);
    }

    public boolean canPlanDeliveryInZone(Site source, List<DeliveryStop> stops, ShippingZone shippingZone) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (stops == null) {
            throw new IllegalArgumentException("stops cannot be null");
        }
        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }

        if (!source.belongsToZone(shippingZone)) {
            return false;
        }

        for (DeliveryStop stop : stops) {
            if (stop == null || !stop.belongsToZone(shippingZone)) {
                return false;
            }
        }

        return true;
    }

    public int generateNextDocumentNumber() {
        return nextDocumentNumber++;
    }

    public DeliveryDocument createDocument(List<DeliveryItem> items) {
        if (items == null) {
            throw new IllegalArgumentException("items cannot be null");
        }

        return new DeliveryDocument(generateNextDocumentNumber(), items);
    }

    public Delivery createDelivery(LocalDate deliveryDate,
                                   Site source,
                                   List<DeliveryStop> stops,
                                   LocalTime departureTime,
                                   double initialWeight,
                                   Truck truck,
                                   Driver driver,
                                   ShippingZone shippingZone) {
        if (deliveryDate == null) {
            throw new IllegalArgumentException("deliveryDate cannot be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (stops == null || stops.isEmpty()) {
            throw new IllegalArgumentException("stops cannot be null or empty");
        }
        if (departureTime == null) {
            throw new IllegalArgumentException("departureTime cannot be null");
        }
        if (truck == null) {
            throw new IllegalArgumentException("truck cannot be null");
        }
        if (driver == null) {
            throw new IllegalArgumentException("driver cannot be null");
        }
        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }

        if (!canAssignDriverToTruck(driver, truck)) {
            throw new IllegalArgumentException("driver is not licensed for the selected truck");
        }

        if (!canPlanDeliveryInZone(source, stops, shippingZone)) {
            throw new IllegalArgumentException("source and all stops must belong to the selected shipping zone");
        }

        prepareStopsForCreation(stops);

        DeliveryForm deliveryForm = new DeliveryForm();
        deliveryForm.addWeightMeasurement(initialWeight);

        int deliveryId = nextDeliveryId++;
        Delivery delivery = new Delivery(
            deliveryId,
            deliveryDate,
            source,
            stops,
            departureTime,
            initialWeight,
            truck,
            driver,
            shippingZone,
            DeliveryStatus.PLANNED,
            deliveryForm
        );

        LocalDateTime deliveryDateTime = LocalDateTime.of(delivery.getDeliveryDate(), delivery.getDepartureTime());
        employeeTransportationService.createDriverAssignmentRequest(
            driver.getEmployeeId(),
            delivery.getDeliveryId(),
            deliveryDateTime
        );

        if (delivery.isOverweight()) {
            delivery.setStatus(DeliveryStatus.PENDING_REPLAN);
        }

        deliveries.add(delivery);
        
        return delivery;
        
    }

    public void cancelDelivery(Delivery delivery) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);
        delivery.setStatus(DeliveryStatus.CANCELLED);
    }

    public void recordWeightMeasurement(Delivery delivery, double weight) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);

        delivery.recordWeightMeasurement(weight);
    }

    public boolean isOverweight(Delivery delivery) {
        validateRegisteredDelivery(delivery);
        return delivery.isOverweight();
    }

    public void markDeliveryForReplan(Delivery delivery) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);

        delivery.markForReplan();
    }

    public void replaceTruck(Delivery delivery, Truck newTruck) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);

        if (newTruck == null) {
            throw new IllegalArgumentException("newTruck cannot be null");
        }
        if (!delivery.getDriver().canDrive(newTruck)) {
            throw new IllegalArgumentException("current driver cannot drive the new truck");
        }
        if (!newTruck.canCarryWeight(delivery.getFinalMeasuredWeightBeforeDeparture())) {
            throw new IllegalArgumentException("Truck cannot carry current delivery weight");
        }

        delivery.setTruck(newTruck);
    }

    public void replaceDriver(Delivery delivery, Driver newDriver) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);

        if (newDriver == null) {
            throw new IllegalArgumentException("newDriver cannot be null");
        }
        if (!newDriver.canDrive(delivery.getTruck())) {
            throw new IllegalArgumentException("new driver is not licensed for the current truck");
        }

        delivery.setDriver(newDriver);

        employeeTransportationService.createDriverAssignmentRequest(
            newDriver.getEmployeeId(),
            delivery.getDeliveryId(),
            LocalDateTime.of(delivery.getDeliveryDate(), delivery.getDepartureTime()));
    }

    public void addStop(Delivery delivery, DeliveryStop newStop) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);

        if (newStop == null) {
            throw new IllegalArgumentException("newStop cannot be null");
        }
        if (!newStop.belongsToZone(delivery.getShippingZone())) {
            throw new IllegalArgumentException("new stop must belong to the delivery shipping zone");
        }

        if (newStop.getStopType() == StopType.DROPOFF && !newStop.hasDocument()) {
            newStop.setDocument(createDocument(new ArrayList<>()));
        }

        newStop.setStopOrder(delivery.getStops().size());
        delivery.addStop(newStop);
    }

    public void removeStopByOrder(Delivery delivery, int stopOrder) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);

        DeliveryStop targetStop = null;
        for (DeliveryStop stop : delivery.getStops()) {
            if (stop.getStopOrder() == stopOrder) {
                targetStop = stop;
                break;
            }
        }

        if (targetStop == null) {
            throw new IllegalArgumentException("stop with the given order was not found");
        }

        delivery.removeStop(targetStop);
        reassignStopOrders(delivery);
    }

    public void updateStopDocumentItems(Delivery delivery, int stopOrder, List<DeliveryItem> updatedItems) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);

        if (updatedItems == null) {
            throw new IllegalArgumentException("updatedItems cannot be null");
        }

        DeliveryStop targetStop = null;
        for (DeliveryStop stop : delivery.getStops()) {
            if (stop.getStopOrder() == stopOrder) {
                targetStop = stop;
                break;
            }
        }

        if (targetStop == null) {
            throw new IllegalArgumentException("stop with the given order was not found");
        }

        DeliveryDocument updatedDocument = createDocument(updatedItems);
        targetStop.setDocument(updatedDocument);
    }

    public void dispatchDelivery(Delivery delivery) {
        validateRegisteredDelivery(delivery);
        ensureDeliveryIsStillModifiable(delivery);

        if (!delivery.getDriver().canDrive(delivery.getTruck())) {
            throw new IllegalStateException("cannot dispatch delivery: driver is not licensed for truck");
        }

        if (!delivery.allStopsBelongToShippingZone()) {
            throw new IllegalStateException("cannot dispatch delivery: all stops must belong to shipping zone");
        }

        if (delivery.isOverweight()) {
            throw new IllegalStateException("cannot dispatch delivery: delivery is overweight");
        }

        LocalDateTime departureDateTime = LocalDateTime.of(delivery.getDeliveryDate(), delivery.getDepartureTime());

        if (shouldValidateEmployeeShiftIntegration(delivery)) {
            if (!employeeTransportationService.isDriverAssignedToShift(
                    delivery.getDriver().getEmployeeId(),
                    departureDateTime)) {
                throw new IllegalStateException("Driver is not assigned to the delivery shift");
            }

            for (DeliveryStop stop : delivery.getStops()) {
                if (!employeeTransportationService.hasStorekeeperInShift(
                        stop.getPlannedArrivalDateTime())) {
                    throw new IllegalStateException("No storekeeper assigned for delivery arrival time");
                }
            }
        }
        delivery.markAsDispatched();
    }

    public void clearAllData() {
        deliveries.clear();
        sites.clear();
        trucks.clear();
        drivers.clear();
        shippingZones.clear();
        nextDeliveryId = 1;
        nextDocumentNumber = 1;
    }

    private void prepareStopsForCreation(List<DeliveryStop> stops) {
        for (int i = 0; i < stops.size(); i++) {
            DeliveryStop stop = stops.get(i);

            if (stop == null) {
                throw new IllegalArgumentException("stops cannot contain null values");
            }

            stop.setStopOrder(i);

            if (stop.getStopType() == StopType.DROPOFF && !stop.hasDocument()) {
                stop.setDocument(createDocument(new ArrayList<>()));
            }
        }
    }

    private void reassignStopOrders(Delivery delivery) {
        List<DeliveryStop> currentStops = delivery.getStops();
        for (int i = 0; i < currentStops.size(); i++) {
            currentStops.get(i).setStopOrder(i);
        }
    }

    private void validateRegisteredDelivery(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("delivery cannot be null");
        }
        if (!deliveries.contains(delivery)) {
            throw new IllegalArgumentException("delivery is not managed by this manager");
        }
    }

    public boolean validateWarehouseWorkerForArrival(Site site, LocalDateTime arrivalDateTime) {
        if (site == null) {
            throw new IllegalArgumentException("site cannot be null");
        }
        if (arrivalDateTime == null) {
            throw new IllegalArgumentException("arrivalDateTime cannot be null");
        }

        return employeeTransportationService.hasStorekeeperInShift(arrivalDateTime);
    }

    private void ensureDeliveryIsStillModifiable(Delivery delivery) {
        if (!delivery.canStillBeModified()) {
            throw new IllegalStateException("delivery can no longer be modified");
        }
    }

    private boolean shouldValidateEmployeeShiftIntegration(Delivery delivery) {
        String employeeId = delivery.getDriver().getEmployeeId();
        return employeeId != null && !employeeId.startsWith("LEGACY-");
    }
}
