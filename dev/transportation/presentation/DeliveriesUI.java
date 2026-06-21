package presentation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import domain.Delivery;
import domain.DeliveryItem;
import domain.DeliveryStatus;
import domain.DeliveryStop;
import domain.Driver;
import domain.ShippingZone;
import domain.Site;
import domain.Truck;
import service.DeliveriesApplication;

public class DeliveriesUI {

    private final DeliveriesApplication deliveriesApplication;
    private final Scanner scanner;

    public DeliveriesUI() {
        this(new DeliveriesApplication());
    }

    public DeliveriesUI(DeliveriesApplication deliveriesApplication) {
        if (deliveriesApplication == null) {
            throw new IllegalArgumentException("deliveriesApplication cannot be null");
        }
        this.deliveriesApplication = deliveriesApplication;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;

        System.out.println("======================================");
        System.out.println("  Super-Li Deliveries Management CLI  ");
        System.out.println("======================================");

        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1:
                        handleInitializeEmpty();
                        break;
                    case 2:
                        handleLoadSampleData();
                        break;
                    case 3:
                        handleShowDeliveries();
                        break;
                    case 4:
                        handleShowSites();
                        break;
                    case 5:
                        handleShowTrucks();
                        break;
                    case 6:
                        handleShowDrivers();
                        break;
                    case 7:
                        handleShowShippingZones();
                        break;
                    case 8:
                        handleCreateDelivery();
                        break;
                    case 9:
                        handleRecordWeightMeasurement();
                        break;
                    case 10:
                        handleReplanDelivery();
                        break;
                    case 11:
                        handleDispatchDelivery();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Exiting system. Goodbye.");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception ex) {
                System.out.println("Operation failed: " + ex.getMessage());
            }

