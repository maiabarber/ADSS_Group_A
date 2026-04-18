package domain;

import java.util.ArrayList;
import java.util.List;

public class DeliveryManager{

    private List<Delivery> deliveries;
    private List<Site> sites;
    private List<Truck> trucks;
    private List<Driver> drivers;

    public DeliveryManager(){
        this.deliveries = new ArrayList<>();
        this.sites = new ArrayList<>();
        this.trucks = new ArrayList<>();
        this.drivers = new ArrayList<>();
    }

    public List<Delivery> getDeliveries(){
        return new ArrayList<>(deliveries);
    }

    public List<Site> getSites(){
        return new ArrayList<>(sites);
    }

    public List<Truck> getTrucks(){
        return new ArrayList<>(trucks);
    }

    public List<Driver> getDrivers(){
        return new ArrayList<>(drivers);
    }

    public void addDelivery(Delivery delivery){
        if (delivery == null){
            throw new IllegalArgumentException("delivery cannot be null");
        }
        deliveries.add(delivery);
    }

    public void addSite(Site site){
        if (site == null){
            throw new IllegalArgumentException("site cannot be null");
        } 
        sites.add(site);
    }

    public void addTruck(Truck truck){
        if (truck == null){
            throw new IllegalArgumentException("truck cannot be null");
        } 
        trucks.add(truck);
    }

    public void addDriver(Driver driver){
        if (driver == null){
            throw new IllegalArgumentException("driver cannot be null");
        } 
        drivers.add(driver);
    }

    public boolean canAssignDriverToTruck(Driver driver, Truck truck){
        if (driver == null || truck == null){
            throw new IllegalArgumentException("driver and truck cannot be null");
        }
        return driver.hasLicenseType(truck.getRequiredLicenseType());
    }

}