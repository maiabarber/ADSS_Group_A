package service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import domain.Delivery;
import domain.DeliveryDocument;
import domain.DeliveryItem;
import domain.DeliveryManager;
import domain.DeliveryStatus;
import domain.DeliveryStop;
import domain.Driver;
import domain.ShippingZone;
import domain.Site;
import domain.StopType;
import domain.Truck;

public class DeliveriesApplication {

    private DeliveryManager deliveryManager;

    public DeliveriesApplication() {
        this.deliveryManager = new DeliveryManager();
    }

    public DeliveriesApplication(DeliveryManager deliveryManager) {
        if (deliveryManager == null) {
            throw new IllegalArgumentException("deliveryManager cannot be null");
        }
        this.deliveryManager = deliveryManager;
    }

    public DeliveryManager getDeliveryManager() {
        return deliveryManager;
    }

    // ============================== Initialization ==============================

    public void initializeEmpty() {
        deliveryManager.clearAllData();
    }

    public void initializeWithSampleData() {
        deliveryManager.loadSampleData();
    }

    // ============================== Read-only accessors ==============================

    public List<Delivery> getAllDeliveries() {
        return deliveryManager.getDeliveries();
    }

    public List<Site> getAllSites() {
        return deliveryManager.getSites();
    }

    public List<Truck> getAllTrucks() {
        return deliveryManager.getTrucks();
    }

    public List<Driver> getAllDrivers() {
        return deliveryManager.getDrivers();
    }

    public List<ShippingZone> getAllShippingZones() {
        return deliveryManager.getShippingZones();
    }

    public Delivery getDeliveryByIndex(int index) {
        List<Delivery> deliveries = getAllDeliveries();
        validateIndex(index, deliveries.size(), "delivery");
        return deliveries.get(index);
    }

    public Site getSiteByIndex(int index) {
        List<Site> sites = getAllSites();
        validateIndex(index, sites.size(), "site");
        return sites.get(index);
    }

    public Truck getTruckByIndex(int index) {
        List<Truck> trucks = getAllTrucks();
        validateIndex(index, trucks.size(), "truck");
        return trucks.get(index);
    }

    public Driver getDriverByIndex(int index) {
        List<Driver> drivers = getAllDrivers();
        validateIndex(index, drivers.size(), "driver");
        return drivers.get(index);
    }

    public ShippingZone getShippingZoneByIndex(int index) {
        List<ShippingZone> shippingZones = getAllShippingZones();
        validateIndex(index, shippingZones.size(), "shipping zone");
        return shippingZones.get(index);
    }

    // ============================== Finders ==============================

    public Site findSiteByName(String siteName) {
        return deliveryManager.findSiteByName(siteName);
    }

    public Truck findTruckByLicenseNumber(String licenseNumber) {
        return deliveryManager.findTruckByLicenseNumber(licenseNumber);
    }

    public Driver findDriverByName(String driverName) {
        return deliveryManager.findDriverByName(driverName);
    }

    public ShippingZone findShippingZoneByCode(String zoneCode) {
        return deliveryManager.findShippingZoneByCode(zoneCode);
    }

    // ============================== Registration / setup ==============================

    public void addShippingZone(ShippingZone shippingZone) {
        deliveryManager.addShippingZone(shippingZone);
    }

    public void addSite(Site site) {
        deliveryManager.addSite(site);
    }

    public void addTruck(Truck truck) {
        deliveryManager.addTruck(truck);
    }

    public void addDriver(Driver driver) {
        deliveryManager.addDriver(driver);
    }

    // ============================== Business helper methods ==============================

    public boolean canAssignDriverToTruck(Driver driver, Truck truck) {
        return deliveryManager.canAssignDriverToTruck(driver, truck);
    }

    public boolean canPlanDeliveryInZone(Site source, List<DeliveryStop> stops, ShippingZone shippingZone) {
        return deliveryManager.canPlanDeliveryInZone(source, stops, shippingZone);
    }

    public DeliveryDocument createDocument(List<DeliveryItem> items) {
        return deliveryManager.createDocument(items);
    }

    public DeliveryStop createPickupStop(int stopOrder, Site site) {
        return new DeliveryStop(stopOrder, StopType.PICKUP, site);
    }

    public DeliveryStop createPickupStop(int stopOrder, Site site, List<DeliveryItem> items) {
        DeliveryDocument document = createDocument(items);
        return new DeliveryStop(stopOrder, StopType.PICKUP, site, document);
    }

    public DeliveryStop createDropoffStop(int stopOrder, Site site, List<DeliveryItem> items) {
        DeliveryDocument document = createDocument(items);
        return new DeliveryStop(stopOrder, StopType.DROPOFF, site, document);
    }

    public DeliveryStop createDropoffStop(int stopOrder, Site site) {
        return new DeliveryStop(stopOrder, StopType.DROPOFF, site, createDocument(new ArrayList<>()));
    }

