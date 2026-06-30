package dataaccess.dto;

public final class EmployeeRoleDto {
    private final String employeeId;
    private final String roleName;

    public EmployeeRoleDto(String employeeId, String roleName) {
        this.employeeId = employeeId;
        this.roleName = roleName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getRoleName() {
        return roleName;
    }

}