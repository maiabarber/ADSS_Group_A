package domain;

import java.util.Objects;

public class ShippingZone {

    private String zoneCode;
    private String zoneName;

    public ShippingZone(String zoneCode, String zoneName) {
        if (zoneCode == null || zoneCode.isBlank()) {
            throw new IllegalArgumentException("zoneCode cannot be empty");
        }
        if (zoneName == null || zoneName.isBlank()) {
            throw new IllegalArgumentException("zoneName cannot be empty");
        }

        this.zoneCode = zoneCode;
        this.zoneName = zoneName;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        if (zoneName == null || zoneName.isBlank()) {
            throw new IllegalArgumentException("zoneName cannot be empty");
        }
        this.zoneName = zoneName;
    }

    @Override
    public String toString() {
        return "ShippingZone{" +
                "zoneCode='" + zoneCode + '\'' +
                ", zoneName='" + zoneName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ShippingZone other)) return false;
        return Objects.equals(zoneCode, other.zoneCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zoneCode);
    }
}