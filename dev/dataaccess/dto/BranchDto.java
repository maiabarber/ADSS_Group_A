package dataaccess.dto;

public final class BranchDto {
    private final String branchId;
    private final String branchName;
    private final String location;
    private final SiteDto deliveryStop;

    public BranchDto(String branchId, String branchName, String location, SiteDto deliveryStop) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.location = location;
        this.deliveryStop = deliveryStop;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getLocation() {
        return location;
    }

    public SiteDto getDeliveryStop() {
        return deliveryStop;
    }
}