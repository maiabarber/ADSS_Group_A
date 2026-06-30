package dataaccess.dto;

public final class DeliveryDto {
    private final int deliveryId;
    private final String deliveryDate;
    private final int sourceSiteId;
    private final String departureTime;
    private final double finalMeasuredWeight;
    private final String truckLicenseNumber;
    private final String driverEmployeeId;
    private final String zoneCode;
    private final String status;

    public DeliveryDto(int deliveryId, String deliveryDate, int sourceSiteId, String departureTime, double finalMeasuredWeight, String truckLicenseNumber, String driverEmployeeId, String zoneCode, String status) {
        this.deliveryId = deliveryId;
        this.deliveryDate = deliveryDate;
        this.sourceSiteId = sourceSiteId;
        this.departureTime = departureTime;
        this.finalMeasuredWeight = finalMeasuredWeight;
        this.truckLicenseNumber = truckLicenseNumber;
        this.driverEmployeeId = driverEmployeeId;
        this.zoneCode = zoneCode;
        this.status = status;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public int getSourceSiteId() {
        return sourceSiteId;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public double getFinalMeasuredWeight() {
        return finalMeasuredWeight;
    }

    public String getTruckLicenseNumber() {
        return truckLicenseNumber;
    }

    public String getDriverEmployeeId() {
        return driverEmployeeId;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getStatus() {
        return status;
    }

}