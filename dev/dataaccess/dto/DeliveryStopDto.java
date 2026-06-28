package dataaccess.dto;

public final class DeliveryStopDto {
    private final int stopId;
    private final int deliveryId;
    private final int stopOrder;
    private final String stopType;
    private final int siteId;
    private final String plannedArrival;

    public DeliveryStopDto(int stopId, int deliveryId, int stopOrder, String stopType, int siteId, String plannedArrival) {
        this.stopId = stopId;
        this.deliveryId = deliveryId;
        this.stopOrder = stopOrder;
        this.stopType = stopType;
        this.siteId = siteId;
        this.plannedArrival = plannedArrival;
    }

    public int getStopId() {
        return stopId;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public String getStopType() {
        return stopType;
    }

    public int getSiteId() {
        return siteId;
    }

    public String getPlannedArrival() {
        return plannedArrival;
    }

}