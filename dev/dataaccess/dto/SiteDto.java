package dataaccess.dto;

import transportation.domain.SiteType;

public class SiteDto {
    private Integer siteId;
    private String siteName;
    private String address;
    private String phoneNumber;
    private String contactName;
    private ShippingZoneDto shippingZone;
    private SiteType siteType;
    private BranchDto branch;

    public SiteDto(
        Integer siteId,
            String siteName,
            String address,
            String contactName,
            String phoneNumber,
            ShippingZoneDto shippingZone,
            SiteType siteType,
            BranchDto branch) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.address = address;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.shippingZone = shippingZone;
        this.siteType = siteType;
        this.branch = branch;
    }

    public Integer getId() {
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

    public ShippingZoneDto getShippingZone() {
        return shippingZone;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public BranchDto getBranch() {
        return branch;
    }
}