package presentation;

import service.DeliveriesApplication;

public class DeliveriesUI {
    private DeliveriesApplication deliveriesApplication;

    public DeliveriesUI(){
        this.deliveriesApplication = new DeliveriesApplication();
    }

    public DeliveriesApplication getDeliveriesApplication(){
        return this.deliveriesApplication;
    }
    
}