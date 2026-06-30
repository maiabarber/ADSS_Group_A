package transportation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseSeeder;
import dataaccess.dao.DeliveryDocumentDaoImpl;
import dataaccess.dao.DeliveryItemDaoImpl;
import dataaccess.dao.DeliveryStopDaoImpl;
import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DeliveryDocumentDto;
import dataaccess.dto.DeliveryItemDto;
import dataaccess.dto.DeliveryStopDto;
import dataaccess.dto.DriverDto;
import dataaccess.dto.ShippingZoneDto;
import dataaccess.dto.SiteDto;
import dataaccess.dto.TruckDto;
import dataaccess.repository.DeliveryRepository;
import dataaccess.repository.DriverRepository;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.SiteRepository;
import dataaccess.repository.TruckRepository;
import dataaccess.repository.impl.DatabaseTransportationDataLoader;
import dataaccess.repository.impl.DeliveryRepositoryImpl;
import dataaccess.repository.impl.DriverRepositoryImpl;
import dataaccess.repository.impl.EmployeeRepositoryImpl;
import dataaccess.repository.impl.ShiftRepositoryImpl;
import dataaccess.repository.impl.ShippingZoneRepositoryImpl;
import dataaccess.repository.impl.SiteRepositoryImpl;
import dataaccess.repository.impl.TruckRepositoryImpl;
import employee.presentation.ShiftController;
import employee.service.EmployeeTransportationService;
import transportation.domain.Delivery;
import transportation.domain.DeliveryDocument;
import transportation.domain.DeliveryItem;
import transportation.domain.DeliveryManager;
import transportation.domain.DeliveryStatus;
import transportation.domain.DeliveryStop;
import transportation.domain.Driver;
import transportation.domain.ShippingZone;
import transportation.domain.Site;
import transportation.domain.StopType;
import transportation.domain.Truck;

public class DeliveriesApplication {

    private DeliveryManager deliveryManager;
    private final DeliveryRepository deliveryRepository;
    private final SiteRepository siteRepository;
    private final TruckRepository truckRepository;
    private final DriverRepository driverRepository;
    private final ShippingZoneRepositoryImpl shippingZoneRepository;

    public DeliveriesApplication() {
        this(new EmployeeTransportationService(
                new ShiftRepositoryImpl(),
                new EmployeeRepositoryImpl()));
    }
    public DeliveriesApplication(EmployeeTransportationService employeeTransportationService) {
        this(new DeliveryManager(employeeTransportationService));
        // loadPersistedData();
    }

    public DeliveriesApplication(DeliveryManager deliveryManager) {
        if (deliveryManager == null) {
            throw new IllegalArgumentException("deliveryManager cannot be null");
        }

        this.deliveryManager = deliveryManager;
        this.deliveryRepository = new DeliveryRepositoryImpl();
        this.siteRepository = new SiteRepositoryImpl();
        this.truckRepository = new TruckRepositoryImpl();
        this.driverRepository = new DriverRepositoryImpl();
        this.shippingZoneRepository = new ShippingZoneRepositoryImpl();
    }

    public DeliveryManager getDeliveryManager() {
        return deliveryManager;
    }

    // ============================== Initialization ==============================

    public void initializeEmpty() {
        deliveryManager.clearAllData();
    }

    private void loadPersistedData() {
        try {
            new DatabaseTransportationDataLoader().loadInto(deliveryManager);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load transportation data from database", e);
        }
    }