            System.out.println();
        }
    }

    private void printMainMenu() {
        System.out.println("--------------- MAIN MENU ---------------");
        System.out.println("1. Initialize empty system");
        System.out.println("2. Load sample data");
        System.out.println("3. Show all deliveries");
        System.out.println("4. Show all sites");
        System.out.println("5. Show all trucks");
        System.out.println("6. Show all drivers");
        System.out.println("7. Show all shipping zones");
        System.out.println("8. Create new delivery");
        System.out.println("9. Record weight measurement");
        System.out.println("10. Replan delivery");
        System.out.println("11. Dispatch delivery");
        System.out.println("0. Exit");
        System.out.println("-----------------------------------------");
    }

    private void handleInitializeEmpty() {
        deliveriesApplication.initializeEmpty();
        System.out.println("System initialized with empty data.");
    }

    private void handleLoadSampleData() {
        deliveriesApplication.initializeWithSampleData();
        System.out.println("Sample data loaded successfully.");
    }

    private void handleShowDeliveries() {
        List<Delivery> deliveries = deliveriesApplication.getAllDeliveries();

        if (deliveries.isEmpty()) {
            System.out.println("There are no deliveries in the system.");
            return;
        }

        System.out.println("--------------- DELIVERIES ---------------");
        for (int i = 0; i < deliveries.size(); i++) {
            Delivery delivery = deliveries.get(i);
            System.out.println("[" + i + "] " + deliveriesApplication.getDeliverySummary(delivery));

            List<DeliveryStop> stops = delivery.getStops();
            for (DeliveryStop stop : stops) {
                String documentInfo = stop.hasDocument()
                        ? "Document #" + stop.getDocument().getDocumentNumber()
                        : "No document";

                System.out.println("    Stop #" + stop.getStopOrder()
                        + " | Type: " + stop.getStopType()
                        + " | Site: " + stop.getSite().getSiteName()
                        + " | " + documentInfo);
            }
        }
    }

    private void handleShowSites() {
        List<String> siteSummaries = deliveriesApplication.getAllSiteSummaries();

        if (siteSummaries.isEmpty()) {
            System.out.println("There are no sites in the system.");
            return;
        }

        System.out.println("--------------- SITES ---------------");
        for (String summary : siteSummaries) {
            System.out.println(summary);
        }
    }

    private void handleShowTrucks() {
        List<String> truckSummaries = deliveriesApplication.getAllTruckSummaries();

        if (truckSummaries.isEmpty()) {
            System.out.println("There are no trucks in the system.");
            return;
        }

        System.out.println("--------------- TRUCKS ---------------");
        for (String summary : truckSummaries) {
            System.out.println(summary);
        }
    }

    private void handleShowDrivers() {
        List<String> driverSummaries = deliveriesApplication.getAllDriverSummaries();

        if (driverSummaries.isEmpty()) {
            System.out.println("There are no drivers in the system.");
            return;
        }

        System.out.println("--------------- DRIVERS ---------------");
        for (String summary : driverSummaries) {
            System.out.println(summary);
        }
    }

    private void handleShowShippingZones() {
        List<String> shippingZoneSummaries = deliveriesApplication.getAllShippingZoneSummaries();

        if (shippingZoneSummaries.isEmpty()) {
            System.out.println("There are no shipping zones in the system.");
            return;
        }

        System.out.println("--------------- SHIPPING ZONES ---------------");
        for (String summary : shippingZoneSummaries) {
            System.out.println(summary);
        }
    }

    private void handleCreateDelivery() {
        ensurePlanningDataExists();

        LocalDate deliveryDate = readDate("Enter delivery date (YYYY-MM-DD): ");
        LocalTime departureTime = readTime("Enter departure time (HH:MM): ");

        ShippingZone shippingZone = chooseShippingZone();
        List<Site> sitesInZone = getSitesByZone(shippingZone);

        if (sitesInZone.isEmpty()) {
            System.out.println("There are no sites in the selected zone.");
            return;
        }

        Site source = chooseSiteFromList("Choose source site:", sitesInZone);
        Truck truck = chooseTruck();
        Driver driver = chooseCompatibleDriver(truck);

        int stopCount = readInt("How many stops does this delivery have? ");
        while (stopCount <= 0) {
            System.out.println("A delivery must have at least one stop.");
            stopCount = readInt("How many stops does this delivery have? ");
        }

        List<DeliveryStop> stops = new ArrayList<>();
        for (int i = 0; i < stopCount; i++) {
            System.out.println("Creating stop #" + i);
            DeliveryStop stop = buildStop(i, shippingZone);
            stops.add(stop);
        }

        double initialWeight = readDouble("Enter initial measured weight: ");
        while (initialWeight < 0) {
            System.out.println("Weight cannot be negative.");
            initialWeight = readDouble("Enter initial measured weight: ");
        }

        Delivery delivery = deliveriesApplication.planDelivery(
                deliveryDate,
                source,
                stops,
                departureTime,
                initialWeight,
                truck,
                driver,
                shippingZone
        );

        System.out.println("Delivery created successfully.");
        System.out.println(deliveriesApplication.getDeliverySummary(delivery));

        if (delivery.getStatus() == DeliveryStatus.PENDING_REPLAN) {
            System.out.println("Warning: delivery is overweight and was marked as PENDING_REPLAN.");
        }
    }

    private void handleRecordWeightMeasurement() {
        Delivery delivery = chooseDelivery("Choose delivery to record weight for:");

        double weight = readDouble("Enter measured weight: ");
        while (weight < 0) {
            System.out.println("Weight cannot be negative.");
            weight = readDouble("Enter measured weight: ");
        }

        deliveriesApplication.recordWeightMeasurement(delivery, weight);

        System.out.println("Weight recorded successfully.");
        System.out.println("Current status: " + deliveriesApplication.getDeliveryStatus(delivery));
        System.out.println("Overweight: " + deliveriesApplication.isOverweight(delivery));
    }

    private void handleReplanDelivery() {
        Delivery delivery = chooseDelivery("Choose delivery to replan:");

        boolean back = false;
        while (!back) {
            printReplanMenu();
            int choice = readInt("Choose replan option: ");

            try {
                switch (choice) {
                    case 1:
                        deliveriesApplication.markDeliveryForReplan(delivery);
                        System.out.println("Delivery marked as PENDING_REPLAN.");
                        break;
                    case 2:
                        Truck newTruck = chooseTruck();
                        deliveriesApplication.replaceTruck(delivery, newTruck);
                        System.out.println("Truck replaced successfully.");
                        break;
                    case 3:
                        Driver newDriver = chooseCompatibleDriver(delivery.getTruck());
                        deliveriesApplication.replaceDriver(delivery, newDriver);
                        System.out.println("Driver replaced successfully.");
                        break;
                    case 4:
                        DeliveryStop newStop = buildStop(
                                delivery.getStops().size(),
                                delivery.getShippingZone()
                        );
                        deliveriesApplication.addStopToDelivery(delivery, newStop);
                        System.out.println("Stop added successfully.");
                        break;
                    case 5:
                        int stopOrder = readInt("Enter stop order to remove: ");
                        deliveriesApplication.removeStopFromDeliveryByOrder(delivery, stopOrder);
                        System.out.println("Stop removed successfully.");
                        break;
                    case 6:
                        int targetStopOrder = readInt("Enter stop order whose document items should be updated: ");
                        List<DeliveryItem> updatedItems = readItemsFromUser();
                        deliveriesApplication.updateStopDocumentItems(delivery, targetStopOrder, updatedItems);
                        System.out.println("Stop document updated successfully.");
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception ex) {
                System.out.println("Replan operation failed: " + ex.getMessage());
            }
        }
    }

    private void handleDispatchDelivery() {
        Delivery delivery = chooseDelivery("Choose delivery to dispatch:");
        deliveriesApplication.dispatchDelivery(delivery);
        System.out.println("Delivery dispatched successfully.");
        System.out.println("Current status: " + deliveriesApplication.getDeliveryStatus(delivery));
    }

    private void printReplanMenu() {
        System.out.println("------------- REPLAN MENU -------------");
        System.out.println("1. Mark delivery for replan");
        System.out.println("2. Replace truck");
        System.out.println("3. Replace driver");
        System.out.println("4. Add stop");
        System.out.println("5. Remove stop by order");
        System.out.println("6. Update stop document items");
        System.out.println("0. Back to main menu");
        System.out.println("---------------------------------------");
    }

    private void ensurePlanningDataExists() {
        if (deliveriesApplication.getAllShippingZones().isEmpty()) {
            throw new IllegalStateException("No shipping zones found. Load sample data or add data first.");
        }
        if (deliveriesApplication.getAllSites().isEmpty()) {
            throw new IllegalStateException("No sites found. Load sample data or add data first.");
        }
        if (deliveriesApplication.getAllTrucks().isEmpty()) {
            throw new IllegalStateException("No trucks found. Load sample data or add data first.");
        }
        if (deliveriesApplication.getAllDrivers().isEmpty()) {
            throw new IllegalStateException("No drivers found. Load sample data or add data first.");
        }
    }

    private ShippingZone chooseShippingZone() {
        List<ShippingZone> shippingZones = deliveriesApplication.getAllShippingZones();

        System.out.println("Available shipping zones:");
        for (String summary : deliveriesApplication.getAllShippingZoneSummaries()) {
            System.out.println(summary);
        }

        int index = readInt("Choose shipping zone index: ");
        while (index < 0 || index >= shippingZones.size()) {
            System.out.println("Invalid index.");
            index = readInt("Choose shipping zone index: ");
        }

        return deliveriesApplication.getShippingZoneByIndex(index);
    }

    private Site chooseSiteFromList(String prompt, List<Site> sites) {
        System.out.println(prompt);
        for (int i = 0; i < sites.size(); i++) {
            Site site = sites.get(i);
            System.out.println("[" + i + "] "
                    + site.getSiteName()
                    + " | " + site.getAddress()
                    + " | Zone: " + site.getShippingZone().getZoneCode());
        }

        int index = readInt("Choose site index: ");
        while (index < 0 || index >= sites.size()) {
            System.out.println("Invalid index.");
            index = readInt("Choose site index: ");
        }

        return sites.get(index);
    }

    private Truck chooseTruck() {
        List<Truck> trucks = deliveriesApplication.getAllTrucks();

        if (trucks.isEmpty()) {
            throw new IllegalStateException("No trucks available.");
        }

        System.out.println("Available trucks:");
        for (String summary : deliveriesApplication.getAllTruckSummaries()) {
            System.out.println(summary);
        }

        int index = readInt("Choose truck index: ");
        while (index < 0 || index >= trucks.size()) {
            System.out.println("Invalid index.");
            index = readInt("Choose truck index: ");
        }

        return deliveriesApplication.getTruckByIndex(index);
    }

    private Driver chooseCompatibleDriver(Truck truck) {
        List<Driver> drivers = deliveriesApplication.getAllDrivers();

        if (drivers.isEmpty()) {
            throw new IllegalStateException("No drivers available.");
        }

        System.out.println("Available drivers:");
        for (String summary : deliveriesApplication.getAllDriverSummaries()) {
            System.out.println(summary);
        }

        int index = readInt("Choose driver index: ");
        while (index < 0 || index >= drivers.size()) {
            System.out.println("Invalid index.");
            index = readInt("Choose driver index: ");
        }

        Driver driver = deliveriesApplication.getDriverByIndex(index);

        while (!deliveriesApplication.canAssignDriverToTruck(driver, truck)) {
            System.out.println("Selected driver cannot drive the selected truck. Please choose another driver.");
            index = readInt("Choose driver index: ");
            while (index < 0 || index >= drivers.size()) {
                System.out.println("Invalid index.");
                index = readInt("Choose driver index: ");
            }
            driver = deliveriesApplication.getDriverByIndex(index);
        }

        return driver;
    }

    private Delivery chooseDelivery(String prompt) {
        List<Delivery> deliveries = deliveriesApplication.getAllDeliveries();

        if (deliveries.isEmpty()) {
            throw new IllegalStateException("No deliveries available.");
        }

        System.out.println(prompt);
        List<String> summaries = deliveriesApplication.getAllDeliverySummaries();
        for (String summary : summaries) {
            System.out.println(summary);
        }

        int index = readInt("Choose delivery index: ");
        while (index < 0 || index >= deliveries.size()) {
            System.out.println("Invalid index.");
            index = readInt("Choose delivery index: ");
        }

        return deliveriesApplication.getDeliveryByIndex(index);
    }

    private DeliveryStop buildStop(int stopOrder, ShippingZone shippingZone) {
        List<Site> sitesInZone = getSitesByZone(shippingZone);

        if (sitesInZone.isEmpty()) {
            throw new IllegalStateException("No sites available in the selected shipping zone.");
        }

        System.out.println("Stop type:");
        System.out.println("1. PICKUP");
        System.out.println("2. DROPOFF");

        int stopTypeChoice = readInt("Choose stop type: ");
        while (stopTypeChoice != 1 && stopTypeChoice != 2) {
            System.out.println("Invalid choice.");
            stopTypeChoice = readInt("Choose stop type: ");
        }

        Site site = chooseSiteFromList("Choose stop site:", sitesInZone);
        List<DeliveryItem> items = readItemsFromUser();

        if (stopTypeChoice == 1) {
            if (items.isEmpty()) {
                return deliveriesApplication.createPickupStop(stopOrder, site);
            }
            return deliveriesApplication.createPickupStop(stopOrder, site, items);
        } else {
            if (items.isEmpty()) {
                return deliveriesApplication.createDropoffStop(stopOrder, site);
            }
            return deliveriesApplication.createDropoffStop(stopOrder, site, items);
        }
    }

    private List<DeliveryItem> readItemsFromUser() {
        List<DeliveryItem> items = new ArrayList<>();

        int itemCount = readInt("How many items should be added to this stop/document? ");
        while (itemCount < 0) {
            System.out.println("Item count cannot be negative.");
            itemCount = readInt("How many items should be added to this stop/document? ");
        }

        for (int i = 0; i < itemCount; i++) {
            System.out.println("Creating item #" + (i + 1));

            String itemId = readNonEmptyString("Enter item id: ");

            while (isItemIdTaken(items, itemId)) {
                System.out.println("Id is already taken");
                itemId = readNonEmptyString("Enter item id: ");
            }

            String itemName = readNonEmptyString("Enter item name: ");
            int quantity = readInt("Enter quantity: ");

            while (quantity < 0) {
                System.out.println("Quantity cannot be negative.");
                quantity = readInt("Enter quantity: ");
            }

            items.add(new DeliveryItem(itemId, itemName, quantity));
        }

        return items;
    }

    private boolean isItemIdTaken(List<DeliveryItem> items, String itemId) {
        for (DeliveryItem item : items) {
            if (item.getItemId().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    private List<Site> getSitesByZone(ShippingZone shippingZone) {
        List<Site> result = new ArrayList<>();

        for (Site site : deliveriesApplication.getAllSites()) {
            if (site.belongsToZone(shippingZone)) {
                result.add(site);
            }
        }

        return result;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid integer. Please try again.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                return Double.parseDouble(input.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                return LocalDate.parse(input.trim());
            } catch (Exception ex) {
                System.out.println("Invalid date. Use format YYYY-MM-DD.");
            }
        }
    }

    private LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                return LocalTime.parse(input.trim());
            } catch (Exception ex) {
                System.out.println("Invalid time. Use format HH:MM.");
            }
        }
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            if (!input.trim().isEmpty()) {
                return input.trim();
            }

            System.out.println("Input cannot be empty.");
        }
    }
}