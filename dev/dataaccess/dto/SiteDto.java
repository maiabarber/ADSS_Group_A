package dataaccess.dto;

public final class SiteDto {
    private final int siteId;
    private final String siteName;
    private final String address;
    private final String contactName;
    private final String phoneNumber;
    private final String zoneCode;
    private final String siteType;

    public SiteDto(int siteId, String siteName, String address, String contactName, String phoneNumber, String zoneCode, String siteType) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.address = address;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.zoneCode = zoneCode;
        this.siteType = siteType;
    }

    public int getSiteId() {
        return siteId;
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

    public String getZoneCode() {
        return zoneCode;
    }

    public String getSiteType() {
        return siteType;
    }

}