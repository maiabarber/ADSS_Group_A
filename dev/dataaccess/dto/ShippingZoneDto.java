package dataaccess.dto;

public final class ShippingZoneDto {
    private final String zoneCode;
    private final String zoneName;

    public ShippingZoneDto(String zoneCode, String zoneName) {
        this.zoneCode = zoneCode;
        this.zoneName = zoneName;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getZoneName() {
        return zoneName;
    }

}