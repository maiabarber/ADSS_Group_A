package domain;

public class DeliveryStop {
    
    private int stopOrder;
    private StopType stopType;
    private Site site;

    public DeliveryStop(int stopOrder, StopType stopType, Site site){
        if (stopType == null || site == null) {
            throw new IllegalArgumentException("stopType and site cannot be null");
        }
        if (stopOrder < 0) {
            throw new IllegalArgumentException("stopOrder cannot be negative");
        }
        this.stopOrder = stopOrder;
        this.stopType = stopType;
        this.site = site;
    }

    public int getStopOrder(){
        return this.stopOrder;
    }

    public StopType getStopType(){
        return this.stopType;
    }

    public Site getSite(){
        return this.site;
    }

    public void setStopOrder(int stopOrder){
        if (stopOrder < 0){
            throw new IllegalArgumentException("stopOrder cannot be negative");
        }
        this.stopOrder = stopOrder;
    }

    public void setSite(Site site){
        if (site == null){
            throw new IllegalArgumentException("site cannot be null");
        }
        this.site = site;
    }

}
