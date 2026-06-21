package domain;

public class Site {

    private static final ShippingZone DEFAULT_SHIPPING_ZONE =
            new ShippingZone("UNASSIGNED", "Unassigned Zone");

    private String siteName;
    private String address;
    private String phoneNumber;
    private String contactName;
    private ShippingZone shippingZone;

    public Site(String siteName, String address, String phoneNumber, String contactName) {
        this(siteName, address, phoneNumber, contactName, DEFAULT_SHIPPING_ZONE);
    }

    public Site(String siteName, String address, String phoneNumber, String contactName, ShippingZone shippingZone) {
        validateSiteName(siteName);
        validateAddress(address);
        validatePhoneNumber(phoneNumber);
        validateContactName(contactName);

        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }

        this.siteName = siteName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.shippingZone = shippingZone;
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
                '}';
    }
}