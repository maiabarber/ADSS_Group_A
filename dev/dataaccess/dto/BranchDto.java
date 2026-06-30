package dataaccess.dto;

public final class BranchDto {
    private final int branchId;
    private final String branchName;
    private final String address;

    public BranchDto(int branchId, String branchName, String address) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getAddress() {
        return address;
    }

}