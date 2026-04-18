package domain;

public class Site {
    
    private String siteName;
    private String address;
    private String phoneNumber;
    private String contactName;

    public Site(String siteName, String address, String phoneNumber, String contactName){
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("siteName cannot be empty");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address cannot be empty");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber cannot be empty");
        }
        if (contactName == null || contactName.isBlank()) {
            throw new IllegalArgumentException("contactName cannot be empty");
        }
        
        this.siteName = siteName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
    }

    public String getSiteName() {
        return this.siteName;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getContactName() {
        return this.contactName;
    }

    public void setAddress(String address) {
        if (address == null || address.isBlank()){
            throw new IllegalArgumentException("address cannot be empty");
        }
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber){
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber cannot be empty");
        }
        this.phoneNumber = phoneNumber;
    }

    public void setContactName(String contactName){
        if (contactName == null || contactName.isBlank()) {
            throw new IllegalArgumentException("contactName cannot be empty");
        }
        this.contactName = contactName;
    }
}
