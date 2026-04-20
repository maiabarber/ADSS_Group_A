package tests;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import domain.Delivery;
import domain.DeliveryDocument;
import domain.DeliveryItem;
import domain.DeliveryStop;
import domain.Driver;
import domain.LicenseType;
import domain.Site;
import domain.StopType;
import domain.Truck;

public class DeliveryTest {

    private Truck createTruck(double maxAllowedWeight) {
        return new Truck("123-45-678", "Volvo", 5000, maxAllowedWeight, LicenseType.C);
    }

    private Driver createDriver() {
        return new Driver("Eden", Set.of(LicenseType.C, LicenseType.B));
    }

    private Site createSite() {
        return new Site("Main Site", "Beer Sheva", "0501234567", "Dana");
    }

    private DeliveryStop createStop(int order) {
        return new DeliveryStop(order, StopType.PICKUP, createSite());
    }

    private DeliveryDocument createDocument() {
        List<DeliveryItem> items = new ArrayList<>();
        items.add(new DeliveryItem("1", "Milk", 10));
        return new DeliveryDocument(1001, items);
    }

    private Delivery createDelivery(double actualWeightAtDeparture) {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

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

    @Test
    void constructor_validInput_createsDeliverySuccessfully() {
        Delivery delivery = createDelivery(7000);

        assertNotNull(delivery);
        assertEquals(LocalDate.of(2026, 4, 20), delivery.getDeliveryDate());
        assertEquals(LocalTime.of(10, 30), delivery.getDepartureTime());
        assertEquals(7000, delivery.getActualWeightAtDeparture());
        assertEquals(1, delivery.getStops().size());
    }

    @Test
    void constructor_nullDeliveryDate_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        null,
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createDocument()
                )
        );
    }

    @Test
    void constructor_nullStops_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        null,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createDocument()
                )
        );
    }

    @Test
    void constructor_nullDepartureTime_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        stops,
                        null,
                        7000,
                        createTruck(10000),
                        createDriver(),
                        createDocument()
                )
        );
    }

    @Test
    void constructor_nullTruck_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        null,
                        createDriver(),
                        createDocument()
                )
        );
    }

    @Test
    void constructor_nullDriver_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        stops,
                        LocalTime.of(10, 30),
                        7000,
                        createTruck(10000),
                        null,
                        createDocument()
                )
        );
    }

    @Test
    void constructor_nullDocument_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

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
    void constructor_negativeActualWeight_throwsException() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

        assertThrows(IllegalArgumentException.class, () ->
                new Delivery(
                        LocalDate.of(2026, 4, 20),
                        stops,
                        LocalTime.of(10, 30),
                        -1,
                        createTruck(10000),
                        createDriver(),
                        createDocument()
                )
        );
    }

    @Test
    void getStops_returnsCopyAndNotOriginalList() {
        Delivery delivery = createDelivery(7000);

        List<DeliveryStop> stopsFromGetter = delivery.getStops();
        stopsFromGetter.add(createStop(2));

        assertEquals(1, delivery.getStops().size());
    }

    @Test
    void constructor_copiesStopsListAndDoesNotKeepOriginalReference() {
        List<DeliveryStop> originalStops = new ArrayList<>();
        originalStops.add(createStop(1));

        Delivery delivery = new Delivery(
                LocalDate.of(2026, 4, 20),
                originalStops,
                LocalTime.of(10, 30),
                7000,
                createTruck(10000),
                createDriver(),
                createDocument()
        );

        originalStops.add(createStop(2));

        assertEquals(1, delivery.getStops().size());
    }

    @Test
    void setActualWeightAtDeparture_validWeight_updatesWeight() {
        Delivery delivery = createDelivery(7000);

        delivery.setActualWeightAtDeparture(8000);

        assertEquals(8000, delivery.getActualWeightAtDeparture());
    }

    @Test
    void setActualWeightAtDeparture_negativeWeight_throwsException() {
        Delivery delivery = createDelivery(7000);

        assertThrows(IllegalArgumentException.class, () ->
                delivery.setActualWeightAtDeparture(-5)
        );
    }

    @Test
    void setDriver_updatesDriver() {
        Delivery delivery = createDelivery(7000);
        Driver newDriver = new Driver("Noa", Set.of(LicenseType.C1, LicenseType.C));

        delivery.setDriver(newDriver);

        assertEquals("Noa", delivery.getDriver().getDriverName());
    }

    @Test
    void setTruck_updatesTruck() {
        Delivery delivery = createDelivery(7000);
        Truck newTruck = new Truck("987-65-432", "MAN", 4000, 9000, LicenseType.C1);

        delivery.setTruck(newTruck);

        assertEquals("MAN", delivery.getTruck().getModel());
    }

    @Test
    void addStop_addsStopSuccessfully() {
        Delivery delivery = createDelivery(7000);

        delivery.addStop(createStop(2));

        assertEquals(2, delivery.getStops().size());
    }

    @Test
    void removeStop_removesExistingStop() {
        Delivery delivery = createDelivery(7000);
        DeliveryStop existingStop = delivery.getStops().get(0);

        delivery.removeStop(existingStop);

        assertEquals(0, delivery.getStops().size());
    }

    @Test
    void isOverweight_whenActualWeightGreaterThanTruckLimit_returnsTrue() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

        Delivery delivery = new Delivery(
                LocalDate.of(2026, 4, 20),
                stops,
                LocalTime.of(10, 30),
                12000,
                createTruck(10000),
                createDriver(),
                createDocument()
        );

        assertTrue(delivery.isOverweight());
    }

    @Test
    void isOverweight_whenActualWeightEqualsTruckLimit_returnsFalse() {
        List<DeliveryStop> stops = new ArrayList<>();
        stops.add(createStop(1));

        Delivery delivery = new Delivery(
                LocalDate.of(2026, 4, 20),
                stops,
                LocalTime.of(10, 30),
                10000,
                createTruck(10000),
                createDriver(),
                createDocument()
        );

        assertFalse(delivery.isOverweight());
    }

    @Test
    void isOverweight_whenActualWeightLessThanTruckLimit_returnsFalse() {
        Delivery delivery = createDelivery(7000);

        assertFalse(delivery.isOverweight());
    }
}