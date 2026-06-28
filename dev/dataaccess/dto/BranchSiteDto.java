package dataaccess.dto;

public final class BranchSiteDto {
    private final int branchId;
    private final int siteId;

    public BranchSiteDto(int branchId, int siteId) {
        this.branchId = branchId;
        this.siteId = siteId;
    }

    public int getBranchId() {
        return branchId;
    }

    public int getSiteId() {
        return siteId;
    }

}