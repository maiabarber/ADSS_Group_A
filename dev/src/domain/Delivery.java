package domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Delivery{

    private LocalDate deliveryDate;
    private List<DeliveryStop> stops;
    private LocalTime departureTime;
    private double actualWeightAtDeparture;
    private Truck truck;
    private Driver driver;
    private DeliveryDocument document;

    public Delivery(LocalDate deliveryDate, List<DeliveryStop> stops, LocalTime departureTime,
                    double actualWeightAtDeparture, Truck truck, Driver driver, DeliveryDocument document){
        
        if (deliveryDate == null || stops == null || departureTime == null ||
            truck == null || driver == null || document == null) {
            throw new IllegalArgumentException("delivery fields cannot be null");
        }
        if (actualWeightAtDeparture < 0){
            throw new IllegalArgumentException("actualWeightAtDeparture cannot be negative");
        }
        
        this.deliveryDate = deliveryDate;
        this.stops = new ArrayList<>(stops); // a copy of stops and not original because it's safer
        this.departureTime = departureTime;
        this.actualWeightAtDeparture = actualWeightAtDeparture;
        this.truck = truck;
        this.driver = driver;
        this.document = document;
    }

    public LocalDate getDeliveryDate(){
        return this.deliveryDate;
    }

    public List<DeliveryStop> getStops(){
        return new ArrayList<>(this.stops);
    }

    public LocalTime getDepartureTime(){
        return this.departureTime;
    }

    public double getActualWeightAtDeparture(){
        return this.actualWeightAtDeparture;
    }

    public Truck getTruck(){
        return this.truck;
    }

    public Driver getDriver(){
        return this.driver;
    }

    public DeliveryDocument getDocument(){
        return this.document;
    }

    public void setDriver(Driver driver){
        this.driver = driver;
    }

    public void setTruck(Truck truck){
        this.truck = truck;
    }

    public void setActualWeightAtDeparture(double actualWeightAtDeparture){
        if (actualWeightAtDeparture < 0){
            throw new IllegalArgumentException("actualWeightAtDeparture cannot be negative");
        }
        this.actualWeightAtDeparture = actualWeightAtDeparture;
    }

    public void addStop(DeliveryStop stop){
        this.stops.add(stop);
    }

    public void removeStop(DeliveryStop stop){
        this.stops.remove(stop);
    }

    public boolean isOverweight(){
        return actualWeightAtDeparture > truck.getMaxAllowedWeight();
    }

}