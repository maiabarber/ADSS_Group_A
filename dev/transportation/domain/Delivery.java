package domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Delivery {

    private int deliveryId;
    private LocalDate deliveryDate;
    private List<DeliveryStop> stops;
    private LocalTime departureTime;
    private double finalMeasuredWeightBeforeDeparture;
    private Truck truck;
    private Driver driver;
    private Site source;
    private ShippingZone shippingZone;
    private DeliveryStatus status;
    private DeliveryForm deliveryForm;

    public Delivery(int deliveryId, 
                    LocalDate deliveryDate,
                    Site source,
                    List<DeliveryStop> stops,
                    LocalTime departureTime,
                    double finalMeasuredWeightBeforeDeparture,
                    Truck truck,
                    Driver driver,
                    ShippingZone shippingZone,
                    DeliveryStatus status,
                    DeliveryForm deliveryForm) {

        validateCoreFields(deliveryId, deliveryDate, source, stops, departureTime, truck, driver, shippingZone, status, deliveryForm);
        validateWeight(finalMeasuredWeightBeforeDeparture);

        this.deliveryDate = deliveryDate;
        this.source = source;
        this.stops = new ArrayList<>(stops);
        this.departureTime = departureTime;
        this.finalMeasuredWeightBeforeDeparture = finalMeasuredWeightBeforeDeparture;
        this.truck = truck;
        this.driver = driver;
        this.shippingZone = shippingZone;
        this.status = status;
        this.deliveryForm = deliveryForm;

        validateStopsArrivalTimes();

        if (!deliveryForm.hasMeasurements()) {
            deliveryForm.addWeightMeasurement(finalMeasuredWeightBeforeDeparture);
        }
        if (!allStopsBelongToShippingZone()) {
            throw new IllegalArgumentException("all stops and source must belong to the delivery shipping zone");
        }
    }

    // public Delivery(LocalDate deliveryDate,
    //                 List<DeliveryStop> stops,
    //                 LocalTime departureTime,
    //                 double actualWeightAtDeparture,
    //                 Truck truck,
    //                 Driver driver,
    //                 DeliveryDocument document) {

    //     if (document == null) {
    //         throw new IllegalArgumentException("document cannot be null");
    //     }
    //     if (stops == null) {
    //         throw new IllegalArgumentException("stops cannot be null");
    //     }
    //     if (stops.isEmpty()) {
    //         throw new IllegalArgumentException("stops cannot be empty in legacy constructor");
    //     }

   //     DeliveryStop firstStop = stops.get(0);
    //     firstStop.setDocument(document);

    //     validateCoreFields(
    //             deliveryDate,
    //             firstStop.getSite(),
    //             stops,
    //             departureTime,
    //             truck,
    //             driver,
    //             firstStop.getSite().getShippingZone(),
    //             DeliveryStatus.PLANNED,
    //             new DeliveryForm()
    //     );
    //     validateWeight(actualWeightAtDeparture);

    //     this.deliveryDate = deliveryDate;
    //     this.source = firstStop.getSite();
    //     this.stops = new ArrayList<>(stops);
    //     this.departureTime = departureTime;
    //     this.finalMeasuredWeightBeforeDeparture = actualWeightAtDeparture;
    //     this.truck = truck;
    //     this.driver = driver;
    //     this.shippingZone = firstStop.getSite().getShippingZone();
    //     this.status = DeliveryStatus.PLANNED;
    //     this.deliveryForm = new DeliveryForm();
    //     this.deliveryForm.addWeightMeasurement(actualWeightAtDeparture);
    // }

    public int getDeliveryId() {
        return deliveryId;
    }
    
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public Site getSource() {
        return source;
    }

    public List<DeliveryStop> getStops() {
        return new ArrayList<>(stops);
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public double getFinalMeasuredWeightBeforeDeparture() {
        return finalMeasuredWeightBeforeDeparture;
    }

    public double getActualWeightAtDeparture() {
        return getFinalMeasuredWeightBeforeDeparture();
    }

    public Truck getTruck() {
        return truck;
    }

    public Driver getDriver() {
        return driver;
    }

    public ShippingZone getShippingZone() {
        return shippingZone;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public DeliveryForm getDeliveryForm() {
        return deliveryForm;
    }

    public DeliveryDocument getDocument() {
        if (stops.isEmpty()) {
            return null;
        }
        return stops.get(0).getDocument();
    }

    public void setDriver(Driver driver) {
        ensureCanStillBeModified();
        if (driver == null) {
            throw new IllegalArgumentException("driver cannot be null");
        }
        this.driver = driver;
    }

    public void setTruck(Truck truck) {
        ensureCanStillBeModified();
        if (truck == null) {
            throw new IllegalArgumentException("truck cannot be null");
        }
        this.truck = truck;
    }

    public void setSource(Site source) {
        ensureCanStillBeModified();
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        this.source = source;
    }

    public void setShippingZone(ShippingZone shippingZone) {
        ensureCanStillBeModified();
        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }
        this.shippingZone = shippingZone;
    }

    public void setStatus(DeliveryStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        this.status = status;
    }

    public void setFinalMeasuredWeightBeforeDeparture(double finalMeasuredWeightBeforeDeparture) {
        ensureCanStillBeModified();
        validateWeight(finalMeasuredWeightBeforeDeparture);
        this.finalMeasuredWeightBeforeDeparture = finalMeasuredWeightBeforeDeparture;
        deliveryForm.addWeightMeasurement(finalMeasuredWeightBeforeDeparture);
    }

    public void setActualWeightAtDeparture(double actualWeightAtDeparture) {
        setFinalMeasuredWeightBeforeDeparture(actualWeightAtDeparture);
    }

    public void addStop(DeliveryStop stop) {
        ensureCanStillBeModified();
        if (stop == null) {
            throw new IllegalArgumentException("stop cannot be null");
        }
        this.stops.add(stop);
    }

    public void removeStop(DeliveryStop stop) {
        ensureCanStillBeModified();
        if (stop == null) {
            throw new IllegalArgumentException("stop cannot be null");
        }
        this.stops.remove(stop);
    }

    public void recordWeightMeasurement(double weight) {
        ensureCanStillBeModified();
        validateWeight(weight);
        deliveryForm.addWeightMeasurement(weight);
        this.finalMeasuredWeightBeforeDeparture = weight;

        if (isOverweight()) {
            this.status = DeliveryStatus.PENDING_REPLAN;
        }
    }

    public void markForReplan() {
        this.status = DeliveryStatus.PENDING_REPLAN;
    }

    public void markAsDispatched() {
        this.status = DeliveryStatus.DISPATCHED;
    }

    public boolean isOverweight() {
        return finalMeasuredWeightBeforeDeparture > truck.getMaxAllowedWeight();
    }

    public boolean canStillBeModified() {
        return status != DeliveryStatus.DISPATCHED;
    }

    public boolean allStopsBelongToShippingZone() {
        for (DeliveryStop stop : stops) {
            if (!stop.belongsToZone(shippingZone)) {
                return false;
            }
        }
        return source.belongsToZone(shippingZone);
    }

    private void validateCoreFields(int deliveryId, 
                                    LocalDate deliveryDate,
                                    Site source,
                                    List<DeliveryStop> stops,
                                    LocalTime departureTime,
                                    Truck truck,
                                    Driver driver,
                                    ShippingZone shippingZone,
                                    DeliveryStatus status,
                                    DeliveryForm deliveryForm) {    
        if (deliveryId <= 0) {
            throw new IllegalArgumentException("deliveryId must be positive");
        }
        if (deliveryDate == null) {
            throw new IllegalArgumentException("deliveryDate cannot be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (stops == null) {
            throw new IllegalArgumentException("stops cannot be null");
        }
        if (stops.isEmpty()) {
            throw new IllegalArgumentException("stops cannot be empty");
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
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        if (deliveryForm == null) {
            throw new IllegalArgumentException("deliveryForm cannot be null");
        }
    }

    private void validateWeight(double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("weight cannot be negative");
        }
    }

    
    private void validateStopsArrivalTimes() {
        LocalDateTime departureDateTime = LocalDateTime.of(deliveryDate, departureTime);

        for (DeliveryStop stop : stops) {
            if (stop.getPlannedArrivalDateTime().isBefore(departureDateTime)) {
                throw new IllegalArgumentException("stop arrival time cannot be before delivery departure time");
            }
        }
    }

    private void ensureCanStillBeModified() {
        if (!canStillBeModified()) {
            throw new IllegalStateException("dispatched delivery cannot be modified");
        }
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "deliveryDate=" + deliveryDate +
                ", departureTime=" + departureTime +
                ", finalMeasuredWeightBeforeDeparture=" + finalMeasuredWeightBeforeDeparture +
                ", truck=" + truck.getLicenseNumber() +
                ", driver=" + driver.getDriverName() +
                ", source=" + source.getSiteName() +
                ", shippingZone=" + shippingZone.getZoneCode() +
                ", status=" + status +
                ", stopsCount=" + stops.size() +
                '}';
    }
}