package service;

import domain.DeliveryManager;

public class DeliveriesApplication {
    private DeliveryManager deliveryManager;

    public DeliveriesApplication(){
        this.deliveryManager = new DeliveryManager();
    }

    public DeliveryManager getDeliveryManager(){
        return this.deliveryManager;
    }

}