    // ============================== Delivery lifecycle ==============================

    public Delivery planDelivery(LocalDate deliveryDate,
                                 Site source,
                                 List<DeliveryStop> stops,
                                 LocalTime departureTime,
                                 double initialWeight,
                                 Truck truck,
                                 Driver driver,
                                 ShippingZone shippingZone) {

        return deliveryManager.createDelivery(
                deliveryDate,
                source,
                stops,
                departureTime,
                initialWeight,
                truck,
                driver,
                shippingZone
        );
    }

    public void recordWeightMeasurement(Delivery delivery, double weight) {
        deliveryManager.recordWeightMeasurement(delivery, weight);
    }

    public boolean isOverweight(Delivery delivery) {
        return deliveryManager.isOverweight(delivery);
    }

    public void markDeliveryForReplan(Delivery delivery) {
        deliveryManager.markDeliveryForReplan(delivery);
    }

    public void replaceTruck(Delivery delivery, Truck newTruck) {
        deliveryManager.replaceTruck(delivery, newTruck);
    }

    public void replaceDriver(Delivery delivery, Driver newDriver) {
        deliveryManager.replaceDriver(delivery, newDriver);
    }

    public void addStopToDelivery(Delivery delivery, DeliveryStop newStop) {
        deliveryManager.addStop(delivery, newStop);
    }

    public void removeStopFromDeliveryByOrder(Delivery delivery, int stopOrder) {
        deliveryManager.removeStopByOrder(delivery, stopOrder);
    }

    public void updateStopDocumentItems(Delivery delivery, int stopOrder, List<DeliveryItem> updatedItems) {
        deliveryManager.updateStopDocumentItems(delivery, stopOrder, updatedItems);
    }

    public void dispatchDelivery(Delivery delivery) {
        deliveryManager.dispatchDelivery(delivery);
    }

    // ============================== Convenience methods for UI ==============================

    public String getDeliverySummary(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("delivery cannot be null");
        }

        return "Date: " + delivery.getDeliveryDate() +
                ", Time: " + delivery.getDepartureTime() +
                ", Source: " + delivery.getSource().getSiteName() +
                ", Driver: " + delivery.getDriver().getDriverName() +
                ", Truck: " + delivery.getTruck().getLicenseNumber() +
                ", Zone: " + delivery.getShippingZone().getZoneCode() +
                ", Status: " + delivery.getStatus() +
                ", Stops: " + delivery.getStops().size() +
                ", Final Weight: " + delivery.getFinalMeasuredWeightBeforeDeparture();
    }

    public List<String> getAllDeliverySummaries() {
        List<String> summaries = new ArrayList<>();
        List<Delivery> deliveries = getAllDeliveries();

        for (int i = 0; i < deliveries.size(); i++) {
            summaries.add("[" + i + "] " + getDeliverySummary(deliveries.get(i)));
        }

        return summaries;
    }

    public List<String> getAllSiteSummaries() {
        List<String> summaries = new ArrayList<>();
        List<Site> sites = getAllSites();

        for (int i = 0; i < sites.size(); i++) {
            Site site = sites.get(i);
            summaries.add("[" + i + "] " +
                    site.getSiteName() +
                    " | " + site.getAddress() +
                    " | Zone: " + site.getShippingZone().getZoneCode());
        }

        return summaries;
    }

    public List<String> getAllTruckSummaries() {
        List<String> summaries = new ArrayList<>();
        List<Truck> trucks = getAllTrucks();

        for (int i = 0; i < trucks.size(); i++) {
            Truck truck = trucks.get(i);
            summaries.add("[" + i + "] " +
                    truck.getLicenseNumber() +
                    " | " + truck.getModel() +
                    " | Max Weight: " + truck.getMaxAllowedWeight() +
                    " | Required License: " + truck.getRequiredLicenseType());
        }

        return summaries;
    }

    public List<String> getAllDriverSummaries() {
        List<String> summaries = new ArrayList<>();
        List<Driver> drivers = getAllDrivers();

        for (int i = 0; i < drivers.size(); i++) {
            Driver driver = drivers.get(i);
            summaries.add("[" + i + "] " +
                    driver.getDriverName() +
                    " | Licenses: " + driver.getLicenseTypes());
        }

        return summaries;
    }

    public List<String> getAllShippingZoneSummaries() {
        List<String> summaries = new ArrayList<>();
        List<ShippingZone> shippingZones = getAllShippingZones();

        for (int i = 0; i < shippingZones.size(); i++) {
            ShippingZone shippingZone = shippingZones.get(i);
            summaries.add("[" + i + "] " +
                    shippingZone.getZoneCode() +
                    " | " + shippingZone.getZoneName());
        }

        return summaries;
    }

    public DeliveryStatus getDeliveryStatus(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("delivery cannot be null");
        }
        return delivery.getStatus();
    }

    private void validateIndex(int index, int size, String entityName) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException(entityName + " index is out of range");
        }
    }
}