    public void initializeWithSampleData() {
        deliveryManager.clearAllData();
        try {
            DatabaseSeeder.seedSampleData();
            loadPersistedData();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize sample data from database", e);
        }
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

    public List<Driver> getAvailableDriversForDelivery(LocalDate date, LocalTime time, Truck truck) {
        return deliveryManager.getAvailableDriversForDelivery(date, time, truck);
    }

    public void setShiftController(ShiftController shiftController) {
        // Kept for compatibility. Real shift integration is done through EmployeeTransportationService.
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
        saveShippingZone(shippingZone);
    }

    public void addSite(Site site) {
        Site siteToAdd = site;

        Integer existingSiteId = findExistingSiteIdByName(site.getSiteName());

        if (existingSiteId != null) {
            siteToAdd = new Site(
                    existingSiteId,
                    site.getSiteName(),
                    site.getAddress(),
                    site.getPhoneNumber(),
                    site.getContactName(),
                    site.getShippingZone(),
                    site.getSiteType(),
                    site.getBranch()
            );
        } else if (site.getSiteId() <= 0) {
            int newSiteId = nextSiteId();

            siteToAdd = new Site(
                    newSiteId,
                    site.getSiteName(),
                    site.getAddress(),
                    site.getPhoneNumber(),
                    site.getContactName(),
                    site.getShippingZone(),
                    site.getSiteType(),
                    site.getBranch()
            );
        }

        deliveryManager.addSite(siteToAdd);
        saveSite(siteToAdd);
    }

    private Integer findExistingSiteIdByName(String siteName) {
        try {
            for (SiteDto dto : siteRepository.findAll()) {
                if (dto.getSiteName().equalsIgnoreCase(siteName)) {
                    return dto.getSiteId();
                }
            }

            return null;
        } catch (RepositoryException e) {
            throw new IllegalStateException("Failed to check if site already exists", e);
        }
    }

    public void addTruck(Truck truck) {
        deliveryManager.addTruck(truck);
        saveTruck(truck);
    }

    public void addDriver(Driver driver) {
        deliveryManager.addDriver(driver);
        saveDriver(driver);
    }

    private void saveShippingZone(ShippingZone shippingZone) {
        try {
            shippingZoneRepository.save(new ShippingZoneDto(
                    shippingZone.getZoneCode(),
                    shippingZone.getZoneName()
            ));
        } catch (RepositoryException e) {
            throw new IllegalStateException("Shipping zone added in memory but failed to save to database", e);
        }
    }

    private void saveSite(Site site) {
        try {
            siteRepository.save(new SiteDto(
                    site.getSiteId(),
                    site.getSiteName(),
                    site.getAddress(),
                    site.getContactName(),
                    site.getPhoneNumber(),
                    site.getShippingZone().getZoneCode(),
                    site.getSiteType().name()
            ));
        } catch (RepositoryException e) {
            throw new IllegalStateException("Site added in memory but failed to save to database", e);
        }
    }

    private void saveTruck(Truck truck) {
        try {
            truckRepository.save(new TruckDto(
                    truck.getLicenseNumber(),
                    truck.getModel(),
                    truck.getNetWeight(),
                    truck.getMaxAllowedWeight(),
                    truck.getRequiredLicenseType().name()
            ));
        } catch (RepositoryException e) {
            throw new IllegalStateException("Truck added in memory but failed to save to database", e);
        }
    }

    private void saveDriver(Driver driver) {
        try {
            driverRepository.save(new DriverDto(
                    driver.getEmployeeId(),
                    driver.getDriverName()
            ));
        } catch (RepositoryException e) {
            throw new IllegalStateException("Driver added in memory but failed to save to database", e);
        }
    }

    private int nextSiteId() {
        try {
            int maxId = 0;

            for (SiteDto dto : siteRepository.findAll()) {
                if (dto.getSiteId() > maxId) {
                    maxId = dto.getSiteId();
                }
            }

            return maxId + 1;
        } catch (RepositoryException e) {
            throw new IllegalStateException("Failed to generate site id", e);
        }
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

    public Delivery createDelivery(LocalDate deliveryDate,
                                Site source,
                                List<DeliveryStop> stops,
                                LocalTime departureTime,
                                double initialWeight,
                                Truck truck,
                                Driver driver,
                                ShippingZone shippingZone) {

        Delivery delivery = deliveryManager.createDelivery(
                deliveryDate,
                source,
                stops,
                departureTime,
                initialWeight,
                truck,
                driver,
                shippingZone
        );

        saveDeliveryChange(delivery);

        return delivery;
    }

    // private void saveDeliveryChange(Delivery delivery) {
    //     try {
    //         deliveryRepository.save(toDeliveryDto(delivery));
    //     } catch (RepositoryException e) {
    //         throw new IllegalStateException(
    //                 "Delivery changed in memory but failed to save to database",
    //                 e
    //         );
    //     }
    // }

    private void saveDeliveryChange(Delivery delivery) {
        try {
            DeliveryDto dto = toDeliveryDto(delivery);

            // System.out.println("DEBUG saving delivery:");
            // System.out.println("id = " + dto.getDeliveryId());
            // System.out.println("status = " + dto.getStatus());
            // System.out.println("sourceSiteId = " + dto.getSourceSiteId());

            deliveryRepository.save(dto);
            saveDeliveryStops(delivery);
        } catch (RepositoryException e) {
            throw new IllegalStateException(
                    "Delivery changed in memory but failed to save to database",
                    e
            );
        }
    }

    private DeliveryDto toDeliveryDto(Delivery delivery) {
        return new DeliveryDto(
                delivery.getDeliveryId(),
                delivery.getDeliveryDate().toString(),
                delivery.getSource().getSiteId(),
                delivery.getDepartureTime().toString(),
                delivery.getFinalMeasuredWeightBeforeDeparture(),
                delivery.getTruck().getLicenseNumber(),
                delivery.getDriver().getEmployeeId(),
                delivery.getShippingZone().getZoneCode(),
                delivery.getStatus().name()
        );
    }

    private void saveDeliveryStops(Delivery delivery) {
        try (java.sql.Connection connection = DatabaseConnection.getConnection()) {
            DeliveryStopDaoImpl stopDao = new DeliveryStopDaoImpl(connection);
            DeliveryDocumentDaoImpl documentDao = new DeliveryDocumentDaoImpl(connection);
            DeliveryItemDaoImpl itemDao = new DeliveryItemDaoImpl(connection);

            for (DeliveryStop stop : delivery.getStops()) {
                int stopId = generateStopId(delivery.getDeliveryId(), stop.getStopOrder());

                stopDao.createOrUpdate(new DeliveryStopDto(
                        stopId,
                        delivery.getDeliveryId(),
                        stop.getStopOrder(),
                        stop.getStopType().name(),
                        stop.getSite().getSiteId(),
                        stop.getPlannedArrivalDateTime().toString()
                ));

                replaceStopDocumentAndItems(connection, documentDao, itemDao, stopId, stop);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Delivery saved but failed to save delivery stops and items", e);
        }
    }

    private int generateStopId(int deliveryId, int stopOrder) {
        return deliveryId * 1000 + stopOrder + 1;
    }

    private void replaceStopDocumentAndItems(
            java.sql.Connection connection,
            DeliveryDocumentDaoImpl documentDao,
            DeliveryItemDaoImpl itemDao,
            int stopId,
            DeliveryStop stop) throws Exception {

        deletePersistedDocumentAndItemsForStop(connection, stopId);

        if (!stop.hasDocument()) {
            return;
        }

        DeliveryDocument document = stop.getDocument();
        documentDao.createOrUpdate(new DeliveryDocumentDto(
                document.getDocumentNumber(),
                stopId
        ));

        for (DeliveryItem item : document.getItems()) {
            itemDao.createOrUpdate(new DeliveryItemDto(
                    item.getItemId(),
                    document.getDocumentNumber(),
                    item.getItemName(),
                    item.getQuantity()
            ));
        }
    }

    private void deletePersistedDocumentAndItemsForStop(java.sql.Connection connection, int stopId)
            throws java.sql.SQLException {
        try (java.sql.PreparedStatement deleteItems = connection.prepareStatement("""
                DELETE FROM delivery_items
                WHERE document_number IN (
                    SELECT document_number
                    FROM delivery_documents
                    WHERE stop_id = ?
                )
                """);
             java.sql.PreparedStatement deleteDocuments = connection.prepareStatement(
                     "DELETE FROM delivery_documents WHERE stop_id = ?")) {
            deleteItems.setInt(1, stopId);
            deleteItems.executeUpdate();

            deleteDocuments.setInt(1, stopId);
            deleteDocuments.executeUpdate();
        }
    }

    public void cancelDelivery(Delivery delivery) {
        deliveryManager.cancelDelivery(delivery);
        saveDeliveryChange(delivery);
    }

    public void recordWeightMeasurement(Delivery delivery, double weight) {
        deliveryManager.recordWeightMeasurement(delivery, weight);
        saveDeliveryChange(delivery);
    }

    public boolean isOverweight(Delivery delivery) {
        return deliveryManager.isOverweight(delivery);
    }

    public void markDeliveryForReplan(Delivery delivery) {
        deliveryManager.markDeliveryForReplan(delivery);
        saveDeliveryChange(delivery);
    }

    public void replaceTruck(Delivery delivery, Truck newTruck) {
        deliveryManager.replaceTruck(delivery, newTruck);
        saveDeliveryChange(delivery);
    }

    public void replaceDriver(Delivery delivery, Driver newDriver) {
        deliveryManager.replaceDriver(delivery, newDriver);
        saveDeliveryChange(delivery);
    }

    public void addStopToDelivery(Delivery delivery, DeliveryStop newStop) {
        deliveryManager.addStop(delivery, newStop);
        saveDeliveryChange(delivery);
    }

    public void removeStopFromDeliveryByOrder(Delivery delivery, int stopOrder) {
        deliveryManager.removeStopByOrder(delivery, stopOrder);
        saveDeliveryChange(delivery);
    }

    public void updateStopDocumentItems(Delivery delivery, int stopOrder, List<DeliveryItem> updatedItems) {
        deliveryManager.updateStopDocumentItems(delivery, stopOrder, updatedItems);
        saveDeliveryChange(delivery);
    }

    public void dispatchDelivery(Delivery delivery) {
        deliveryManager.dispatchDelivery(delivery);
        saveDeliveryChange(delivery);
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

    public Delivery planDelivery(LocalDate deliveryDate,
                                 Site source,
                                 List<DeliveryStop> stops,
                                 LocalTime departureTime,
                                 double initialWeight,
                                 Truck truck,
                                 Driver driver,
                                 ShippingZone shippingZone) {
        return createDelivery(
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
}
