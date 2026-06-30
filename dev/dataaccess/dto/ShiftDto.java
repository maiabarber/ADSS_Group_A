package dataaccess.dto;

public final class ShiftDto {
    private final int shiftId;
    private final String shiftDate;
    private final String shiftType;
    private final Integer branchId;

    public ShiftDto(int shiftId, String shiftDate, String shiftType, Integer branchId) {
        this.shiftId = shiftId;
        this.shiftDate = shiftDate;
        this.shiftType = shiftType;
        this.branchId = branchId;
    }

    public int getShiftId() {
        return shiftId;
    }

    public String getShiftDate() {
        return shiftDate;
    }

    public String getShiftType() {
        return shiftType;
    }

    public Integer getBranchId() {
        return branchId;
    }

}