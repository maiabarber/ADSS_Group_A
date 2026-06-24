package dataaccess.dto;

import transportation.domain.SiteType;

public final class SiteDto {
    private final String siteName;
    private final String address;
    private final String contactName;
    private final String phoneNumber;
    private final ShippingZoneDto shippingZone;
    private final SiteType siteType;

    public SiteDto(
            String siteName,
            String address,
            String contactName,
            String phoneNumber,
            ShippingZoneDto shippingZone,
            SiteType siteType) {
        this.siteName = siteName;
        this.address = address;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.shippingZone = shippingZone;
        this.siteType = siteType;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getAddress() {
        return address;
    }

    public String getContactName() {
        return contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public ShippingZoneDto getShippingZone() {
        return shippingZone;
    }

    public SiteType getSiteType() {
        return siteType;
    }
}