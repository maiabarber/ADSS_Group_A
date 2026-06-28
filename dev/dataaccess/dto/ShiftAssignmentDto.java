package dataaccess.dto;

public final class ShiftAssignmentDto {
    private final int assignmentId;
    private final int shiftId;
    private final String employeeId;
    private final String roleName;
    private final String status;

    public ShiftAssignmentDto(int assignmentId, int shiftId, String employeeId, String roleName, String status) {
        this.assignmentId = assignmentId;
        this.shiftId = shiftId;
        this.employeeId = employeeId;
        this.roleName = roleName;
        this.status = status;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public int getShiftId() {
        return shiftId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getStatus() {
        return status;
    }

}