package transportation.domain;

import employee.domain.Branch;

public class Site {

    private static final ShippingZone DEFAULT_SHIPPING_ZONE =
            new ShippingZone("UNASSIGNED", "Unassigned Zone");

    private int siteId;
    private String siteName;
    private String address;
    private String phoneNumber;
    private String contactName;
    private ShippingZone shippingZone;
    private SiteType siteType;
    private Branch branch;

    public Site(int siteId, String siteName, String address, String phoneNumber, String contactName, ShippingZone shippingZone, SiteType siteType, Branch branch) {
        this(siteName, address, phoneNumber, contactName, shippingZone, siteType, branch);
        this.siteId = siteId;
    }

    public Site(String siteName, String address, String phoneNumber, String contactName) {
        this(siteName, address, phoneNumber, contactName, DEFAULT_SHIPPING_ZONE, SiteType.REGULAR, null);
    }

    public Site(String siteName, String address, String phoneNumber, String contactName, ShippingZone shippingZone) {
        this(siteName, address, phoneNumber, contactName, shippingZone, SiteType.REGULAR, null);
    }

    public Site(String siteName, String address, String phoneNumber, String contactName, ShippingZone shippingZone, SiteType siteType, Branch branch) {
        validateSiteName(siteName);
        validateAddress(address);
        validatePhoneNumber(phoneNumber);
        validateContactName(contactName);

        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }
        if (siteType == null) {
            throw new IllegalArgumentException("siteType cannot be null");
        }
        this.siteName = siteName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.shippingZone = shippingZone;
        this.siteType = branch != null ? SiteType.BRANCH : siteType;
        this.branch = branch;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public ShippingZone getShippingZone() {
        return shippingZone;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setAddress(String address) {
        validateAddress(address);
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        this.phoneNumber = phoneNumber;
    }

    public void setContactName(String contactName) {
        validateContactName(contactName);
        this.contactName = contactName;
    }

    public void setShippingZone(ShippingZone shippingZone) {
        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }
        this.shippingZone = shippingZone;
    }

    public void setSiteType(SiteType siteType) {
        if (siteType == null) {
            throw new IllegalArgumentException("siteType cannot be null");
        }
        if (siteType != SiteType.BRANCH) {
            this.branch = null;
        }
        this.siteType = siteType;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
        if (branch != null) {
            this.siteType = SiteType.BRANCH;
        }
    }

    public boolean belongsToZone(ShippingZone shippingZone) {
        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }
        return this.shippingZone.equals(shippingZone);
    }

    private void validateSiteName(String siteName) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("siteName cannot be empty");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address cannot be empty");
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber cannot be empty");
        }
    }

    private void validateContactName(String contactName) {
        if (contactName == null || contactName.isBlank()) {
            throw new IllegalArgumentException("contactName cannot be empty");
        }
    }

    @Override
    public String toString() {
        return "Site{" +
                "siteName='" + siteName + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", contactName='" + contactName + '\'' +
                ", shippingZone=" + shippingZone.getZoneCode() +
                ", siteType=" + siteType +
                ", branch=" + (branch != null ? branch.getBranchName() : "null") +
                '}';
    }

    
}